# Local Execution Plan for CI/CD CLI
- **Version:** 1.0
- **Updated:** March 2, 2025
- **Author:** Yiwen Wang

## 📌 Overview

This document outlines the plan for supporting **local execution** of pipelines within the CLI without directly relying on the backend for execution. The local run must still **log execution states** but should not depend on backend processing.

---

## ✅ **Key Requirements**

### **1️⃣ Logging Execution Locally**

- Local execution **must track execution progress** for future reporting.
- Execution **does not require backend processing** but **should be logged**.
- The CLI should **store results** in a file/database to support reporting (`xx report`).
- If CLI only prints execution results **without logging**, reports won’t work.

### **2️⃣ Execution Without Backend**

- Local execution should **run the pipelineEntity entirely on the local machine**.
- CLI **must not call backend APIs** for execution.
- However, execution states should still be **logged locally**.

### **3️⃣ Syncing Execution Logs with Backend**

- Local executions should be **stored first** and later synced to the backend.
- A separate command (`xx sync`) will **upload logs** to the backend.

---

## 🚀 **Planned Implementation**

### **1️⃣ Local Execution Logger**

Create a `LocalExecutionLogger` that:

- Stores execution states **locally** in `local-executions.json`.
- **Implements an interface** for consistency with backend execution tracking.
- Allows easy **syncing of execution results** later.

```java
package edu.neu.cs6510.sp25.t1.cli.execution;

import edu.neu.cs6510.sp25.t1.common.execution.ExecutionState;
import edu.neu.cs6510.sp25.t1.common.executor.ExecutionStateUpdater;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * CLI implementation of ExecutionStateUpdater that logs execution locally.
 */
public class LocalExecutionLogger implements ExecutionStateUpdater {
    private static final String LOG_FILE = "local-executions.json";

    @Override
    public void updatePipelineState(String pipelineName, ExecutionState state) {
        log("Pipeline " + pipelineName + " state: " + state);
        saveExecutionLocally(pipelineName, "N/A", state);
    }

    @Override
    public void updateJobState(String jobName, ExecutionState state) {
        log("Job " + jobName + " state: " + state);
        saveExecutionLocally("N/A", jobName, state);
    }

    @Override
    public void log(String message) {
        System.out.println("[Local Execution] " + message);
    }

    @Override
    public void saveExecutionLocally(String pipelineName, String jobName, ExecutionState state) {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            String entry = String.format(
                "{ \"pipelineEntity\": \"%s\", \"jobEntity\": \"%s\", \"state\": \"%s\" }\n",
                pipelineName, jobName, state.name()
            );
            writer.write(entry);
        } catch (IOException e) {
            System.err.println("Error saving local execution log: " + e.getMessage());
        }
    }
}
```

---

### **2️⃣ CLI Uses `PipelineExecutor` with `LocalExecutionLogger`**

Now, the CLI runs pipelines **without calling the backend** but **tracks execution states locally**.

#### ✅ **Modify `RunCommand.java`**

```java
PipelineExecutor executor = new PipelineExecutor(new LocalExecutionLogger());

PipelineRunState pipelineState = new PipelineRunState("test-pipelineEntity");
executor.executePipeline(pipelineState);
```

---

### **3️⃣ Sync Command to Upload Logs to Backend**

Later, we need a command (`xx sync`) to **sync logs with the backend**.

```java
@CommandLine.Command(name = "sync", description = "Sync local execution logs to backend")
public class SyncCommand extends BaseCommand {

    private final CliBackendClient backendClient;

    public SyncCommand() {
        this.backendClient = new CliBackendClient("http://localhost:8080");
    }

    @Override
    public Integer call() {
        String logs = LocalExecutionLogger.readExecutionLog();
        if (logs.isEmpty()) {
            logInfo("No logs to sync.");
            return 0;
        }

        try {
            backendClient.syncExecutionLogs(logs);
            logInfo("Execution logs successfully synced with backend.");
            return 0;
        } catch (Exception e) {
            logError("Failed to sync logs: " + e.getMessage());
            return 1;
        }
    }
}
```

---

## 📌 **Final Implementation Plan Summary**

| **Feature**                         | **Implemented In**                     | **Details**                           |
| ----------------------------------- | -------------------------------------- | ------------------------------------- |
| ✅ **Log execution state locally**   | `LocalExecutionLogger.java`            | Store logs in `local-executions.json` |
| ✅ **Run pipelines without backend** | `RunCommand.java`                      | CLI calls `PipelineExecutor` locally  |
| ✅ **Sync logs with backend later**  | `SyncCommand.java`                     | Upload logs via `xx sync`             |
| ✅ **Ensure reports work**           | `local-executions.json` stores results | CLI can still generate reports        |

---

## 🎯 **Expected CLI Behavior**

### **Running a Pipeline Locally**

```sh
xx run --local --file .pipelines/pipelineEntity.yaml
```

📌 Logs stored in `local-executions.json`, but **no backend interaction**.

---

### **Syncing Local Logs to Backend**

```sh
xx sync
```

📌 Sends stored logs to backend for **reporting & tracking**.

---

## 🔥 **Final Thoughts**

✅ Local execution works **without backend dependency**.  
✅ Logs are stored locally and can be **synced later**.  
✅ Supports **full offline execution** with later reporting.  
🚀 **Efficient, modular, and fully functional!** 🎯