## Worker Module Code Review & Execution Flow Summary
**Author**: Yiwen Wang
**Version**: 1.0
**Last Updated**: Mar 11, 2025


## 🔍 **Code Review & Key Fixes**

### ✅ **`WorkerBackendClient.java` (API Client)**
- Now properly fetches job details using `Optional<JobExecutionDTO>` to **handle missing jobs gracefully**.
- Logs failures when **updating job status**.

### ✅ **`JobExecutionController.java`**
- Now **forwards job execution requests** to `WorkerExecutionService`, ensuring the **worker only runs assigned jobs**.

### ✅ **`DockerExecutor.java`**
- Executes the job inside a **Docker container**.
- **Captures and logs output** before reporting status.
- Uses **ProcessBuilder** to run the command **safely**.

### ✅ **`WorkerExecutionService.java`**
- **Runs jobs & updates backend** status.
- If a **job fails but is allowed to fail**, it reports **SUCCESS instead** (to avoid stopping the pipeline).
- Uses `backendClient.updateJobStatus()` to report both **running and final job states**.

---

## ⚡ **Worker Execution Flow**

### **1️⃣ Job Execution Request (Triggered by Backend)**
- The backend sends a **POST** request to the worker:  
  ➜ `/api/job/execute`
- `JobExecutionController` receives the request and **calls**:  
  ➜ `WorkerExecutionService.executeJob(job)`

### **2️⃣ Worker Starts the Job**
- **Logs job start** and updates backend:
  ```java
  backendClient.updateJobStatus(job.getId(), ExecutionStatus.RUNNING, "Job execution started.");
  ```
- Calls `DockerExecutor.execute(job)` to run inside a **Docker container**.

### **3️⃣ Docker Runs the Job**
- **Builds and runs the command**:
  ```java
  docker run --rm <docker-image> sh -c "<script>"
  ```
- **Captures logs & waits for completion**.

### **4️⃣ Worker Reports Execution Result**
- If **job succeeds** ➜ Reports `SUCCESS`
- If **job fails** but `allowFailure = true` ➜ Reports `SUCCESS`
- Otherwise ➜ Reports `FAILED`

---


