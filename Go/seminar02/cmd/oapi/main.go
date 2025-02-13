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
		Handler: withPrintUrl(h),
		Addr:    ":8080",
	}

	log.Fatal(s.ListenAndServe())
}

func withPrintUrl(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		log.Printf("%s %s", r.Method, r.URL.Path)

		next.ServeHTTP(w, r)
	})
}
