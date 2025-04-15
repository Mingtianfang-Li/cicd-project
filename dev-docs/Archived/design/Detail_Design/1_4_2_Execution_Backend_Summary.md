# 🚀 Execution Logic Breakdown
**Author**: Yiwen Wang
**Version**: 1.0
**Last Updated**: Mar 11, 2025

**CICD pipeline execution** is structured in **three levels**:
1. **`PipelineExecutionService`** – Manages the overall pipeline execution.
2. **`StageExecutionService`** – Handles execution of **stages** in sequence.
3. **`JobExecutionService`** – Sends jobs to the **worker** and processes job results.

---

## **🔍 Step-by-Step Execution Flow**

### **1️⃣ Pipeline Execution (`PipelineExecutionService`)**
- The **pipeline starts** when `startPipelineExecution()` is called.
- It **reads & validates the YAML** configuration for execution.
- A **PipelineExecutionEntity** is created in the database.
- It calls **`executeStagesSequentially(pipelineExecutionId)`** to begin execution.

---

### **2️⃣ Stage Execution (`StageExecutionService`)**
- **Each stage runs sequentially** within the pipeline.
- **Stage execution starts:**
    - The **stage status** is updated to `RUNNING`.
    - Calls **`executeJobsByDependencies(stageExecutionId)`** to begin job execution.

---

### **3️⃣ Job Execution (`JobExecutionService`)**
- The `executeJobsByDependencies()` method ensures **dependency-based execution**:
    - Jobs **with no dependencies** are executed **immediately**.
    - Jobs **with dependencies** wait until their dependencies finish.
    - Jobs are executed in **parallel** whenever possible.
- **Job Execution Flow:**
    1. **Start a job:**
        - Calls **`startJobExecution(jobId)`**.
        - Marks job as **RUNNING**.
        - Sends the job **to the worker** via `sendJobToWorker(jobId)`.
    2. **Worker handles execution and sends a status update.**
    3. **Job status is updated** (SUCCESS / FAILED).
    4. **Event Trigger:**
        - If **job succeeds**, it **triggers `JobCompletedEvent`**.
        - If **job fails**, handling depends on the `allowFailure` flag:
            - If `allowFailure == true`, execution **continues**.
            - If `allowFailure == false`, remaining jobs **cancel** and **stage fails**.

---

### **4️⃣ Handling Job Failures (`onJobCompleted` in `StageExecutionService`)**
- When a **job completes**, the event `onJobCompleted(JobCompletedEvent event)` fires.
- **It checks all jobs:**
    - If **all jobs are `SUCCESS`**, the stage is **finalized as SUCCESS**.
    - If **some jobs failed**, it checks `allowFailure`:
        - If all failed jobs **allow failure**, stage execution **continues**.
        - If **a job failed & does NOT allow failure**, the **remaining jobs cancel**, and the stage **fails**.

---

### **5️⃣ Stage Completion (`onStageCompleted` in `PipelineExecutionService`)**
- When **all jobs in a stage are successful**, it **publishes `StageCompletedEvent`**.
- If **all stages complete successfully**, the **pipeline execution is finalized**.

---

## **🔹 Parallel Execution Considerations**
✅ **Can jobs run in parallel?**
- **Yes**, jobs with **no dependencies** start **immediately**.
- Jobs **with dependencies** wait until their dependencies are completed.
- Execution follows a **dependency graph**.

✅ **What happens if a job has no dependencies?**
- It is executed **immediately** in `executeJobsByDependencies()`.
- Runs in **parallel** with other independent jobs.

✅ **What if a job fails?**
- If **failure is allowed**, execution **continues**.
- If **failure is NOT allowed**, all remaining jobs are **canceled**, and the stage **fails**.

---

## **🎯 Final Takeaways**
✔ **Jobs run in parallel** when possible.  
✔ **Dependency-based execution** ensures correct order.  
✔ **Failure handling logic** respects `allowFailure` per job.  
✔ **Event-based execution** automatically manages progress.

