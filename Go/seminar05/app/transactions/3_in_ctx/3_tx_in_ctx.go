package __in_ctx

import (
	"context"

	department "example.com/seminar05/app/transactions/3_in_ctx/repo/derartment"
	"example.com/seminar05/app/transactions/3_in_ctx/repo/txs"
	"example.com/seminar05/app/transactions/3_in_ctx/repo/user"
	"github.com/jackc/pgx/v5/pgxpool"
)

type Transactor interface {
	WithTransaction(context.Context, func(context.Context) error) error
}

type UserRepository interface {
	GetUserName(context.Context, int) (string, error)
}

type DepartmentRepository interface {
	AddUser(context.Context, int, string) error
}

type App struct {
	transactor           Transactor
	userRepository       UserRepository
	departmentRepository DepartmentRepository
}

func (u *App) addUserToDepartment(ctx context.Context, userID int) error {
	return u.transactor.WithTransaction(ctx, func(ctx context.Context) error {
		name, err := u.userRepository.GetUserName(ctx, userID)
		if err != nil {
			return err
		}

		return u.departmentRepository.AddUser(ctx, userID, name)
	})
}

func NewApp(userRepo UserRepository, depRepo DepartmentRepository, tx Transactor) *App {
	return &App{
		transactor:           tx,
		userRepository:       userRepo,
		departmentRepository: depRepo,
	}
}

func main() {
	pool := &pgxpool.Pool{}

	tx := txs.NewTxBeginner(pool)
	userRepo := user.NewRepository(pool)
	depRepo := department.NewRepository(pool)

	app := NewApp(userRepo, depRepo, tx)

	_ = app.addUserToDepartment(context.Background(), 1)
}
