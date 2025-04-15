# **Backend Responsibilities in the CI/CD System**

## **1. Pipeline Execution Management**
The backend is responsible for **orchestrating the execution of CI/CD pipelines**, ensuring that:
- Pipelines are executed in the correct sequence.
- Dependencies are handled properly.
- Execution states (`Pending`, `Running`, `Success`, `Failed`, `Canceled`) are tracked.
- Execution failures halt the pipeline unless explicitly allowed.

### **Core Functions**
✔ **Receives Execution Requests** – Accepts pipeline execution requests from the CLI.  
✔ **Creates Pipeline Execution Records** – Logs new pipeline execution requests in the database.  
✔ **Schedules Jobs & Stages** – Determines execution order and schedules jobs accordingly.  
✔ **Tracks Execution Status** – Updates pipeline execution status based on worker reports.  
✔ **Handles Job Failures** – Stops execution unless `allow_failure` is set for the failed job.  
✔ **Finalizes Execution** – Marks the pipeline as `SUCCESS` or `FAILED` when execution completes.

### **API Endpoints**
- `POST /api/pipeline/execute` → Start pipeline execution.
- `GET /api/pipeline/status/{executionId}` → Fetch execution status.

---

## **2. Job & Stage Scheduling**
The backend **manages stage execution** and ensures jobs within a stage are executed **in the correct order**.

### **Core Functions**
✔ **Assigns Jobs to Worker Module** – Sends jobs to the worker when they are ready to run.  
✔ **Handles Job Dependencies** – Ensures dependent jobs run **only after** their dependencies complete.  
✔ **Monitors Execution Flow** – Checks if all jobs in a stage have finished before proceeding.  
✔ **Handles Stage Failures** – Cancels subsequent jobs if a failure occurs (unless `allow_failure` is set).  
✔ **Reports Stage Completion** – Marks a stage as `SUCCESS` or `FAILED` when all jobs finish.

### **API Endpoints**
- `POST /api/job/execute` → Assign a job to a worker.
- `POST /api/job/status` → Worker updates job execution status.

---

## **3. Job Execution Status & Communication**
The backend **tracks job execution updates from the worker** and takes action accordingly.

### **Core Functions**
✔ **Receives Execution Status Updates** – Worker reports job status (`RUNNING`, `SUCCESS`, `FAILED`).  
✔ **Stores Logs & Artifacts** – Saves job logs and artifacts for later retrieval.  
✔ **Triggers Next Stages** – Starts the next stage if all jobs in the current stage are complete.  
✔ **Handles Failed Jobs** – Cancels further execution if a non-`allow_failure` job fails.

### **API Endpoints**
- `POST /api/job/status` → Receive worker job execution updates.
- `POST /api/job/artifact/upload` → Worker uploads artifacts after execution.

---

## **4. Execution Summary & Reporting**
The backend **stores execution history** and provides APIs for retrieving reports.

### **Core Functions**
✔ **Stores Execution History** – Saves pipeline, stage, and job execution details.  
✔ **Retrieves Reports** – Fetches past executions based on filters like pipeline name, run number, and commit hash.  
✔ **Supports Different Levels of Reports**:
- **Pipeline-level report** → Status of an entire pipeline execution.
- **Stage-level report** → Status of a specific stage in a pipeline execution.
- **Job-level report** → Status of an individual job in a stage.

### **API Endpoints**
- `GET /api/report/pipeline/{pipelineName}` → Fetch all past executions for a pipeline.
- `GET /api/report/pipeline/{pipelineName}/stage/{stageName}` → Fetch past executions for a stage.
- `GET /api/report/pipeline/{pipelineName}/stage/{stageName}/job/{jobName}` → Fetch past executions for a job.

---

## **5. Execution Flow in the Backend**
1. **Receives Execution Request from CLI**
    - CLI sends a request to `POST /api/pipeline/execute`
    - Backend creates a **new pipeline execution record** in the database.
    - Identifies **initial stages and jobs** to run.

2. **Schedules & Assigns Jobs**
    - Backend checks job dependencies and assigns **ready-to-run jobs** to the worker.
    - Sends `POST /api/job/execute` to worker.

3. **Worker Executes Jobs**
    - Worker executes jobs and **sends status updates** back to backend (`POST /api/job/status`).
    - Backend **logs the status change** and determines if next jobs should start.

4. **Handles Job Failures & Stage Completion**
    - If a **job fails**, backend checks if `allow_failure = false`:
        - If **true**, pipeline execution stops.
        - If **false**, execution continues.
    - If all jobs in a **stage** succeed, backend marks the stage as **complete** and schedules the next stage.

5. **Finalizes Execution**
    - Once all jobs and stages are complete, backend updates pipeline status (`SUCCESS` or `FAILED`).
    - Stores execution summary for reporting.

6. **Provides Reports**
    - CLI fetches execution summaries from `/api/report/pipeline/{pipelineName}`
    - Backend retrieves **stored execution data** and returns structured report output.

---

# **Backend Module Overview**

- **Module:** Backend
- **Version:** 1.0
- **Last Updated:** Mar 5, 2025

## **Overview**

The backend module is responsible for orchestrating the execution of pipelines, stages, and jobs. It provides REST APIs for triggering and monitoring pipeline executions, manages database storage for execution history, and facilitates interactions between different modules (CLI, Worker, and Common).

