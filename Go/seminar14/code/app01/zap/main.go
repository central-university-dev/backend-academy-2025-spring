package main

import (
	"go.uber.org/zap"
)

func main() {
	// console logger
	log := zap.Must(zap.NewProduction())
	defer log.Sync()

	log.Info(
		"order received",
		zap.String("customer", "Vasya Pupkin"),
		zap.Int("order_id", 123),
	)

	sugar := log.Sugar()
	sugar.Infow(
		"order received",
		"customer", "Vasya Pupkin",
		"order_id", 456,
	)

	// file logger
	prodConfig := zap.NewProductionConfig()
	prodConfig.OutputPaths = []string{"some-file.txt"}

	fileLog := zap.Must(prodConfig.Build())
	defer fileLog.Sync()

	fileLog.Info(
		"order received",
		zap.String("customer", "Vasya Pupkin"),
		zap.Int("order_id", 789),
	)
}
