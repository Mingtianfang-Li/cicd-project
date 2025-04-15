# System Design Document: CI/CD Pipeline System

**Author**: Wenxue Fang, Team 1, CS6510 Spring 2025
**Date**: April 11, 2025

## 1. System Components

### 1.1 Backend Component
**Responsibility**: Central orchestration service for pipeline execution
- **Key Services**:
    - PipelineExecutionService: Manages pipeline execution workflow
    - StageExecutionService: Handles stage execution logic
    - JobExecutionService: Processes individual job execution
    - ExecutionQueueService: Coordinates job scheduling
    - YamlConfigurationService: Parses pipeline definitions
- **Controllers**:
    - REST endpoints for pipeline, job, stage execution and reporting
- **Database Integration**:
    - Entity repositories for execution history and configuration
- **External Dependencies**:
    - Spring Boot, PostgreSQL, RabbitMQ

### 1.2 Worker Component
**Responsibility**: Executes jobs in isolated environments
- **Core Services**:
    - DockerExecutor: Runs jobs in Docker containers
    - GitCloneService: Manages repository access
    - WorkerJobQueue: Processes jobs from message queue
    - LogStorageService: Captures and stores execution logs
- **External Dependencies**:
    - Docker API, RabbitMQ, kubernetes
- **Resource Requirements**:
    - Docker socket access, shared volume mount

### 1.3 CLI Component
**Responsibility**: User interface for system interaction
- **Commands**:
    - RunCommand: Executes pipelines
    - CheckCommand: Validates configurations
    - ReportCommand: Retrieves execution reports
    - StatusCommand: Checks execution status
    - DryRunCommand: Displays execution order
- **Services**:
    - CliBackendClient: API communication
    - K8sService: Kubernetes deployment support

### 1.4 Common Component
**Responsibility**: Shared models and utilities
- **Models**: Pipeline, Stage, Job definitions
- **Validation**: Pipeline configuration validation
- **Utilities**: Git operations, logging, YAML parsing

## 2. Component Communication

### 2.1 REST API Communication
- **CLI → Backend**:
    - POST /api/pipeline/run: Trigger execution
    - GET /api/pipeline/status/{id}: Check status
    - GET /api/report/pipeline/{name}: Get reports

### 2.2 Message Queue Communication
- **Backend → Worker**:
    - RabbitMQ queues for job distribution
    - Message format: JobExecutionRequest
- **Worker → Backend**:
    - Status updates via RabbitMQ
    - Queue-based callback mechanism

### 2.3 Database Communication
- Shared PostgreSQL database
- Entity tables for pipeline, stage, job definitions
- Execution records for status tracking
- Transaction-based consistency

### 2.4 File System Communication
- Shared volume mounted at `/mnt/pipeline`
- Used for:
    - Pipeline definitions
    - Git repositories
    - Job working directories
    - Artifacts (before storage)

## 3. Deployment Topology

### 3.1 Local Development
- Components run as Java applications
- Shared PostgreSQL and RabbitMQ instances
- Local Docker daemon for job execution

### 3.2 Docker Compose Deployment
- Services defined in docker-compose.yml:
    - Backend container
    - Worker container
    - PostgreSQL database
    - RabbitMQ message broker
- Shared volume for pipeline data
- Docker socket mounted to worker

### 3.3 Kubernetes Deployment
- Backend: Deployment with ConfigMap for settings
- Worker: Deployment with privileged container access
- Shared PVC for pipeline data
- Service exposures for API access
- ConfigMaps for environment variables

## 4. Data Flow Diagram
1. User submits pipeline via CLI
2. Backend validates and creates execution records
3. Jobs dispatched to workers via RabbitMQ
4. Workers execute jobs in Docker containers
5. Execution results flow back to backend
6. Users query execution status and reports

## 5. Security and Failure Handling
- Isolated job execution in containers
- Environment-based secrets management
- Failed jobs propagate failure to parent stages
- Execution logs capture error details
- Database transactions ensure data consistency