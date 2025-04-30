package main

import (
	"fmt"
	"sync"
	"time"

	"github.com/samber/lo"
)

func NewListToHandle(cfg *Config) []int {
	list := make([]int, cfg.ListSize)
	for i := range list {
		list[i] = i
	}

	return list
}

func HandleListConcurrently(cfg *Config, list []int) {
	startedAt := time.Now()
	defer func() {
		fmt.Println("HandleListConcurrently duration =", time.Since(startedAt).String())
	}()

	workersQty := cfg.WorkerPoolSize
	chunks := SplitToChunks(list, workersQty)
	results := make([]int, workersQty)

	wg := sync.WaitGroup{}
	for i := 0; i < workersQty; i++ {
		wg.Add(1)
		go func(chunk []int, result *int) {
			defer wg.Done()
			for _, v := range chunk {
				if IsNumberEven(v, cfg.SlowFactor) {
					*result++
				}
			}
		}(chunks[i], &results[i])
	}
	wg.Wait()

	fmt.Println()
	fmt.Printf("There are %d even numbers in list\n", lo.Sum(results))
}

func IsNumberEven(number int, slowFactor int) (result bool) {
	for range slowFactor {
		result = number%2 == 0
	}
	return result
}

func SplitToChunks(list []int, chunksQty int) [][]int {
	result := make([][]int, chunksQty)

	totalQty := len(list)
	chunkLen := totalQty / chunksQty
	if totalQty%chunksQty != 0 {
		chunkLen++
	}

	for i := 0; i < chunksQty; i++ {
		chunkStart := i * chunkLen
		chunkEnd := (i + 1) * chunkLen
		if chunkEnd > totalQty {
			chunkEnd = totalQty
		}

		result[i] = list[chunkStart:chunkEnd]
	}

	return result
}
