#!/usr/bin/env bash
set -e

if [ $# -lt 1 ]; then
  echo "Usage: ./publish-all.sh <dockerhub-username>"
  exit 1
fi

USERNAME="$1"

echo "Tagging images..."
docker tag ecommerce-membership:1.0 "$USERNAME/ecommerce-membership:1.0"
docker tag ecommerce-product:1.0    "$USERNAME/ecommerce-product:1.0"
docker tag ecommerce-order:1.0      "$USERNAME/ecommerce-order:1.0"

echo "Pushing images to Docker Hub..."
docker push "$USERNAME/ecommerce-membership:1.0"
docker push "$USERNAME/ecommerce-product:1.0"
docker push "$USERNAME/ecommerce-order:1.0"

echo "publish-all.sh completed successfully."
