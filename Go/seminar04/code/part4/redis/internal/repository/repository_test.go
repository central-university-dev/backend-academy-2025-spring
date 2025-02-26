package repository_test

import (
	"context"
	"testing"

	"github.com/stretchr/testify/suite"

	"example.com/seminar04/part4/redis/internal/repository"
	testHelpers "example.com/seminar04/part4/redis/internal/testhelpers"
)

type RepositoryTestSuite struct {
	*testHelpers.Suite
}

func NewRepositoryTestSuite() *RepositoryTestSuite {
	return &RepositoryTestSuite{Suite: testHelpers.NewSuite(testHelpers.RedisVer7)}
}

func (s *RepositoryTestSuite) SetupTest() {
	s.Require().NoError(s.RedisClient().FlushDB(context.Background()).Err())
}

func (s *RepositoryTestSuite) TestIsAvailable() {
	ctx := context.Background()
	repo := repository.NewRepository(s.RedisClient())

	isAvailable, err := repo.IsAvailable(ctx)

	s.NoError(err)
	s.True(isAvailable)
}

func (s *RepositoryTestSuite) TestSetValue() {
	ctx := context.Background()
	repo := repository.NewRepository(s.RedisClient())

	s.NoError(repo.SetValue(ctx, "some-key", "some-value"))
}

func (s *RepositoryTestSuite) TestGetValue_NotFound() {
	ctx := context.Background()
	repo := repository.NewRepository(s.RedisClient())

	_, err := repo.GetValue(ctx, "non-existing-key")

	s.EqualError(err, "failed to get value for key non-existing-key: redis: nil")
}

func (s *RepositoryTestSuite) TestGetValue_Found() {
	ctx := context.Background()
	repo := repository.NewRepository(s.RedisClient())

	s.NoError(repo.SetValue(ctx, "existing-key", "value"))

	value, err := repo.GetValue(ctx, "existing-key")

	s.NoError(err)
	s.Equal("value", value)
}

func TestRepository(t *testing.T) {
	t.Parallel()

	suite.Run(t, NewRepositoryTestSuite())
}
