# **Low-Level Design Document**

* **Project Name**: Custom CI/CD System
* **Author**: Yiwen Wang
* **Version**: 2.0
* **Last Updated**: Mar 4, 2025

## **1. Overview**
This document provides a detailed design for the Custom CI/CD system, covering database schema, module interactions, and detailed API logic.

## **2. Database Schema**

### **2.1. Tables & Relationships**

#### **Pipeline Execution Table**
| Column         | Type       | Description |
|---------------|-----------|-------------|
| `id`          | UUID      | Unique identifier for execution. |
| `pipeline_name` | String   | Name of the executed pipeline. |
| `repo`        | String    | Repository URL. |
| `branch`      | String    | Branch used for execution. |
| `commit`      | String    | Git commit hash. |
| `status`      | ENUM      | Execution state (`PENDING`, `RUNNING`, `SUCCESS`, `FAILED`). |
| `start_time`  | Timestamp | Time execution started. |
| `end_time`    | Timestamp | Time execution completed. |

#### **Job Execution Table**
| Column         | Type       | Description |
|---------------|-----------|-------------|
| `id`          | UUID      | Unique identifier for job execution. |
| `pipeline_execution_id` | UUID | References Pipeline Execution. |
| `stage`       | String    | Name of the stage. |
| `job_name`    | String    | Name of the job. |
| `docker_image` | String   | Docker image used for execution. |
| `status`      | ENUM      | Job execution status. |
| `logs`        | TEXT      | Execution logs. |
| `start_time`  | Timestamp | Time job started. |
| `end_time`    | Timestamp | Time job completed. |

#### **Artifact Table**
| Column         | Type       | Description |
|---------------|-----------|-------------|
| `id`          | UUID      | Unique identifier for the artifact. |
| `job_execution_id` | UUID | References Job Execution. |
| `file_path`   | String    | Path to the stored artifact. |
| `uploaded_at` | Timestamp | Upload timestamp. |

---

## **3. Module Design**

### **3.1. Backend Services**

#### **PipelineExecutionService**
- Starts new pipeline execution.
- Tracks execution status in the database.
- Ensures proper stage execution order.

#### **JobExecutionService**
- Assigns jobs to available workers.
- Checks job dependencies before execution.
- Updates job status based on worker reports.

#### **ArtifactService**
- Handles artifact uploads and storage.
- Retrieves artifacts for reporting and debugging.

### **3.2. Worker Module**

#### **JobExecutor**
- Runs jobs inside Docker containers.
- Captures execution logs.
- Reports execution status back to the backend.

#### **DockerManager**
- Manages container lifecycle (start, stop, cleanup).
- Ensures execution isolation.

### **3.3. CLI Module**

#### **RunCommand**
- Parses pipeline YAML.
- Sends execution request to the backend.
- Displays real-time execution progress.

#### **CheckCommand**
- Validates pipeline configuration for correctness.

#### **ReportCommand**
- Fetches execution reports from the backend.

---

## **4. Execution Flow**

1. **User runs a pipeline via CLI**.
2. **CLI validates and sends request to the backend**.
3. **Backend starts pipeline execution and schedules jobs**.
4. **Worker executes jobs in Docker containers**.
5. **Workers report job statuses back to the backend**.
6. **Backend finalizes execution and updates reports**.

---

## **5. API Interactions**

| Endpoint | Method | Description |
|-----------|--------|-------------|
| `/api/pipeline/execute` | `POST` | Start a pipeline execution. |
| `/api/pipeline/status/{executionId}` | `GET` | Retrieve execution status. |
| `/api/job/execute` | `POST` | Assign job execution to a worker. |
| `/api/job/status` | `POST` | Worker updates job execution status. |
| `/api/artifact/upload` | `POST` | Upload job artifacts. |
| `/api/job/cancel/{jobId}` | `POST` | Cancel a running job. |

---

## **6. Error Handling**

- **Invalid Configuration**: Return meaningful validation messages.
- **Job Dependency Issues**: Halt execution if dependencies are not met.
- **Worker Failures**: Retry mechanism in case of worker failure.
- **Duplicate Execution Prevention**: Ensure the same execution is not started twice.

---

## **7. Conclusion**
This low-level design outlines the implementation details of the CI/CD system, covering database schema, services, worker execution, CLI interactions, and error handling to ensure smooth execution of pipelines.

