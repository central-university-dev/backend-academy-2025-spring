FROM golang:1.23-alpine AS builder
WORKDIR /build

COPY main.go ./
ENV GO111MODULE=off
RUN go build -o hello.out ./

FROM alpine:latest AS runtime

COPY --from=builder /build/hello.out /bin/hello.out

CMD ["hello.out"]
