#!/bin/bash
# Rapid development deployment script for K3s

set -e


if [[ -z "$1" ]]; then
  echo "ERROR: APP_NAME is required"
  exit 1
fi

if [[ -z "$2" ]]; then
  echo "ERROR: APP_DIR is required"
  exit 1
fi

APP_NAME="$1"
APP_DIR="$2"
TAG="${3:-latest}"
NAMESPACE="${4:-default}"

echo "Building ${APP_NAME}:${TAG}..."
docker build -t "${APP_NAME}:${TAG}" ${APP_DIR}

echo "Exporting image..."
docker save "${APP_NAME}:${TAG}" -o "/tmp/${APP_NAME}.tar"

echo "Importing to K3s..."
sudo k3s ctr images import "/tmp/${APP_NAME}.tar"

echo "Restarting deployment..."
# Force pod recreation to pull the new image
kubectl rollout restart deployment/${APP_NAME} -n ${NAMESPACE} 2>/dev/null || \
  echo "No existing deployment found, applying manifests..."

# Apply any manifest changes
if [ -f "${APP_DIR}/k8s/${APP_NAME}.yaml" ]; then
  kubectl apply -f "k8s/${APP_NAME}.yaml" -n ${NAMESPACE}
fi

echo "Waiting for rollout..."
kubectl rollout status deployment/${APP_NAME} -n ${NAMESPACE} --timeout=60s

echo "Deployment complete!"
kubectl get pods -n ${NAMESPACE} -l app=${APP_NAME}