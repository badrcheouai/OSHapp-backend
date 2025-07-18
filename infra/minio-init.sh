#!/bin/sh

export MINIO_ROOT_USER=minioadmin
export MINIO_ROOT_PASSWORD=minioadmin

mc alias set myminio http://minio:9000 $MINIO_ROOT_USER $MINIO_ROOT_PASSWORD
mc mb myminio/documents || true
mc mb myminio/rapports || true
mc mb myminio/fichiers-medicaux || true 