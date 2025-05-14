package config

type Tracing struct {
	JaegerEndpoint string `env:"JAEGER_ENDPOINT,required"`
	ServiceName    string `env:"SERVICE_NAME,required"`
	MainTracerName string `env:"MAIN_TRACER_NAME" envDefault:"main-tracer"`
}
