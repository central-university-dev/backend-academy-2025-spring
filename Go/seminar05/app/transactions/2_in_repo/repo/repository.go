package repo

import (
	"context"
	"errors"

	"github.com/jackc/pgx/v5/pgxpool"
)

func NewRepository(pool *pgxpool.Pool) *Repository {
	return &Repository{
		db: pool,
	}
}

type Repository struct {
	db *pgxpool.Pool
}

func (r *Repository) AddUserToDepartment(ctx context.Context, userID int) (err error) {
	tx, err := r.db.Begin(ctx)
	if err != nil {
		return err
	}

	defer func() {
		if err != nil {
			err = errors.Join(err, tx.Rollback(ctx))
		}
	}()

	var name string

	row := tx.QueryRow(ctx, "select name from users where id = $1", userID)
	if err = row.Scan(&name); err != nil {
		return err
	}

	_, err = tx.Exec(ctx, "insert into departments (user_id, user_name) values ($1, $2)", userID, name)
	if err != nil {
		return err
	}

	return tx.Commit(ctx)
}
