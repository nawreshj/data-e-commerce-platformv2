#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"

echo "Pulling images from Docker Hub..."
docker compose pull

echo "Starting containers..."
docker compose up -d

echo "Running containers:"
docker compose ps
