package main

import (
	"log"
	"net/http"

	api "example.com/seminar02/api/oapi"
)

func main() {
	manager := taskManager{tasks: make(map[string]api.Task)}

	r := http.NewServeMux()
	h := api.HandlerFromMux(&manager, r)

	s := &http.Server{
		Handler: h,
		Addr:    ":8080",
	}

	log.Fatal(s.ListenAndServe())
}
