FROM golang:1.23-alpine
WORKDIR /usr/local/app

COPY main.go ./
ENV GO111MODULE=off
RUN go build -o hello.out ./

ENV PATH=/usr/local/app:$PATH
CMD ["hello.out"]
