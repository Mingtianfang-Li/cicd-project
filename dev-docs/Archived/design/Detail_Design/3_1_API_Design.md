# **API Design for CI/CD System**
* **Author**: Yiwen Wang
* **Version**: 1.0
* **Last Updated**: Mar 4, 2025

## **Overview**
This document outlines the API design for the CI/CD system based on the provided **repository structure**, **execution flow**, and **module responsibilities**. The API will facilitate communication between the CLI, backend, and worker modules.

---

## **1. API Endpoints**

### **1.1. Pipeline Execution API**
**Endpoint:** `/api/pipeline/execute`
- **Method:** `POST`
- **Description:** Triggers a pipeline execution request.
- **Request Body:**
  ```json
  {
    "repo": "https://github.com/org/repo",
    "branch": "main",
    "commit": "abcdef123456",
    "pipeline": "build-pipeline"
  }
  ```
- **Response:**
  ```json
  {
    "executionId": "12345",
    "status": "PENDING"
  }
  ```

### **1.2. Pipeline Execution Status API**
**Endpoint:** `/api/pipeline/status/{executionId}`
- **Method:** `GET`
- **Description:** Retrieves the status of a pipeline execution.
- **Response:**
  ```json
  {
    "executionId": "12345",
    "status": "RUNNING",
    "stages": [
      {"stage": "build", "status": "COMPLETED"},
      {"stage": "test", "status": "RUNNING"}
    ]
  }
  ```

### **1.3. Job Execution API (Worker Assignment)**
**Endpoint:** `/api/job/execute`
- **Method:** `POST`
- **Description:** Assigns a job to the worker for execution.
- **Request Body:**
  ```json
  {
    "executionId": "12345",
    "stage": "build",
    "job": "compile",
    "dockerImage": "gradle:8.12-jdk21",
    "commands": ["./gradlew build"]
  }
  ```
- **Response:**
  ```json
  {
    "jobExecutionId": "67890",
    "status": "QUEUED"
  }
  ```

### **1.4. Job Status Update API (Worker → Backend)**
**Endpoint:** `/api/job/status`
- **Method:** `POST`
- **Description:** Allows workers to update job execution status.
- **Request Body:**
  ```json
  {
    "jobExecutionId": "67890",
    "status": "SUCCESS",
    "logs": "Compilation completed successfully."
  }
  ```
- **Response:**
  ```json
  {"message": "Job status updated."}
  ```

### **1.5. Artifact Upload API**
**Endpoint:** `/api/job/artifact/upload`
- **Method:** `POST`
- **Description:** Allows workers to upload artifacts after job execution.
- **Request Body:**
  ```json
  {
    "jobExecutionId": "67890",
    "artifacts": [
      {"path": "build/output.jar", "size": 102400},
      {"path": "logs/test.log", "size": 2048}
    ]
  }
  ```
- **Response:**
  ```json
  {"message": "Artifacts uploaded successfully."}
  ```

### **1.6. Job Cancellation API**
**Endpoint:** `/api/job/cancel/{jobExecutionId}`
- **Method:** `POST`
- **Description:** Cancels a running job.
- **Response:**
  ```json
  {"message": "Job execution canceled successfully."}
  ```

### **1.7. Duplicate Execution Detection API**
**Endpoint:** `/api/pipeline/check-duplicate`
- **Method:** `POST`
- **Description:** Checks if a pipeline execution with the same parameters already exists.
- **Request Body:**
  ```json
  {
    "repo": "https://github.com/org/repo",
    "branch": "main",
    "commit": "abcdef123456",
    "pipeline": "build-pipeline"
  }
  ```
- **Response:**
  ```json
  {
    "isDuplicate": true,
    "existingExecutionId": "12345"
  }
  ```

