package main

import (
	"database/sql"
	"fmt"

	"github.com/Masterminds/squirrel"
)

func main() {
	buildQueries()
}

func buildQueries() {
	builder := squirrel.StatementBuilder.PlaceholderFormat(squirrel.Dollar)

	query, args, _ := buildComplexSelectQuery(builder)
	fmt.Println(query)
	fmt.Println(args)
	fmt.Println()

	query, args, _ = buildInsertQuery(builder)
	fmt.Println(query)
	fmt.Println(args)
	fmt.Println()

	query, args, _ = buildUpdateQuery(builder)
	fmt.Println(query)
	fmt.Println(args)
	fmt.Println()

	query, args, _ = buildDeleteQuery(builder)
	fmt.Println(query)
	fmt.Println(args)
	fmt.Println()

	query, args, _ = buildComplexWhereQuery(builder)
	fmt.Println(query)
	fmt.Println(args)
	fmt.Println()

	query, args, err := buildSelectWithCaseQuery(builder)
	fmt.Println(query)
	fmt.Println(args)
	fmt.Println(err)
}

func buildComplexSelectQuery(builder squirrel.StatementBuilderType) (string, []interface{}, error) {
	return builder.Select("u.id", "u.name", "COUNT(o.id) AS order_count").
		From("users u").
		LeftJoin("orders o ON u.id = o.user_id").
		Where(squirrel.Eq{"u.active": true}).
		GroupBy("u.id", "u.name").
		Having("COUNT(o.id) > ?", 5).
		OrderBy("order_count DESC").
		Limit(10).
		ToSql()
}

func buildInsertQuery(builder squirrel.StatementBuilderType) (string, []interface{}, error) {
	return builder.Insert("orders").
		Columns("user_id", "amount").
		Values(squirrel.Expr("(SELECT id FROM users WHERE name = ?) ", "John Doe"), 100).
		ToSql()
}

func buildUpdateQuery(builder squirrel.StatementBuilderType) (string, []interface{}, error) {
	return builder.Update("users").
		Set("name", "Jane Doe").
		Set("active", false).
		Where(squirrel.Eq{"id": 1}).
		ToSql()
}

func buildDeleteQuery(builder squirrel.StatementBuilderType) (string, []interface{}, error) {
	return builder.Delete("orders").
		Where(squirrel.Eq{"user_id": squirrel.Expr("(SELECT id FROM users WHERE name = ?)", "John Doe")}).
		ToSql()
}

func buildComplexWhereQuery(builder squirrel.StatementBuilderType) (string, []interface{}, error) {
	return builder.Select("u.id", "u.name").
		From("users u").
		Where(
			squirrel.And{
				squirrel.Eq{"u.active": true},
				squirrel.Or{
					squirrel.Eq{"u.role": "admin"},
					squirrel.Eq{"u.role": "editor"},
				},
			},
		).
		OrderBy("u.created_at DESC").
		ToSql()
}

func buildSelectWithCaseQuery(builder squirrel.StatementBuilderType) (string, []interface{}, error) {
	caseStmt := squirrel.Case("u.active").
		When("true", "active").
		When("false", "inactive").
		Else(squirrel.Expr("?", "undefined"))

	return builder.Select("u.id", "u.name").Column(caseStmt).
		From("users u").
		Where(squirrel.Eq{"u.account_type": "premium"}).
		OrderBy("u.name ASC").
		ToSql()
}

func bdExec(db *sql.DB) {
	builder := squirrel.StatementBuilder.PlaceholderFormat(squirrel.Dollar)

	_, _ = builder.Update("orders").
		Set("order_count", squirrel.Expr("order_count + 1")).
		Where(squirrel.Eq{"id": 1}).
		RunWith(db).
		Exec()

	// RunWith in prepare
	builder = squirrel.StatementBuilder.PlaceholderFormat(squirrel.Dollar).RunWith(db)

	raw := builder.Select("id", "user_name").
		From("users").
		Where(squirrel.Eq{"id": 1}).
		QueryRow()

	var id int64
	var name string
	if err := raw.Scan(&id, &name); err != nil {
		panic(err)
	}
}
