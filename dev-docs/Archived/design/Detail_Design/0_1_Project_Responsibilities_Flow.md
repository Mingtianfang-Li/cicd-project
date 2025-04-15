# **CICD Module Responsibilities Execution Flow**

* **Author**: Yiwen Wang
* **Version**: 1.0
* **Last Updated**: Mar 4, 2025



## 🔹 Correct Execution Flow
### 1️⃣ CLI Receives YAML File
    - User provides a pipeline YAML file via the CLI.
    - The CLI validates and parses the file.
    - If valid, the CLI sends the parsed pipeline configuration to the backend.

### 2️⃣ Backend Receives Pipeline Execution Request

    - The backend does NOT fetch pipeline metadata from the database.
    - Instead, the backend receives all execution details from the CLI request.

### 3️⃣ Backend Starts Execution

    - It creates a new pipeline execution record in the database.
    - It identifies and schedules the first stage.
    - The StageExecutionService handles stage execution and triggers jobs.
    - The JobScheduler assigns jobs to the worker (which resolves dependencies).
    - The backend tracks execution history in the database.

### 4️⃣ Worker Executes Jobs & Reports Back

    - The worker runs jobs, handles dependencies, and sends results back.
    - Backend updates execution status after receiving worker reports.
    - If all jobs in a stage are completed successfully, backend schedules the next stage.
    - If a job fails and is not marked as "allow failure," the pipeline execution fails.

### 5️⃣ Backend Finalizes Execution
    - Once all stages are complete, backend updates final pipeline execution status.
    - Execution data is stored for reporting and retrieval via API.

## **1. Module Responsibilities**

Each module in the project has a specific role in executing and managing pipelines. The responsibilities are as follows:

### **Backend Module**
- Stores and manages execution history in the database.
- Orchestrates pipeline execution by scheduling and assigning jobs to the worker.
- Handles pipeline status updates and checks if all jobs in a stage are completed before proceeding to the next stage.
- Provides API endpoints for the CLI to trigger pipeline execution and fetch execution summaries.

### **CLI Module**
- Initiates pipeline execution requests by calling backend APIs.
- Provides a `--local` execution mode to run pipelines locally without interacting with the backend.
- Retrieves and displays execution reports from either the backend or local storage.
- Offers options to filter reports based on repository, pipeline, run number, stage, or job.

### **Common Module**
- Defines shared data models, enums, DTOs, and API contracts.
- Provides abstractions for communication between different modules.
- Does not contain any execution logic.

### **Worker Module**
- Executes jobs inside Docker containers.
- Handles job dependencies and ensures that required jobs are completed before executing dependent jobs.
- Reports execution logs and status updates to the backend.
- Runs each job’s script in sequence and applies failure handling rules.

---

## **2. Execution Flow for the Project**

1. **CLI Initiates Execution**
    - The user triggers a pipeline execution via the CLI.
    - The CLI sends a request to the backend for normal execution.
    - If the `--local` flag is used, the CLI executes the pipeline directly.

2. **Backend Schedules and Assigns Jobs**
    - The backend retrieves the pipeline configuration from the database.
    - It identifies the first stage’s jobs and assigns them to the worker.
    - If the previous stage is completed, the backend assigns jobs in the next stage.

3. **Worker Executes Jobs**
    - The worker receives job execution requests from the backend.
    - It verifies that all dependencies of a job are met before execution.
    - Each job runs inside a Docker container, executing its script.
    - Once a job completes, the worker sends logs and execution status back to the backend.

4. **Backend Updates Execution Status**
    - The backend stores execution logs and results in the database.
    - If all jobs in a stage succeed, the backend schedules the next stage.
    - If a job fails and is not marked as "allow failure," the pipeline fails.

5. **Completion and Reporting**
    - Once all stages complete, the backend marks the pipeline as `SUCCESS` or `FAILED`.
    - The CLI retrieves execution reports from the backend or local storage.

---

## **3. Report Function Implementation Plan**

### **Saving Reports to the Database (Backend)**
- When a job completes, the worker sends execution details (logs, status, timestamps) to the backend.
- The backend stores pipeline execution data in a database, including:
    - Pipeline name
    - Run number
    - Git commit hash
    - Start and completion timestamps
    - Execution status for each job, stage, and pipeline
- The CLI can query this data via backend API endpoints.

### **Saving Reports Locally (CLI)**
- If the `--local` flag is used, the CLI executes the pipeline directly.
- Execution reports are stored in `~/.xx-pipeline/reports/` in JSON or YAML format.
- The local report includes:
    - Pipeline name, run number, and commit hash
    - Execution logs and status for each job, stage, and pipeline
    - A separate record for each run

### **Retrieving Reports**
- The CLI fetches reports from:
    - The backend database for remote executions.
    - The `~/.xx-pipeline/reports/` directory for local executions.

---

## **4. Worker Module Plan**

### **Job Execution Inside Docker**
- The worker receives a job execution request with the following details:
    - Job name
    - Stage name
    - Docker image to use
    - List of commands to execute inside the container
- The worker starts a Docker container using the specified image.
- Each command in the script runs sequentially inside the container.
- If a command fails:
    - If `allow failure` is set to `true`, the worker logs the failure but continues execution.
    - If `allow failure` is `false`, the job fails, and execution stops.
- The worker captures logs and status, then sends them back to the backend.

### **Handling Job Dependencies**
- Each job can define dependencies on other jobs in the same stage.
- Before executing a job, the worker checks that all required dependencies have completed successfully.
- If dependencies are missing:
    - The worker retries checking their status periodically.
    - If dependencies do not complete within a timeout period, the job fails.
- The worker ensures that **cyclic dependencies are prevented** by checking for circular references in job configurations.

### **Worker Execution Flow**
1. **Receives job execution request from the backend.**
2. **Checks for required dependencies.**
    - If dependencies are incomplete, wait and retry.
    - If dependencies fail, the job fails immediately.
3. **Starts a Docker container using the specified image.**
4. **Runs each command in the job script inside the container.**
5. **Handles failures based on the `allow failure` setting.**
6. **Sends execution logs and status back to the backend.**

---
