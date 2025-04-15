#  Worker Module Overview & Interaction with Backend
* This document provides an overview of the Worker Module in the CI/CD system.
* **Last Update**: 2025-03-05 by Yiwen

## 📌 Worker Module Responsibilities

The **Worker Module** in the CI/CD system is responsible for:

1. **Receiving job execution requests from the backend** (via API)
2. **Resolving job dependencies before execution**
3. **Executing jobs inside Docker containers**
4. **Reporting job status updates and logs to the backend**
5. **Uploading job artifacts to the backend**
6. **Supporting worker health check & configuration retrieval**

---

## 🔹 1. Worker Module Components & Interaction with Backend

### 1️⃣ WorkerBackendClient (API Client)

- **📍 Location:** `worker.api.client.WorkerBackendClient`
- **🛠 Role:** Facilitates HTTP communication with the backend via `RestTemplate`.

| **Method**                                            | **Backend API Endpoint**         | **Purpose**                         |
| ----------------------------------------------------- | -------------------------------- | ----------------------------------- |
| `getJobExecution(UUID jobId)`                         | `/api/jobs/{jobId}`              | Fetch job execution details         |
| `getJobDependencies(UUID jobId)`                      | `/api/jobs/{jobId}/dependencies` | Fetch job dependencies              |
| `getJobStatus(UUID jobId)`                            | `/api/jobs/{jobId}/status`       | Fetch job execution status          |
| `updateJobStatus(UUID, ExecutionStatus, String)`      | `/api/job/status`                | Reports job execution status & logs |
| `uploadArtifacts(UUID jobId, List<String> artifacts)` | `/api/job/artifact/upload`       | Uploads job artifacts to backend    |

### 2️⃣ Worker API Controllers (Exposes REST APIs for backend to trigger jobs)

#### 🔹 JobExecutionController

- **📍 Location:** `worker.api.controller.JobExecutionController`
- **🛠 Role:** Handles job execution requests from the backend.

| **Endpoint**       | **Method** | **Purpose**                                                  |
| ------------------ | ---------- | ------------------------------------------------------------ |
| `/api/job/execute` | `POST`     | Receives job execution request from backend & triggers execution |

#### 🔹 WorkerConfigController

- **📍 Location:** `worker.api.controller.WorkerConfigController`
- **🛠 Role:** Exposes worker configuration settings.

| **Endpoint**         | **Method** | **Purpose**                                               |
| -------------------- | ---------- | --------------------------------------------------------- |
| `/api/worker/config` | `GET`      | Retrieves worker configuration (max retries, retry delay) |

#### 🔹 WorkerHealthController

- **📍 Location:** `worker.api.controller.WorkerHealthController`
- **🛠 Role:** Provides a health check for the worker.

| **Endpoint**         | **Method** | **Purpose**                 |
| -------------------- | ---------- | --------------------------- |
| `/api/worker/health` | `GET`      | Checks if worker is running |

### 3️⃣ Worker Execution Logic

#### 🔹 DockerExecutor

- **📍 Location:** `worker.execution.DockerExecutor`
- **🛠 Role:** Runs jobs inside Docker containers.

| **Method**                              | **Purpose**                                         |
| --------------------------------------- | --------------------------------------------------- |
| `execute(JobExecutionDTO jobExecution)` | Runs a job in Docker & returns its execution status |

#### 🔹 JobRunner

- **📍 Location:** `worker.execution.JobRunner`
- **🛠 Role:** Manages job execution & dependency resolution.

| **Method**                                         | **Purpose**                                           |
| -------------------------------------------------- | ----------------------------------------------------- |
| `runJob(JobExecutionDTO job)`                      | Runs a job, resolves dependencies, and updates status |
| `checkAndWaitForDependencies(JobExecutionDTO job)` | Ensures dependencies are completed before execution   |

### 4️⃣ Worker Services

#### 🔹 PipelineExecutionWorkerService

- **📍 Location:** `worker.service.PipelineExecutionWorkerService`
- **🛠 Role:** Manages pipeline execution in the worker.

| **Method**                                  | **Purpose**                                   |
| ------------------------------------------- | --------------------------------------------- |
| `executePipeline(UUID pipelineExecutionId)` | Fetches jobs & executes them in correct order |
| `executeJob(JobExecutionDTO job)`           | Directly executes a single job                |

#### 🔹 WorkerCommunicationService

- **📍 Location:** `worker.service.WorkerCommunicationService`
- **🛠 Role:** Manages worker-to-backend communication.

| **Method**                                                   | **Purpose**                                    |
| ------------------------------------------------------------ | ---------------------------------------------- |
| `getJobDependencies(UUID jobId)`                             | Fetches job dependencies from backend          |
| `getJobStatus(UUID jobId)`                                   | Fetches job status from backend                |
| `reportJobStatus(UUID jobExecutionId, ExecutionStatus status, String logs)` | Reports job execution status & logs to backend |

### 5️⃣ WorkerApp (Main Entry Point)

- **📍 Location:** `worker.WorkerApp`
- **🛠 Role:** Starts the worker service.

```java
@SpringBootApplication
public class WorkerApp {
  public static void main(String[] args) {
    SpringApplication.run(WorkerApp.class, args);
  }
}
```

---

## 🔹 2. Execution Flow

### **1️⃣ Backend → Worker (Job Execution Request)**

1. Backend sends `POST /api/job/execute` → **Worker receives the request**.
2. `JobExecutionController.executeJob()` → Calls `PipelineExecutionWorkerService.executeJob()`.
3. `PipelineExecutionWorkerService.executeJob()` → Calls `JobRunner.runJob()`.

### **2️⃣ Worker Resolves Dependencies & Executes Job**

4. `JobRunner.runJob()`:
    - Calls `WorkerCommunicationService.getJobDependencies()`.
    - If dependencies are pending, waits until they complete.
    - Calls `DockerExecutor.execute(job)`, running the job in Docker.

### **3️⃣ Worker Reports Job Status**

5. After execution, `JobRunner`:
    - Calls `WorkerCommunicationService.reportJobStatus()`.
    - `WorkerCommunicationService` calls `WorkerBackendClient.updateJobStatus()`, sending logs and status to backend.

### **4️⃣ Worker Uploads Artifacts (If Any)**

6. If the job produces artifacts:
    - `WorkerBackendClient.uploadArtifacts()` is called.

### **5️⃣ Backend Updates Execution Status**

7. Backend receives **job execution status updates**.
8. If all jobs in a stage **succeed**, backend triggers the **next stage**.

---

## 🔹 3. Summary: How Worker Module Connects to Backend

✅ **Backend → Worker:**

- Backend calls **Worker's API** to **execute jobs** (`POST /api/job/execute`).
- Worker **fetches job dependencies** from the backend.

✅ **Worker → Backend:**

- Worker **updates job status** (`POST /api/job/status`).
- Worker **uploads artifacts** (`POST /api/job/artifact/upload`).
- Worker **fetches execution state** (dependencies & job status).