package app

import (
	"context"
	"errors"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type UserRepository interface {
	GetUserName(context.Context, pgx.Tx, int) (string, error)
}

type DepartmentRepository interface {
	AddUser(context.Context, pgx.Tx, int, string) error
}

type UserApp struct {
	db                   *pgxpool.Pool
	userRepository       UserRepository
	departmentRepository DepartmentRepository
}

func runInTx(ctx context.Context, db *pgxpool.Pool, fn func(ctx context.Context, tx pgx.Tx) error) (err error) {
	tx, err := db.Begin(ctx)
	if err != nil {
		return err
	}

	err = fn(ctx, tx)
	if err == nil {
		return tx.Commit(ctx)
	}

	return errors.Join(err, tx.Rollback(ctx))
}

func (u *UserApp) addUserToDepartment(ctx context.Context, userID int) error {
	return runInTx(ctx, u.db, func(ctx context.Context, tx pgx.Tx) error {
		name, err := u.userRepository.GetUserName(ctx, tx, userID)
		if err != nil {
			return err
		}

		return u.departmentRepository.AddUser(ctx, tx, userID, name)
	})
}
