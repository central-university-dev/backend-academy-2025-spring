package main

import (
	"context"
	"fmt"
	"log"
	"time"

	api "example.com/seminar02/api/ogen"
	"github.com/google/uuid"
)

type taskManager struct {
	api.UnimplementedHandler
	tasks map[uuid.UUID]api.Task
}

func (m *taskManager) TasksGet(_ context.Context) ([]api.Task, error) {
	tasks := make([]api.Task, 0, len(m.tasks))

	for _, t := range m.tasks {
		tasks = append(tasks, t)
	}

	log.Printf("returning tasks counter [%d]\n", len(tasks))

	return tasks, nil
}

func (m *taskManager) TasksPost(_ context.Context, req *api.TaskInput) (api.TasksPostRes, error) {
	if req == nil {
		return nil, fmt.Errorf("nil task input")
	}

	task := api.Task{
		ID:          api.NewOptUUID(uuid.New()),
		Title:       api.NewOptString(req.GetTitle()),
		Description: req.GetDescription(),
		Completed:   req.GetCompleted(),
		CreatedAt:   api.NewOptDateTime(time.Now()),
		UpdatedAt:   api.NewOptDateTime(time.Now()),
	}

	m.tasks[task.ID.Value] = task

	log.Printf("task created [%s]\n", task.ID.Value)

	return &task, nil
}

func (m *taskManager) TasksTaskIdGet(_ context.Context, params api.TasksTaskIdGetParams) (api.TasksTaskIdGetRes, error) {
	task, ok := m.tasks[params.TaskId]
	if !ok {
		log.Printf("task [%s] not found\n", task.ID.Value)

		return &api.TasksTaskIdGetNotFound{}, fmt.Errorf("task not found")
	}

	log.Printf("task was found [%s]\n", task.ID.Value)

	return &task, nil
}
