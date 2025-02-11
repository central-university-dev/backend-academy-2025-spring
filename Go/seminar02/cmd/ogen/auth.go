package main

import (
	"context"
	"fmt"

	api "example.com/seminar02/api/ogen"
)

type Auth struct {
	apiKey string
}

const PIMPApiKey = "P.I.M.P."

func (a *Auth) HandleApiKeyAuth(ctx context.Context, opName api.OperationName, t api.ApiKeyAuth) (context.Context, error) {
	if opName != api.TasksPostOperation {
		return ctx, nil
	}

	if t.GetAPIKey() != a.apiKey {
		return ctx, fmt.Errorf("invalid API key")
	}

	return context.WithValue(ctx, "Api-Key", t.GetAPIKey()), nil
}
