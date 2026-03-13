#!/bin/bash
# FEROS API - Deploy / Update script
# Run this on EC2 to deploy or update the application
# Usage: ./deploy.sh

set -e

echo "==> Pulling latest code..."
git pull origin main

echo "==> Building and restarting API container..."
docker compose up -d --build api

echo "==> Cleaning up old Docker images..."
docker image prune -f

echo "==> Done! Current status:"
docker compose ps
