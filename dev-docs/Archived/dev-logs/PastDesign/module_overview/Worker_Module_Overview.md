# **Worker Module Overview**

- **Version:** 1.0
- **Last Updated:** Feb 28, 2025
- **Author:** Yiwen Wang

## **1. Introduction**
The `worker` module is responsible for **executing jobEntities** in the CI/CD pipelineEntity system. It interacts with the `backend` module and executes jobEntities using **Docker containers**.

This module **receives jobEntities** from the backend, **executes them** in an isolated environment, and **reports status updates** back to the backend.

## **2. Worker Module Components**
### **🔹 API Layer (`worker.api`)**
| **Class** | **Purpose** |
|-----------|------------|
| `WorkerController` | Handles jobEntity execution requests from the backend. |
| **Fixes:** Uses `JobRequest` instead of `JobExecution`, improved logging and validation. |

### **🔹 Client (`worker.client`)**
| **Class** | **Purpose** |
|-----------|------------|
| `BackendClient` | Sends jobEntity execution status updates to the backend. |
| **Fixes:** Uses `common.api.JobStatusUpdate`, implements **retry logic** for API failures. |

### **🔹 Execution Layer (`worker.executor`)**
| **Class** | **Purpose** |
|-----------|------------|
| `DockerManager` | Manages Docker containers for executing jobEntities. |
| **Fixes:** Validates inputs, improves method consistency, and error handling. |
| `DockerRunner` | Runs jobEntities inside Docker containers using system processes. |
| **Fixes:** Added exception handling to prevent silent failures. |
| `JobExecutor` | Manages jobEntity execution lifecycle (start, update status, cleanup). |
| **Fixes:** Uses `ExecutionState` enum, configurable logging, and better error handling. |

## **3. Execution Flow & Responsibilities**
### **🔹 How the Worker Module Works in CI/CD Execution**

1️⃣ **Receives a Job Execution Request**
- `WorkerController` receives a `JobRequest` from the backend.
- Converts `JobRequest` to `JobExecution` and passes it to `JobExecutor`.

2️⃣ **Executes the Job**
- `JobExecutor` starts the jobEntity execution using `DockerManager`.
- `DockerManager` pulls the image, starts a container, and executes commands.
- Worker **monitors** the jobEntity execution.

3️⃣ **Updates Job Status**
- **While running**, the worker sends a `JobStatusUpdate` (`RUNNING`).
- **On completion**, it sends `JobStatusUpdate` (`SUCCESS` or `FAILED`).
- `BackendClient` ensures these updates reach the backend (with retry logic).

4️⃣ **Cleans Up Execution**
- After execution, `DockerManager` removes the container.
- `JobExecutor` logs the execution results.

## **4. Integration with Common Module**
The **worker module relies on the common module** for standardization:

✅ **Job Status Handling** → Uses `ExecutionState` enum from `common.model` instead of raw strings.  
✅ **API Contracts** → Uses `JobRequest` and `JobStatusUpdate` from `common.api`.  
✅ **Execution Tracking** → Uses `JobExecution` from `common.model.execution`.

## **5. Key Design Principles**
✅ **Modular Execution** → The worker is fully independent and scalable.  
✅ **Isolated Execution** → Jobs run inside Docker containers for security.  
✅ **Resilient Communication** → Implements **retry logic** for backend communication failures.  
✅ **Logging & Monitoring** → Execution results are logged for auditing.

## **6. Summary**
- The **worker module** executes jobEntities using **Docker containers**.
- It receives jobEntities from the backend, executes them, and sends **status updates**.
- It integrates with the **common module** for execution tracking and API communication.
- 🚀 **With these improvements, the worker module is robust, modular, and fault-tolerant.**

