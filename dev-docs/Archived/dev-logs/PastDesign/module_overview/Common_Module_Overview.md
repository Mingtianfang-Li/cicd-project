# **Common Module Overview**

- **Version:** 1.0
- **Updated:** Feb 28, 2025
- **Author:** Yiwen Wang

## **1. Introduction**
The `common` module serves as the **core shared component** of the CI/CD pipelineEntity system. It contains **data models and API contracts** used by other modules, including:
- **`backend`** → Orchestration and execution control.
- **`worker`** → Actual execution of jobEntities.
- **`cli`** → User interaction and command execution.

The `common` module ensures **consistency** across different components and enables **modular development**.

---

## **2. Overview of Key Components**
### **🔹 Model (`common.model`)**
This package contains **data structures** representing core CI/CD concepts:

| **Class**               | **Description** |
|-------------------------|---------------|
| `PipelineDefinition`    | Defines a pipelineEntity configuration (stageEntities, global variables). |
| `StageDefinition`       | Defines a single stageEntity within a pipelineEntity, containing jobEntities. |
| `JobDefinition`         | Represents a **static jobEntity configuration** (image, script, dependencies). |
| `PipelineExecution`     | Tracks an **active** pipelineEntity execution. |
| `StageExecution`        | Tracks an **active** stageEntity execution. |
| `JobExecution`         | Tracks an **active** jobEntity execution (status, timestamps, etc.). |
| `ExecutionState`        | **Unified enum** for execution statuses of pipelines, stageEntities, and jobEntities. |
| `JobStatusUpdate`       | Used to update the status of a running jobEntity. |

**🔹 Execution State (`ExecutionState` Enum)**
- Replaces multiple status enums across the system.
- Ensures **consistent status tracking** across pipelines, stageEntities, and jobEntities.
- Possible values:
    - `PENDING`
    - `RUNNING`
    - `SUCCESS`
    - `FAILED`
    - `CANCELED`
    - `UNKNOWN`

### **🔹 API (`common.api`)**
This package contains **API request/response objects** that facilitate communication between different components.

| **Class**                    | **Purpose** |
|------------------------------|------------|
| `RunPipelineRequest`         | Sent to the backend to start a pipelineEntity execution. |
| `PipelineCheckResponse`      | Response from a pipelineEntity validation check. |
| `JobRequest`                 | Represents a request to **run a jobEntity** (Docker image, commands, etc.). |
| `JobResponse`                | Contains execution results of a jobEntity (exit code, logs, artifacts). |
| `JobStatusUpdate`            | Used to **update jobEntity status** (`ExecutionState`). |
| `WorkerRegistrationRequest`  | Sent when a new worker registers to the system. |
| `WorkerRegistrationResponse` | Response confirming worker registration success/failure. |

---

## **3. Execution Flow & Responsibilities**
### **🔹 How the Common Module Works in CI/CD Execution**

1️⃣ **Pipeline Execution**
- The backend **loads** `PipelineDefinition` from the repository YAML.
- `PipelineExecution` is created to **track execution progress**.
- `StageExecution` and `JobExecution` objects are initialized for each stageEntity/jobEntity.

2️⃣ **Job Execution**
- A `JobRequest` is **sent to a worker**.
- The worker **executes the jobEntity** using the provided Docker image and commands.
- The worker sends a `JobStatusUpdate` with an `ExecutionState` as the jobEntity progresses.
- Once completed, the worker sends a `JobResponse` back to the backend.

3️⃣ **Status Updates & Completion**
- `PipelineExecution` and `StageExecution` **update their statuses** based on jobEntity results.
- If all jobEntities complete successfully, the pipelineEntity transitions to `SUCCESS`.
- If a jobEntity fails (and `allowFailure=false`), the pipelineEntity transitions to `FAILED`.

---

## **4. Key Design Principles**
✅ **Separation of Concerns** → API models separate from execution models.  
✅ **Standardized Communication** → API files enforce consistency across modules.  
✅ **Scalability** → The design allows adding more jobEntity types, execution strategies, and reporting mechanisms.  
✅ **Unified Execution Tracking** → The `ExecutionState` enum standardizes status handling across **pipelines, stageEntities, and jobEntities**.  
✅ **Flexibility** → Job execution is **decoupled** from pipelineEntity orchestration, allowing for future **distributed execution**.

---

## **5. Summary**
- The **common module** is the **foundation** of the CI/CD system.
- It defines **data structures** for pipelines, stageEntities, and jobEntities.
- It provides **API contracts** for communication between the backend and workers.
- It ensures **consistency and reusability** across the entire CI/CD system.
- The introduction of `ExecutionState` **unifies status tracking** across all execution components.

🚀 **With this structure, the system supports efficient, modular, and scalable CI/CD execution.**



