package main

import (
	"fmt"
	"runtime"
)

func TraceConfigAndRuntimeData(cfg *Config) {
	fmt.Println("cfg.ListSize =", cfg.ListSize)
	fmt.Println("cfg.WorkerPoolSize =", cfg.WorkerPoolSize)
	fmt.Println("runtime.GOMAXPROCS =", runtime.GOMAXPROCS(0))
	fmt.Println("runtime.NumCPU = ", runtime.NumCPU())
}
