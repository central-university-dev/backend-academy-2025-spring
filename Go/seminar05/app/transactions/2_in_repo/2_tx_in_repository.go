package app

import (
	"context"
	"fmt"

	"example.com/seminar05/app/transactions/2_in_repo/repo"
	"github.com/jackc/pgx/v5/pgxpool"
)

type UserDepartmentRepository interface {
	AddUserToDepartment(context.Context, int) error
}

type App struct {
	userDepartmentRepository UserDepartmentRepository
}

func (u *App) addUserToDepartment(ctx context.Context, userID int) error {
	if err := u.userDepartmentRepository.AddUserToDepartment(ctx, userID); err != nil {
		return fmt.Errorf("add user to department: %w", err)
	}

	return nil
}

func NewApp(userDepartmentRepository UserDepartmentRepository) *App {
	return &App{
		userDepartmentRepository: userDepartmentRepository,
	}
}

func main() {
	pool := &pgxpool.Pool{}

	repository := repo.NewRepository(pool)

	app := NewApp(repository)

	_ = app.addUserToDepartment(context.Background(), 1)
}
