# Full Execution Flow (Backend to Worker and Back)
* **Last Update**: 2025-03-10 by Yiwen
## 1. **Overview**
The system follows a structured execution flow ensuring that the backend and worker communicate correctly.

## 2. **Execution Sequence**

### **1. Backend Starts the Pipeline Execution**
- The backend triggers the **first stage execution** via `StageExecutionService.executeFirstStage()`.

### **2. Backend Assigns Jobs to Workers**
- The backend finds all jobs for a stage and **sends them to a worker** using `WorkerClient.notifyWorkerJobAssigned()`.

### **3. Worker Receives Jobs and Handles Execution**
- The worker **receives job execution requests** via `JobExecutionController.executeJob()`.
- The worker **resolves dependencies** and **executes the job inside Docker** using `JobRunner.runJob()`.

### **4. Worker Reports Job Completion to Backend**
- The worker **updates job execution status** using `WorkerBackendClient.updateJobStatus()`.
- The backend **updates the database** through `JobExecutionService.updateJobExecutionStatus()`.

### **5. Backend Checks Stage Completion**
- If **all jobs finish successfully**, the **stage is marked as complete** in `StageExecutionService.finalizeStageExecution()`.

### **6. Backend Executes the Next Stage**
- If **more stages exist**, the backend **starts the next stage** via `StageExecutionService.executeNextStage()`.

### **7. Backend Completes the Pipeline**
- If **all stages complete**, the backend **finalizes the pipeline** using `PipelineExecutionService.finalizePipelineExecution()`.

✅ **Backend and Worker communicate correctly** throughout this flow.

---

## 3. **Backend Assigning Jobs to Worker**
- **Backend fetches all jobs for a stage** (`JobExecutionService.executeJobsForStage()`).
- **Jobs are sent to workers** (`WorkerClient.notifyWorkerJobAssigned()`).
- **Worker starts job execution** (`WorkerBackendClient.getJobExecution()`).

### **Example Flow:**
#### **Backend Calls Worker**
```java
workerClient.notifyWorkerJobAssigned(job.getId());
```
#### **Worker API Receives Job**
```java
@PostMapping("/execute")
public ResponseEntity<?> executeJob(@RequestBody JobExecutionDTO job) {
  pipelineExecutionWorkerService.executeJob(job);
}
```
---

## 4. **Worker Processing Job and Reporting Back**
- **Worker retrieves dependencies** (`WorkerBackendClient.getJobDependencies()`).
- **Worker executes job in Docker** (`DockerExecutor.execute()`).
- **Worker reports job status** (`WorkerCommunicationService.reportJobStatus()`).

### **Example Flow:**
#### **Worker Reports Job Completion**
```java
public void reportJobStatus(UUID jobExecutionId, ExecutionStatus status, String logs) {
  backendClient.updateJobStatus(jobExecutionId, status, logs);
}
```
#### **Backend Updates Status**
```java
public void updateJobExecutionStatus(UUID jobExecutionId, ExecutionStatus newStatus) {
  jobExecutionRepository.findById(jobExecutionId).orElseThrow(...).updateState(newStatus);
}
```
✅ **Worker correctly updates job status in backend**.

---

## 5. **Backend Handles Dependencies & Execution Order**
- **Backend waits for job dependencies to resolve** (`WorkerCommunicationService.getJobDependencies()`).
- **Worker ensures dependencies are resolved before executing** (`JobRunner.waitAndRunDependentJob()`).
- **Backend executes jobs in correct stage order** (`StageExecutionService.executeNextStage()`).

### **Example Dependency Handling**
#### **Worker Fetching Dependencies**
```java
List<UUID> dependencies = workerCommunicationService.getJobDependencies(job.getId());
```
#### **Worker Waiting for Dependencies**
```java
boolean allDependenciesCompleted = dependencies.stream()
    .allMatch(depId -> workerCommunicationService.getJobStatus(depId) == ExecutionStatus.SUCCESS);
```
✅ **Worker ensures dependencies resolve before execution**.

---

## **6. Final Confirmation: Backend and Worker are Fully Integrated**
✅ **Backend correctly assigns jobs to workers.**  
✅ **Worker correctly executes jobs and reports back.**  
✅ **Execution flow follows pipeline → stages → jobs in order.**  
✅ **Dependencies are resolved before execution.**  
✅ **Pipeline execution finalizes correctly.**

🚀 **System is ready for testing!**

