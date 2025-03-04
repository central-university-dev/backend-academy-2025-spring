package department

import (
	"context"
	"fmt"

	"example.com/seminar05/app/transactions/3_in_ctx/repo/txs"
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

func (r *Repository) AddUser(ctx context.Context, userID int, name string) error {
	querier := txs.GetQuerier(ctx, r.db)

	_, err := querier.Exec(ctx, "insert into departments (user_id, user_name) values ($1, $2)", userID, name)
	if err != nil {
		return fmt.Errorf("add user: %w", err)
	}

	return nil
}
