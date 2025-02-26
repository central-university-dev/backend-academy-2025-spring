package usecase

import (
	"net/http"
)

func NewRouter(get GetHandler, set SetHandler) *http.ServeMux {
	result := http.NewServeMux()

	result.Handle("GET /cache/get/{key}", get)
	result.Handle("POST /cache/set/{key}/{value}", set)

	return result
}
