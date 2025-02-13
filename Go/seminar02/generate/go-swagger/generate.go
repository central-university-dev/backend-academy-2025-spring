package ogen

//go:generate rm -rf model restapi cmd
//go:generate swagger generate server --spec=../../swag/api.yml --api-package api --model-package model --strict-responders --strict-additional-properties
