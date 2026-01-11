#!/usr/bin/env bash
set -e

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

echo "Building JARs..."
(cd "$ROOT_DIR/ms-membership" && mvn clean package -DskipTests)
(cd "$ROOT_DIR/ms-product" && mvn clean package -DskipTests)
(cd "$ROOT_DIR/ms-order" && mvn clean package -DskipTests)

echo "Building Docker images..."
docker build -t ecommerce-membership:1.0 "$ROOT_DIR/ms-membership"
docker build -t ecommerce-product:1.0 "$ROOT_DIR/ms-product"
docker build -t ecommerce-order:1.0 "$ROOT_DIR/ms-order"

echo "build-all.sh completed successfully."
