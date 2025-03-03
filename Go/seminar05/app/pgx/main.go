package main

import (
	"context"
	"embed"
	"fmt"
	"log"
	"sync"
	"time"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/stdlib"
	"github.com/pressly/goose/v3"
)

func main() {
	dsn := "postgres://postgres:postgres@localhost:5432/postgres?target_session_attrs=read-write&sslmode=disable"

	connConfig, err := pgx.ParseConfig(dsn)
	if err != nil {
		log.Fatal(err)
	}

	ctx := context.Background()

	migrate(connConfig)

	conn, err := pgx.ConnectConfig(ctx, connConfig)
	if err != nil {
		log.Fatal(err)
	}

	defer func() { _ = conn.Close(ctx) }()

	if err := scanRow(ctx, 3, conn); err != nil {
		log.Fatal(err)
	}

	scanParallel(ctx, conn)
}

func scanRow(ctx context.Context, id int, conn *pgx.Conn) (err error) {
	tx, err := conn.Begin(ctx)
	if err != nil {
		return fmt.Errorf("begin tx %d: %v", id, err)
	}

	defer func() {
		if p := recover(); p != nil {
			_ = tx.Rollback(ctx)
		}
	}()

	row := conn.QueryRow(ctx, "select status, created_at from example where id = $1", id)

	var (
		status    string
		createdAt time.Time
	)

	if err = row.Scan(&status, &createdAt); err != nil {
		return fmt.Errorf("scan row %d: %v", id, err)
	}

	log.Println(id, status, createdAt)

	return tx.Commit(ctx)
}

func scanParallel(ctx context.Context, conn *pgx.Conn) {
	fmt.Println("----------------")

	wg := sync.WaitGroup{}
	wg.Add(3)

	for i := 3; i < 6; i++ {
		go func() {
			defer wg.Done()

			if err := scanRow(ctx, i, conn); err != nil {
				log.Fatalf("select row %d: %v", i, err)
			}
		}()
	}

	wg.Wait()
}

//go:embed migrations/*.sql
var embedMigrations embed.FS

func migrate(cfg *pgx.ConnConfig) {
	goose.SetBaseFS(embedMigrations)

	if err := goose.SetDialect("postgres"); err != nil {
		log.Fatal("goose.SetDialect", err)
	}

	db := stdlib.OpenDB(*cfg)
	defer func() { _ = db.Close() }()

	if err := goose.Up(db, "migrations"); err != nil {
		log.Fatal("goose.Up", err)
	}
}
