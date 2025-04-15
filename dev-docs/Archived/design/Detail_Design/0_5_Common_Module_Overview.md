# **Common Module Overview**

## **Author**: Yiwen Wang
## **Version**: 1.0
## **Last Updated**: Mar 5, 2025

---

## **Overview**

The **common module** serves as the central hub for **data transfer objects (DTOs), models, enums, request/response classes, and logging utilities** that facilitate communication between different modules (CLI, Backend, Worker) in the CI/CD system. This ensures **consistency, reusability, and maintainability** across the entire project.

---

## **1. Key Components**

### **1.1 API Request DTOs (`api.request`)**
Defines request objects sent between modules, mainly from CLI to Backend and Worker to Backend.

| **File**                     | **Description** |
|------------------------------|------------------------------------------------|
| `ArtifactUploadRequest`      | Worker uploads artifacts to backend. |
| `JobExecutionRequest`        | Backend sends job execution details to worker. |
| `JobStatusUpdate`            | Worker reports job execution status to backend. |
| `PipelineExecutionRequest`   | CLI triggers a pipeline execution request. |
| `ReportRequest`              | Fetch reports at pipeline, stage, or job level. |

### **1.2 API Response DTOs (`api.response`)**
Defines response objects returned from the backend to the CLI or worker.

| **File**                     | **Description** |
|------------------------------|------------------------------------------------|
| `JobExecutionResponse`       | Returns job execution ID and status. |
| `JobReportResponse`          | Returns past executions for a job. |
| `PipelineExecutionResponse`  | Returns execution ID and status. |
| `PipelineReportResponse`     | Returns execution summary for a pipeline. |
| `StageReportResponse`        | Returns past executions for a stage. |

### **1.3 Execution DTOs (`dto`)**
Defines structured objects for pipeline, stage, and job execution metadata.

| **File**                     | **Description** |
|------------------------------|------------------------------------------------|
| `JobDTO`                     | Defines job metadata (e.g., script, dependencies). |
| `JobExecutionDTO`            | Defines job execution metadata (e.g., execution status, logs). |
| `PipelineDTO`                | Defines pipeline metadata. |
| `PipelineExecutionDTO`       | Defines pipeline execution metadata. |
| `StageDTO`                   | Defines stage metadata. |
| `StageExecutionDTO`          | Defines stage execution metadata. |

### **1.4 Execution Models (`model`)**
Defines execution logic for pipelines, stages, and jobs.

| **File**                     | **Description** |
|------------------------------|------------------------------------------------|
| `ExecutionLog`               | Stores logs for jobs, stages, and pipelines. |
| `Job`                        | Represents a job definition in a pipeline. |
| `JobExecution`               | Tracks execution details of a job. |
| `Pipeline`                   | Represents a CI/CD pipeline definition. |
| `PipelineExecution`          | Tracks pipeline execution details. |
| `Stage`                      | Represents a stage definition in a pipeline. |
| `StageExecution`             | Tracks execution details of a stage. |

### **1.5 Enums (`enums`)**
Defines predefined execution states.

| **File**            | **Description** |
|---------------------|------------------------------------------------|
| `ExecutionStatus`   | Enum for execution progress (`PENDING`, `RUNNING`, `SUCCESS`, `FAILED`, `CANCELED`). |

### **1.6 Logging (`logging`)**
Provides centralized logging utilities for debugging and tracking execution status.

| **File**          | **Description** |
|------------------|------------------------------------------------|
| `CicdLogger`    | Centralized logging system for CI/CD system. |

---

## **2. Purpose & Interaction Between Modules**

### **2.1 How It Works**
The **common module** ensures smooth interaction between different components:
- **CLI → Backend**: Sends `PipelineExecutionRequest` to start execution.
- **Backend → Worker**: Sends `JobExecutionRequest` to assign jobs.
- **Worker → Backend**: Reports execution status using `JobStatusUpdate`.
- **Backend → CLI**: Fetches execution summaries via `PipelineReportResponse` and `JobReportResponse`.

### **2.2 Why This Design?**
✅ **Reusability** – Common models eliminate duplication across modules.  
✅ **Consistency** – Standardized request/response format ensures structured data exchange.  
✅ **Scalability** – Additional pipeline/job types can be added without breaking compatibility.

---

## **3. Next Steps**
- **Ensure proper integration with the Backend and Worker modules**.
- **Optimize queries for efficient execution status retrieval**.
- **Implement advanced logging for better debugging insights**.

---

## **4. Conclusion**
The **common module** serves as the backbone of the CI/CD system by standardizing execution **requests, responses, DTOs, and logs**. This design ensures **smooth interaction** between the **CLI, Backend, and Worker** while maintaining **scalability and maintainability**.