## **Responsibilities**

### **1️⃣ Pipeline Execution Management**

- Receives pipeline execution requests from the CLI.
- Creates new pipeline execution records in the database.
- Schedules the execution of stages and jobs.
- Tracks and updates the status of pipeline execution.

### **2️⃣ Job and Stage Execution Management**

- Assigns jobs to workers and receives execution results.
- Ensures job dependencies are met before execution.
- Handles failure propagation based on `allow_failure` flags.
- Tracks stage execution and triggers next stage when applicable.

### **3️⃣ Execution State Tracking & Logs**

- Stores execution logs for debugging.
- Updates job, stage, and pipeline statuses as execution progresses.
- Provides APIs for querying execution history.

### **4️⃣ Report Feature**

- Generates and returns execution reports at different levels:
  - **Pipeline-level summary** (status, timestamps, commit hash)
  - **Stage-level summary** (stage name, execution time, job statuses)
  - **Job-level summary** (job execution status, logs, artifacts)
- Allows querying execution history via API endpoints.

------

## **Key Components**

### **1️⃣ Controllers (REST API Endpoints)**

| Controller           | Endpoint                                              | Description                          |
| -------------------- | ----------------------------------------------------- | ------------------------------------ |
| `HealthController`   | `/health`                                             | Health check endpoint                |
| `PipelineController` | `/api/pipeline/execute`                               | Triggers pipeline execution          |
| `JobController`      | `/api/job/execute`                                    | Assigns job execution                |
| `StageController`    | `/api/stage/status/{pipelineExecutionId}/{stageName}` | Fetches stage execution status       |
| `ReportController`   | `/api/report/pipeline/{pipelineName}`                 | Retrieves pipeline execution history |

### **2️⃣ Database Entities**

| Entity                    | Table                 | Purpose                             |
| ------------------------- | --------------------- | ----------------------------------- |
| `PipelineEntity`          | `pipelines`           | Stores pipeline configurations      |
| `PipelineExecutionEntity` | `pipeline_executions` | Tracks pipeline execution runs      |
| `StageEntity`             | `stages`              | Defines pipeline stages             |
| `StageExecutionEntity`    | `stage_executions`    | Tracks execution of pipeline stages |
| `JobEntity`               | `jobs`                | Stores job metadata                 |
| `JobExecutionEntity`      | `job_executions`      | Stores job execution data           |
| `ExecutionLogEntity`      | `execution_logs`      | Stores execution logs               |

### **3️⃣ Repositories (Database Access Layer)**

- `PipelineRepository`
- `PipelineExecutionRepository`
- `StageRepository`
- `StageExecutionRepository`
- `JobRepository`
- `JobExecutionRepository`
- `ExecutionLogRepository`

### **4️⃣ Services (Business Logic Layer)**

| Service                               | Purpose                                                     |
| ------------------------------------- | ----------------------------------------------------------- |
| `PipelineExecutionService`            | Manages pipeline execution flow                             |
| `StageExecutionService`               | Handles stage execution logic                               |
| `JobExecutionService`                 | Manages job execution assignments and status updates        |
| `ExecutionLogService`                 | Stores logs for pipeline, stage, and job executions         |
| **❗ ReportService (NOT IMPLEMENTED)** | **Should generate execution reports from database records** |

### **5️⃣ Mappers (Entity-to-DTO Converters)**

- `PipelineExecutionMapper`
- `StageExecutionMapper`
- `JobExecutionMapper`

------

## **🚨 Missing Implementations & To-Do List**

🔴 **Critical Missing Components:**

- 🚧 

  `ReportService` not implemented

  - Needs to generate structured execution reports (pipeline, stage, job-level)
  - Should aggregate data from execution tables
  - Must be integrated with `ReportController`

🔴 **Job Execution Flow Needs Implementation**

- `JobController.executeJob()` only returns a stub response.
- Needs integration with `WorkerClient` (currently empty).
- Should create a `JobExecutionEntity`, assign to a worker, and update status.

🔴 **Stage Execution Progression Logic**

- `StageExecutionService.finalizeStageExecution()` only finalizes a stage when all jobs succeed.
- Needs to handle scenarios where failures occur with `allow_failure`.
- Should notify `PipelineExecutionService` when all stages complete.

🔴 **Job Artifact Upload Handling**

- `JobController.uploadJobArtifacts()` is a stub.
- Needs to store artifact metadata and link them to `JobExecutionEntity`.

------

## **✅ Next Steps**

1. **Implement `ReportService`** and integrate with `ReportController`.
2. **Complete `WorkerClient`** to properly communicate with worker module.
3. **Fix `JobController.executeJob()`** to correctly assign jobs and track execution.
4. **Enhance failure handling in `StageExecutionService`**.
5. **Implement job artifact storage logic** in `JobController.uploadJobArtifacts()`.

------

## **🎯 Conclusion**

The backend module is well-structured but requires key implementations for full functionality. The most urgent tasks are building the `ReportService`, properly executing jobs via `WorkerClient`, and improving failure handling in stage execution. Once these gaps are filled, the backend will be fully capable of executing and tracking CI/CD pipelines.
