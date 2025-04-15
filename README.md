# CI/CD Pipeline System

A containerized CI/CD pipeline system with CLI interface for executing and monitoring pipelines.

**Team Project for CS6510 - Software Engineering, Spring 2025**  
**Team 1 | Date**: April 12, 2025
**Authors**:
- Wenxue Fang
- Shenqian Wen
- Mingtianfang Li

## Client Usage Guide

### Installation

```bash
# Clone the repository
git clone https://github.com/yourusername/pipr-project.git
cd pipr-project

# Build the project
./gradlew clean build

# Install CLI tool
./scripts/install.sh
```

### Pipeline Configuration

Create a YAML file defining your pipeline:

```yaml
name: my-pipeline
repository: https://github.com/user/repo
branch: main

stages:
  - name: build
    jobs:
      - name: compile
        image: openjdk:21-jdk-slim
        script:
          - "javac -d out src/main/java/**/*.java"
        allow_failure: false
        working_dir: "/app"

  - name: test
    jobs:
      - name: unit-tests
        image: openjdk:21-jdk-slim
        script:
          - "java -cp out org.junit.runner.JUnitCore TestSuite"
        dependencies: ["compile"]
```

### CLI Commands

#### Check a Pipeline Configuration
```bash
pipr check --file path/to/pipeline.yaml
```

#### Run a Pipeline
```bash
# From a YAML file
pipr run --file path/to/pipeline.yaml

# From a Git repository
pipr run --repo https://github.com/user/repo.git --branch develop
```

#### Check Pipeline Status
```bash
pipr status --pipeline my-pipeline
```

#### Get Execution Reports
```bash
# Get summary of all pipeline runs
pipr report --pipeline my-pipeline

# Get details for a specific run
pipr report --pipeline my-pipeline --run 5

# Get details for a specific stage
pipr report --pipeline my-pipeline --run 5 --stage build

# Get details for a specific job
pipr report --pipeline my-pipeline --run 5 --stage build --job compile
```

#### View Execution Plan (Dry Run)
```bash
pipr dry-run --file path/to/pipeline.yaml
```

### Global Options

All commands support these options:
- `--repo, -r`: Specify repository URL
- `--branch, -br`: Specify Git branch (default: main)
- `--commit, -co`: Specify commit hash
- `--local`: Run pipeline locally
- `--verbose`: Enable detailed logging
- `--help, -h`: Show command help

## Developer Guide

### Project Structure

This is a Gradle multi-module project:
- `backend`: Spring Boot application orchestrating pipeline execution
- `worker`: Spring Boot application executing jobs in Docker containers
- `cli`: Command-line interface for interacting with the backend
- `common`: Shared models, validation, and utilities

### Development Setup

#### Prerequisites
- Java 21
- Docker and Docker Compose
- Gradle

#### Build and Run

1. **Build all modules**
   ```bash
   ./gradlew clean build
   ```

2. **Create a Docker volume**
   ```bash
   docker volume create pipr
   ```

3. **Start services with Docker Compose**
   ```bash
   docker-compose up -d
   ```

4. **Install the CLI for development**
   ```bash
   ./scripts/install.sh
   ```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :backend:test
./gradlew :worker:test
./gradlew :cli:test
./gradlew :common:test
```

### Code Quality Checks

```bash
./gradlew check
```

### Adding New Features

1. Make changes to the respective module
2. Write unit tests
3. Ensure tests pass locally
4. Verify with integration tests
5. Submit a PR

### Deployment

#### Local Development
Run individual components as Java applications with shared services.

#### Docker Compose
```bash
docker volume create pipr  # Create this first
docker-compose up -d
```

#### Kubernetes
Deploy using manifests in the `k8s/` directory:
```bash
kubectl apply -f k8s/
```

## System Architecture

The system consists of four main components:
1. **Backend**: Orchestration service for pipeline execution
2. **Worker**: Executes jobs in isolated Docker containers
3. **CLI**: User interface for interaction
4. **Common**: Shared models and utilities

Components communicate via:
- REST APIs (CLI to Backend)
- Message queues (Backend to Worker)
- Shared PostgreSQL database
- Shared filesystem for artifacts