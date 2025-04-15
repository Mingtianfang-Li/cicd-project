# **Job Execution in Worker Module**

* **Author**: Yiwen Wang
* **Version**: 1.0
* **Last Updated**: Mar 4, 2025

## **Overview**
Each job in the CI/CD pipeline runs inside a **Docker container** and must ensure that its dependencies are met before execution. This document outlines the job execution process, dependency handling, and Docker-based execution design in the worker module.

---

## **1. Correct Execution Flow**

### **1️⃣ CLI Receives YAML File**
- User provides a pipeline YAML file via the CLI.
- The CLI validates and parses the file.
- If valid, the CLI sends the parsed pipeline configuration to the backend.

### **2️⃣ Backend Receives Pipeline Execution Request**
- The backend does **not** fetch pipeline metadata from the database.
- Instead, the backend receives all execution details from the CLI request.

### **3️⃣ Backend Starts Execution**
- It creates a new pipeline execution record in the database.
- It identifies and schedules the first stage.
- The `StageExecutionService` handles stage execution and triggers jobs.
- The `JobScheduler` assigns jobs to the worker (which resolves dependencies).
- The backend tracks execution history in the database.

### **4️⃣ Worker Executes Jobs & Reports Back**
- The worker runs jobs, handles dependencies, and sends results back.
- Backend updates execution status after receiving worker reports.
- If all jobs in a stage are completed successfully, backend schedules the next stage.
- If a job fails and is not marked as `allow_failure`, the pipeline execution fails.

### **5️⃣ Backend Finalizes Execution**
- Once all stages are complete, backend updates the final pipeline execution status.
- Execution data is stored for reporting and retrieval via API.

---

## **2. Job Execution Requirements**
- Each job must run **inside a Docker container**.
- Jobs must **wait for dependencies** to complete before execution.
- Jobs that have **no dependencies** or have met dependencies **should run in parallel**.
- If a job **fails**, it must respect the `allow_failure` flag:
    - If `allow_failure = false`, the pipeline **stops immediately**.
    - If `allow_failure = true`, the pipeline **continues execution**.
- Jobs must use the **Docker image** specified in the configuration file.

---

## **3. Job Dependency Handling**
Each job can have dependencies on other jobs within the same stage. The system must:
1. **Parse dependencies from the configuration file.**
2. **Ensure dependencies have completed successfully** before starting a job.
3. **Detect cyclic dependencies** and prevent execution if cycles exist.

### **3.1. Dependency Resolution Workflow**
1. Build a **dependency graph** from the pipeline configuration.
2. Use **Topological Sorting** (e.g., Kahn’s Algorithm) to determine execution order.
3. Check for **cycles** in job dependencies.
4. Schedule jobs that **have no unmet dependencies** for execution.

---

## **4. Docker-Based Job Execution**
Each job runs inside a Docker container using the **Docker image** specified in the pipeline configuration.

### **4.1. Determining the Docker Image for a Job**
A job's Docker image is determined as follows:
- If a **job specifies its own image**, use that.
- Otherwise, use the **global Docker image** defined in the pipeline configuration.
- If neither is specified, default to `docker.io/library/ubuntu:latest`.

### **4.2. Executing Jobs in Docker Containers**
Each job runs inside a **dedicated container**, which:
- Pulls the required Docker image (if not already available).
- Mounts the workspace directory to share files.
- Executes the job’s command(s).
- Captures logs and execution status.

### **4.3. Docker Execution Flow**
1. **Pull Docker Image** (if not already available):
   ```sh
   docker pull <image_name>:<tag>
   ```
2. **Run Job in Container:**
   ```sh
   docker run --rm -v $(pwd):/workspace -w /workspace <image_name>:<tag> sh -c "<command>"
   ```
3. **Capture Output & Logs**.
4. **Return Execution Status**:
    - `0` → Success
    - Non-zero → Failure

---

## **5. Error Handling & Logging**
### **5.1. Dependency Cycle Detection**
If a cycle is detected in job dependencies, execution must **fail immediately** with an error message.

### **5.2. Logging Job Execution**
- Each job logs **stdout and stderr** output.
- Logs are stored separately for debugging.
- Job execution status is reported back to the pipeline manager.

---

## **6. Conclusion**
This design ensures that:
✅ Jobs run in **Docker containers**.
✅ Dependencies are **resolved and enforced** before execution.
✅ Execution order is **calculated dynamically**.
✅ Job failures **respect `allow_failure` settings**.
✅ Logs and execution results are **stored for reporting**.


