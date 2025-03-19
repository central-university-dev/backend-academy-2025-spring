package slowStorage

import (
	"context"
	"time"

	"example.com/seminar07/app02/internal/config"
)

type EmulatedRepository struct {
	cfg config.SlowStorage
}

func NewRepository(cfg config.SlowStorage) EmulatedRepository {
	return EmulatedRepository{cfg: cfg}
}

func (repo EmulatedRepository) GetValueForItem(ctx context.Context, _ string) (int, error) {
	timer := time.NewTimer(repo.cfg.Delay)
	defer timer.Stop()

	select {
	case <-ctx.Done():
		return 0, ctx.Err()
	case <-timer.C:
		const magicValue = 42

		return magicValue, nil
	}
}
