package main

import (
	"fmt"

	"github.com/doug-martin/goqu/v9"
	_ "github.com/doug-martin/goqu/v9/dialect/postgres"
	"github.com/google/uuid"
)

func main() {
	var err error
	goqu.SetDefaultPrepared(true)

	dialect := goqu.Dialect("postgres")

	query, args, _ := goquJoinNaked()
	fmt.Println(query)
	fmt.Println(args)
	fmt.Println()

	query, args, _ = goquJoin(dialect)
	fmt.Println(query)
	fmt.Println(args)
	fmt.Println()

	query, args, err = goquInsert(dialect)
	fmt.Println(query)
	fmt.Println(args)
	fmt.Println(err)

	query, args, err = goquInsertRows(dialect)
	fmt.Println(query)
	fmt.Println(args)
	fmt.Println(err)
}

func goquJoinNaked() (string, []any, error) {
	return goqu.From("users").
		LeftJoin(
			goqu.T("orders"),
			goqu.On(
				goqu.I("users.id").Eq("orders.user_id"),
			),
		).
		Select("users.id", "users.name", "orders.total").
		Where(goqu.L("users.active").Eq(true)).
		ToSQL()
}

func goquJoin(dialect goqu.DialectWrapper) (string, []any, error) {
	return dialect.From("users").
		LeftJoin(
			goqu.T("orders"),
			goqu.On(
				goqu.I("users.id").Eq("orders.user_id"),
			),
		).
		Select("users.id", "users.name", "orders.total").
		Where(goqu.Ex{"users.active": true}).
		ToSQL()
}

func goquInsert(dialect goqu.DialectWrapper) (string, []any, error) {
	type Item struct {
		Name     string    `db:"name" goqu:"omitempty"`
		ID       uuid.UUID `db:"id"`
		LastName *string   `db:"last_name"`
	}

	items := []Item{
		{
			Name:     "Don",
			ID:       uuid.New(),
			LastName: pointerOf("Don"),
		},
		{
			Name:     "Ben",
			ID:       uuid.New(),
			LastName: nil,
		},
	}

	return dialect.Insert("users").
		Rows(items).
		ToSQL()
}

func goquInsertRows(dialect goqu.DialectWrapper) (string, []any, error) {
	type Item struct {
		Name     string    `db:"name" goqu:"omitempty"`
		ID       uuid.UUID `db:"id"`
		LastName *string   `db:"last_name" goqu:"omitnil"`
	}

	items := []Item{
		{
			Name:     "Don",
			ID:       uuid.New(),
			LastName: pointerOf("Don"),
		},
		{
			Name:     "Ben",
			ID:       uuid.New(),
			LastName: nil,
		},
	}

	var rows []any
	for _, item := range items {
		rows = append(rows, item)
	}

	return dialect.Insert("users").
		Rows(rows).
		ToSQL()
}

func pointerOf[T any](t T) *T {
	return &t
}
