// This file is safe to edit. Once it exists it will not be overwritten

package restapi

import (
	"crypto/tls"
	"net/http"

	"github.com/go-openapi/errors"
	"github.com/go-openapi/runtime"

	swaggerapi "example.com/seminar02/generate/go-swagger/restapi/api"
	"example.com/seminar02/generate/go-swagger/restapi/api/open"
	"example.com/seminar02/generate/go-swagger/restapi/api/secure"
)

//go:generate swagger generate server --target ../../go-swagger --name SubscriptionAPI --spec ../../../swag/api.yml --api-package api --model-package model --principal interface{} --strict-responders

func configureFlags(api *swaggerapi.SubscriptionAPIAPI) {
	// api.CommandLineOptionsGroups = []swag.CommandLineOptionsGroup{ ... }
}

func configureAPI(api *swaggerapi.SubscriptionAPIAPI) http.Handler {
	// configure the api here
	api.ServeError = errors.ServeError

	// Set your custom logger if needed. Default one is log.Printf
	// Expected interface func(string, ...interface{})
	//
	// Example:
	// api.Logger = log.Printf

	api.UseSwaggerUI()
	// To continue using redoc as your UI, uncomment the following line
	// api.UseRedoc()

	api.JSONConsumer = runtime.JSONConsumer()

	api.JSONProducer = runtime.JSONProducer()

	// Applies when the "X-Auth-Token" header is set
	if api.APIKeyAuthAuth == nil {
		api.APIKeyAuthAuth = func(token string) (interface{}, error) {
			return nil, errors.NotImplemented("api key auth (ApiKeyAuth) X-Auth-Token from header param [X-Auth-Token] has not yet been implemented")
		}
	}

	// Set your custom authorizer if needed. Default one is security.Authorized()
	// Expected interface runtime.Authorizer
	//
	// Example:
	// api.APIAuthorizer = security.Authorized()

	if api.SecureAuthCheckHandler == nil {
		api.SecureAuthCheckHandler = secure.AuthCheckHandlerFunc(func(params secure.AuthCheckParams, principal interface{}) secure.AuthCheckResponder {
			return secure.AuthCheckNotImplemented()
		})
	}
	if api.OpenHealthCheckHandler == nil {
		api.OpenHealthCheckHandler = open.HealthCheckHandlerFunc(func(params open.HealthCheckParams) open.HealthCheckResponder {
			return open.HealthCheckNotImplemented()
		})
	}
	if api.OpenSubscribeMailingHandler == nil {
		api.OpenSubscribeMailingHandler = open.SubscribeMailingHandlerFunc(func(params open.SubscribeMailingParams) open.SubscribeMailingResponder {
			return open.SubscribeMailingNotImplemented()
		})
	}

	api.PreServerShutdown = func() {}

	api.ServerShutdown = func() {}

	return setupGlobalMiddleware(api.Serve(setupMiddlewares))
}

// The TLS configuration before HTTPS server starts.
func configureTLS(tlsConfig *tls.Config) {
	// Make all necessary changes to the TLS configuration here.
}

// As soon as server is initialized but not run yet, this function will be called.
// If you need to modify a config, store server instance to stop it individually later, this is the place.
// This function can be called multiple times, depending on the number of serving schemes.
// scheme value will be set accordingly: "http", "https" or "unix".
func configureServer(s *http.Server, scheme, addr string) {
}

// The middleware configuration is for the handler executors. These do not apply to the swagger.json document.
// The middleware executes after routing but before authentication, binding and validation.
func setupMiddlewares(handler http.Handler) http.Handler {
	return handler
}

// The middleware configuration happens before anything, this middleware also applies to serving the swagger.json document.
// So this is a good place to plug in a panic handling middleware, logging and metrics.
func setupGlobalMiddleware(handler http.Handler) http.Handler {
	return handler
}
