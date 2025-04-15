#!/bin/zsh

# Function to clean up processes when script is terminated
cleanup() {
    echo "Stopping services..."
    if [ -n "$WORKERPID" ]; then
        kill $WORKERPID 2>/dev/null
        wait $WORKERPID 2>/dev/null
    fi
    if [ -n "$BACKENDPID" ]; then
        kill $BACKENDPID 2>/dev/null
        wait $BACKENDPID 2>/dev/null
    fi
    echo "All services stopped."
    exit 0
}

# Set up the trap to catch the Ctrl+C (SIGINT) signal
trap cleanup SIGINT SIGTERM

# Drop all tables in postgres docker
echo "Resetting database..."
docker exec -it cicd-db psql -U postgres -d cicd_db -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
# docker exec -it cicd-db bash

# rm backend/.pipelines/*.yaml

# Start services
echo "Starting worker service..."
./gradlew :worker:bootRun > logs/worker.log 2>&1 &
WORKERPID=$!

echo "Starting backend service..."
./gradlew :backend:bootRun > logs/backend.log 2>&1 &
BACKENDPID=$!



echo "Services started. Press Ctrl+C to stop."

# Wait for user to press Ctrl+C
# This will keep the script running until Ctrl+C is pressed
wait $WORKERPID $BACKENDPID