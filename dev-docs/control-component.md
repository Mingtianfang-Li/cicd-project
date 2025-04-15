# Control Component Design Document

**Author**: Wenxue Fang, Team 1, CS6510 Spring 2025
**Date**: April 12, 2025

## Overview

This document describes the control components of the CI/CD Pipeline System. Control components are responsible for handling user interactions, processing commands, and orchestrating workflow between different system modules.

## Control Flow Architecture

The control flow in the CI/CD Pipeline System follows a multi-tier architecture pattern:

1. **Command-Line Interface (CLI)**: Processes user commands and translates them into API calls
2. **Backend API Controllers**: Accept RESTful requests and delegate to appropriate services
3. **Pipeline Orchestration**: Coordinates pipeline execution flow
4. **Worker Control**: Manages job execution with Docker

```
User → CLI Commands → Backend API → Service Layer → Database/Message Queue → Worker Execution
```

## CLI Command Structure

The CLI module serves as the primary user interface and implements the command pattern to handle different operations:

### Core Commands

| Command | Description | Component |
|---------|-------------|-----------|
| `check` | Validates pipeline configuration | `CheckCommand` |
| `run` | Executes a CI/CD pipeline | `RunCommand` |
| `report` | Retrieves execution reports | `ReportCommand` |
| `dry-run` | Shows execution plan without running | `DryRunCommand` |
| `status` | Fetches pipeline status | `StatusCommand` |

### Command Processor

Class: `CliApp.java`

Responsibilities:
- Parse command-line arguments using Picocli
- Route to appropriate command handler
- Process global options (repository, branch, etc.)
- Handle exit codes and output formatting

```java
@CommandLine.Command(
    name = "pipr",
    description = "A command-line interface for running and managing CI/CD pipelines.",
    mixinStandardHelpOptions = true,
    subcommands = {
        CheckCommand.class,
        RunCommand.class,
        ReportCommand.class,
        DryRunCommand.class,
        StatusCommand.class
    }
)
```

### Command Implementation

Each command follows a similar pattern:
1. Define command options and parameters with Picocli annotations
2. Implement `Callable<Integer>` interface
3. In `call()` method, process options and make API calls
4. Return appropriate exit code (0 for success, 1 for failure)

Example (simplified):
```java
@CommandLine.Command(name = "run")
public class RunCommand implements Callable<Integer> {
    @Option(names = {"--file", "-f"})
    private String filePath;
    
    @Override
    public Integer call() {
        try {
            // Validate input
            // Make API call
            // Process response
            return 0; // Success
        } catch (Exception e) {
            return 1; // Failure
        }
    }
}
```

## REST API Controllers

The backend module exposes RESTful APIs that are consumed by the CLI and potentially other clients.

### Key Controllers

| Controller | Endpoint Base | Responsibility |
|------------|---------------|----------------|
| `PipelineController` | `/api/pipeline` | Pipeline execution management |
| `ReportController` | `/api/report` | Execution reporting and history |
| `HealthController` | `/api/health` | System health checks |

### Pipeline Controller

Class: `PipelineController.java`

Endpoints:
- `POST /api/pipeline/run`: Starts pipeline execution
- `GET /api/pipeline/{pipelineName}`: Gets status for a specific pipeline

Control Flow:
1. Request validation
2. Delegate to `PipelineExecutionService`
3. Queue stage execution via `StageQueuePublisher`
4. Return execution ID and initial status

```java
@PostMapping("/run")
public ResponseEntity<?> runPipeline(@RequestBody PipelineExecutionRequest request) {
    // Validate request
    // Start pipeline execution
    Queue<Queue<UUID>> stageQueue = new LinkedList<>();
    PipelineExecutionResponse response = pipelineExecutionService.startPipelineExecution(request, stageQueue);
    stageQueuePublisher.dispatchStageQueue(stageQueue);
    return ResponseEntity.ok(response);
}
```

### Report Controller

Class: `ReportController.java`

Endpoints:
- `GET /api/report/pipeline/{name}`: Gets execution history for a pipeline
- `GET /api/report/pipeline/{name}/run/{runNumber}`: Gets details for a specific run
- `GET /api/report/pipeline/{name}/run/{runNumber}/stage/{stageName}`: Gets stage details
- `GET /api/report/pipeline/{name}/run/{runNumber}/stage/{stageName}/job/{jobName}`: Gets job details

## Worker Control Components

The worker module handles the actual execution of jobs within Docker containers.

### Worker Controllers

| Controller | Endpoint Base | Responsibility |
|------------|---------------|----------------|
| `WorkerStatusController` | `/api/worker/status` | Status reporting |
| `WorkerHealthController` | `/api/worker/health` | Worker health |
| `WorkerConfigController` | `/api/worker/config` | Worker configuration |

### Worker Execution Service

Class: `WorkerExecutionService.java`

Responsibilities:
- Execute jobs in Docker containers
- Process execution results
- Update job status
- Handle artifacts

Control Flow:
1. Receive job execution request from queue
2. Execute job using `DockerExecutor`
3. Process execution result and artifacts
4. Update job status via `JobDataService`

```java
public void executeJob(JobExecutionDTO job) {
    try {
        // Execute job in Docker
        ExecutionStatus result = dockerExecutor.execute(job);
        
        // Process result
        handleExecutionResult(job, result);
    } catch (Exception e) {
        // Handle errors
        jobDataService.updateJobStatus(job.getId(), ExecutionStatus.FAILED, "Job execution failed");
    }
}
```

## Pipeline Execution Flow

The pipeline execution process follows a coordinated flow across multiple components:

1. **CLI Command Processing**:
   - User runs `pipr run --file pipeline.yaml`
   - `RunCommand` validates the file
   - Sends request to backend API

2. **Backend Processing**:
   - `PipelineController` receives request
   - `PipelineExecutionService` clones repository
   - Parses and validates YAML configuration
   - Creates pipeline, stage, and job entities
   - Creates execution records
   - Enqueues stages for execution

3. **Stage Queue Management**:
   - `StageQueuePublisher` dispatches stage execution messages
   - Messages contain IDs of jobs to execute

4. **Worker Processing**:
   - `WorkerJobQueue` consumes messages
   - `WorkerExecutionService` processes job execution requests
   - `DockerExecutor` runs jobs in containers
   - Status updates sent back to backend

5. **Status Updates**:
   - Job status changes propagate to stage status
   - Stage status changes propagate to pipeline status
   - Status available via `StatusCommand` and API

## Error Handling Strategy

The control components implement a comprehensive error handling strategy:

1. **Command Validation**:
   - Input validation before API calls
   - Clear error messages for invalid inputs
   - Appropriate exit codes for CLI

2. **API Error Responses**:
   - Structured `ApiError` response format
   - HTTP status codes for different error types
   - Detailed error messages for client handling

3. **Exception Handling**:
   - Controller-level exception handlers
   - Transaction management with rollback
   - Logging of errors at appropriate levels

4. **Job Failure Management**:
   - `allow_failure` flag for non-critical jobs
   - Failed dependency tracking
   - Stage failure propagation