### **1.5. Report Retrieval API**
#### **1.5.1. Pipeline Report API**
**Endpoint:** `/api/report/pipeline/{pipelineName}`
- **Method:** `GET`
- **Description:** Fetches past executions for a pipeline.
- **Response:**
  ```json
  [
    {"executionId": "12345", "status": "SUCCESS", "commit": "abcdef"},
    {"executionId": "12346", "status": "FAILED", "commit": "ghijkl"}
  ]
  ```

#### **1.5.2. Stage Report API**
**Endpoint:** `/api/report/pipeline/{pipelineName}/stage/{stageName}`
- **Method:** `GET`
- **Description:** Fetches past executions for a specific stage in a pipeline.
- **Response:**
  ```json
  {
    "stage": "build",
    "executions": [
      {"executionId": "12345", "status": "SUCCESS"},
      {"executionId": "12346", "status": "FAILED"}
    ]
  }
  ```

#### **1.5.3. Job Report API**
**Endpoint:** `/api/report/pipeline/{pipelineName}/stage/{stageName}/job/{jobName}`
- **Method:** `GET`
- **Description:** Fetches past executions for a specific job in a pipeline stage.
- **Response:**
  ```json
  {
    "job": "compile",
    "executions": [
      {"executionId": "12345", "status": "SUCCESS", "logs": "Build successful."}
    ]
  }
  ```

---

## **2. API Models and Their Placement**

### **2.1. Request Models (common/api/request/)**
- `PipelineExecutionRequest.java`: Represents the request body for triggering pipeline execution.
- `JobExecutionRequest.java`: Represents job execution requests sent from the backend to the worker.
- `JobStatusUpdate.java`: Represents job status updates sent from the worker to the backend.
- `ReportRequest.java`: Represents the request body for fetching reports.

### **2.2. Response Models (common/api/response/)**
- `PipelineExecutionResponse.java`: Represents the response for pipeline execution requests.
- `JobExecutionResponse.java`: Represents the response when assigning jobs to workers.
- `PipelineReportResponse.java`: DTO for pipeline-level reports.
- `StageReportResponse.java`: DTO for stage-level reports.
- `JobReportResponse.java`: DTO for job-level reports.

### **2.3. API DTOs (common/dto/)**
- `PipelineDTO.java`: Data transfer object for pipeline data.
- `StageDTO.java`: Data transfer object for stage data.
- `JobDTO.java`: Data transfer object for job data.
- `PipelineReportDTO.java`: DTO for pipeline report data transfer.
- `StageReportDTO.java`: DTO for stage report data transfer.
- `JobReportDTO.java`: DTO for job report data transfer.

### **2.4. Execution Models (common/model/)**
- `PipelineExecution.java`: Represents the execution state of a pipeline.
- `StageExecution.java`: Represents the execution state of a stage.
- `JobExecution.java`: Represents the execution state of a job.

### **2.5. Enum Definitions (common/enums/)**
- `ExecutionStatus.java`: Defines execution statuses (`PENDING`, `RUNNING`, `SUCCESS`, `FAILED`).
- `ReportStatus.java`: Defines report statuses (`SUCCESS`, `FAILED`, `CANCELED`).

---

## **3. API Responsibilities in Modules**

### **3.1. Backend Module**
- **PipelineController:** Handles pipeline execution requests and retrieves status.
- **JobController:** Assigns jobs to workers and receives job status updates.
- **ReportController:** Fetches execution reports for pipelines, stages, and jobs.

### **3.2. Worker Module**
- **WorkerController:** Listens for job execution requests from the backend.
- **WorkerBackendClient:** Sends job execution status updates back to the backend.

### **3.3. CLI Module**
- **RunCommand:** Calls the backend API to start a pipeline execution.
- **CheckCommand:** Validates YAML and prints execution order.
- **ReportCommand:** Fetches reports using the backend API.

---

## **4. Conclusion**
This API design includes endpoints, models, and responsibilities across the **backend, worker, CLI, and common modules**, ensuring **modularity and scalability**. 

