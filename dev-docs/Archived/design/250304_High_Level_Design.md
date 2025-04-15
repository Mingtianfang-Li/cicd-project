# **High-Level Design Document**

* **Project Name**: Custom CI/CD System
* **Author**: Yiwen Wang
* **Version**: 2.0
* **Last Updated**: Mar 4, 2025

## **1. Overview**
This document provides a high-level architectural design for the Custom CI/CD system, focusing on core components, execution flow, and API interactions.

## **2. System Architecture**

### **2.1. High-Level Components**
The system consists of four primary components:

1. **CLI Module** – User interface for executing pipelines, validating configurations, and retrieving reports.
2. **Backend Module** – Central service handling pipeline execution, job scheduling, and storing execution history.
3. **Worker Module** – Executes jobs inside Docker containers, manages job dependencies, and reports execution results.
4. **Common Module** – Shared libraries, configurations, and models used across all components.

### **2.2. Execution Flow**
1. **CLI Initiation**:
    - Users execute a command to run a pipeline (e.g., `xx run --pipeline test-pipeline`).
    - CLI parses YAML configuration and validates dependencies.
    - The validated pipeline is sent to the backend.

2. **Backend Processing**:
    - Stores execution details and assigns jobs to workers.
    - Manages execution states (`Pending`, `Running`, `Success`, `Failed`, `Canceled`).
    - Logs execution progress and handles failure conditions.

3. **Worker Execution**:
    - Workers execute jobs in isolated Docker containers.
    - Reports job statuses and logs back to the backend.
    - Ensures jobs adhere to dependency constraints.

4. **Result Collection & Reporting**:
    - Execution results are stored in the database.
    - Users retrieve reports via CLI (`xx report --pipeline test-pipeline`).
    - Reports include pipeline status, job logs, and execution details.

## **3. Key Features**

| Feature | Description |
|---------|-------------|
| **Local & Remote Execution** | Supports running CI/CD pipelines both locally and in a centralized environment. |
| **Dependency Management** | Jobs are scheduled based on dependencies; jobs without dependencies run in parallel. |
| **Docker-based Execution** | Each job runs inside an isolated Docker container using the specified image. |
| **Pipeline Execution Control** | Execution stops immediately upon failure unless the job is marked `allow_failure`. |
| **Duplicate Execution Prevention** | Identifies duplicate requests and prevents redundant execution. |
| **Execution Logging** | Logs are stored separately and retrievable via a dedicated API. |
| **Artifact Handling** | Supports artifact uploads based on job specifications. |

## **4. API Overview**

| Endpoint | Method | Description |
|-----------|--------|-------------|
| `/api/pipeline/execute` | `POST` | Start a pipeline execution. |
| `/api/pipeline/status/{executionId}` | `GET` | Retrieve execution status. |
| `/api/job/execute` | `POST` | Assign job execution to a worker. |
| `/api/job/status` | `POST` | Worker updates job execution status. |
| `/api/report/pipeline/{pipelineName}` | `GET` | Retrieve past pipeline executions. |
| `/api/artifact/upload` | `POST` | Upload job artifacts. |
| `/api/job/cancel/{jobId}` | `POST` | Cancel a running job. |

## **5. Technologies & Tools**
- **Language**: Java (Spring Boot for backend, Gradle for build management)
- **Database**: PostgreSQL for execution history
- **Containerization**: Docker for job execution
- **Version Control**: Git for repository management
- **Testing**: JUnit & Mockito for unit tests; Postman for API testing

## **6. Conclusion**
This high-level design outlines the architectural structure and major functionalities of the CI/CD system. It ensures modularity, scalability, and efficient execution of pipelines while enforcing constraints and tracking execution states.