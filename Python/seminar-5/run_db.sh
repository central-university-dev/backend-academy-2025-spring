#!/bin/bash
docker run --name mydb -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=pwd -p 5432:5432 -d postgres:12.22-bookworm