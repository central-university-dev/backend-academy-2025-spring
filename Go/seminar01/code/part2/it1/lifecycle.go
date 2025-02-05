package main

import (
	"fmt"
	"net/http"
)

func RunServer(srv *http.Server) error {
	if err := srv.ListenAndServe(); err != nil {
		return fmt.Errorf("server failed to start or finished with error: %w", err)
	}

	return nil
}
