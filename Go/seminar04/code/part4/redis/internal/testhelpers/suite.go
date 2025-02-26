package testHelpers

import (
	"bytes"
	"context"
	_ "embed"
	"fmt"
	"regexp"
	"strconv"

	"github.com/redis/go-redis/v9"
	"github.com/stretchr/testify/suite"
	"github.com/testcontainers/testcontainers-go"
	redisModule "github.com/testcontainers/testcontainers-go/modules/redis"
)

type Suite struct {
	suite.Suite
	testcontainers.Container

	redisVersion string
	redisClient  *redis.Client
}

func NewSuite(redisVersion string) *Suite {
	return &Suite{redisVersion: redisVersion}
}

var (
	//go:embed conf/users.acl
	UsersAclFileContent []byte

	//go:embed conf/redis.conf
	RedisConfFileContent []byte
)

func (t *Suite) SetupSuite() {
	ctx := context.Background()
	container, err := redisModule.Run(
		ctx, fmt.Sprintf("redis:%s", t.redisVersion),
		testcontainers.CustomizeRequest(testcontainers.GenericContainerRequest{
			ContainerRequest: testcontainers.ContainerRequest{
				Cmd: []string{
					RedisServerCommand,
					RedisConfPath,
					"--loglevel",
					string(redisModule.LogLevelVerbose),
				},
				Files: []testcontainers.ContainerFile{
					{
						Reader:            bytes.NewBuffer(RedisConfFileContent),
						ContainerFilePath: RedisConfPath,
						FileMode:          DefaultFileMode,
					},
					{
						Reader:            bytes.NewBuffer(UsersAclFileContent),
						ContainerFilePath: UsersAclPath,
						FileMode:          DefaultFileMode,
					},
				},
			},
		}),
	)

	t.Require().NoError(err, "failed to start container")
	t.Container = container

	connString, err := container.ConnectionString(ctx)
	t.Require().NoError(err, "failed to get connection string")

	host, port, err := extractHostAndPortFromConnectionString(connString)
	t.Require().NoError(err, "failed to extract host and port from connection string")

	t.redisClient = redis.NewClient(&redis.Options{
		Addr:     fmt.Sprintf("%s:%d", host, port),
		Username: RedisUsername,
		Password: RedisPassword,
	})
}

func (t *Suite) TearDownSuite() {
	if t.Container != nil {
		t.NoError(t.Container.Terminate(context.Background()))
	}
}

func (t *Suite) RedisClient() *redis.Client {
	return t.redisClient
}

func extractHostAndPortFromConnectionString(connString string) (host string, port int, err error) {
	connStringRe := regexp.MustCompile(`^redis://(.+):(\d+)$`)
	submatch := connStringRe.FindAllStringSubmatch(connString, -1)
	if len(submatch) != 1 || len(submatch[0]) != 3 {
		return "", 0, fmt.Errorf("failed to parse host and port from connection string %s", connString)
	}

	port, err = strconv.Atoi(submatch[0][2])
	if err != nil {
		return "", 0, err
	}

	return submatch[0][1], port, nil
}
