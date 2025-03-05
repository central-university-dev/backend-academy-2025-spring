package user

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

func (r *Repository) GetUserName(ctx context.Context, userID int) (string, error) {
	querier := txs.GetQuerier(ctx, r.db)

	var name string

	row := querier.QueryRow(ctx, "select name from users where id = $1", userID)
	if err := row.Scan(&name); err != nil {
		return "", fmt.Errorf("get user name: %w", err)
	}

	return name, nil
}
