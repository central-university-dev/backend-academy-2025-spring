package main

import (
	"context"
	"fmt"
	"log"
	"sync"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"
)

func main() {
	dsn := "postgres://postgres:postgres@localhost:5432/postgres?target_session_attrs=read-write&sslmode=disable"

	connConfig, err := pgxpool.ParseConfig(dsn)
	if err != nil {
		log.Fatal(err)
	}

	connConfig.MaxConns = 10
	connConfig.MinConns = 2
	connConfig.MaxConnIdleTime = time.Second

	ctx := context.Background()

	pool, err := pgxpool.NewWithConfig(ctx, connConfig)
	if err != nil {
		log.Fatal(err)
	}

	defer pool.Close()

	time.Sleep(time.Second)
	fmt.Println("conn pool", pool.Stat().TotalConns())

	scanParallel(ctx, pool)

	fmt.Println("after scan conn pool", pool.Stat().TotalConns())
}

func scanRow(ctx context.Context, id int, pool *pgxpool.Pool) (err error) {
	tx, err := pool.Begin(ctx)
	if err != nil {
		return fmt.Errorf("begin tx %d: %v", id, err)
	}

	defer func() {
		if p := recover(); p != nil {
			_ = tx.Rollback(ctx)
		}
	}()

	row := pool.QueryRow(ctx, "select status, created_at from example where id = $1", id)

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

func scanParallel(ctx context.Context, pool *pgxpool.Pool) {
	wg := sync.WaitGroup{}

	for i := 1; i < 8; i++ {
		wg.Add(1)

		go func() {
			defer wg.Done()

			if err := scanRow(ctx, i, pool); err != nil {
				log.Fatalf("select row %d: %v", i, err)
			}
		}()
	}

	wg.Wait()
}
