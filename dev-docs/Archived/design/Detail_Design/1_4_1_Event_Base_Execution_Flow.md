# 🔹 Event-Based Execution Flow

**Author**: Yiwen Wang
**Version**: 1.0
**Last Updated**: Mar 11, 2025

## **PipelineExecutionService starts a pipeline**
1. Reads & validates YAML.
2. Saves pipeline execution to the DB.
3. Calls `executeStagesSequentially()`, triggering **StageExecutionService**.

## **StageExecutionService starts executing stages**
1. Marks the stage as **RUNNING**.
2. Calls `executeJobsByDependencies()`, triggering **JobExecutionService**.

## **JobExecutionService sends jobs to workers**
1. Calls `sendJobToWorker()`, sending job requests.
2. **Worker executes the job** and reports status back.
3. If successful, publishes **JobCompletedEvent**.

## **StageExecutionService listens for JobCompletedEvent**
1. When **all jobs** in a stage complete successfully, it publishes a **StageCompletedEvent**.

## **PipelineExecutionService listens for StageCompletedEvent**
1. When **all stages** in the pipeline are done, it **finalizes execution**.

---

# 🚀 Why Use Event-Based Execution?
### **1️⃣ No More Polling (Efficient Resource Utilization)**
- Instead of actively checking status, services **wait for events**.
- Reduces **CPU & DB overhead**.

### **2️⃣ Improved Scalability & Concurrency**
- Events allow **asynchronous** execution.
- Multiple jobs & stages can **run in parallel**.

### **3️⃣ Decoupled Components (Modular Design)**
- **Job, Stage, and Pipeline services are independent**.
- Can **replace or modify** individual services without breaking others.

### **4️⃣ Better Failure Handling & Recovery**
- **Event listeners can retry on failure**.
- Allows **timeouts, cancellations, and re-execution** of failed jobs.

### **5️⃣ Easier Extensibility**
- New **features** (e.g., notifications, logging) can **hook into events**.
- Adding new execution logic doesn’t require modifying core execution flow.

By using **event-driven execution**, the pipeline becomes more **scalable, maintainable, and efficient**.

