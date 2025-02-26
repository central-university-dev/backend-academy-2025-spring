FROM golang:1.23-alpine AS builder
WORKDIR /build

COPY ./go.mod .
COPY ./go.sum .
RUN go mod download

COPY . .
RUN go build -o app ./cmd

FROM alpine:latest AS runtime

COPY --from=builder /build/app /bin/app

CMD ["/bin/app"]
