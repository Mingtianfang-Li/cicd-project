# CI/CD System Overview
* **Author**: Yiwen Wang
* **Version**: 1.0
* **Last Updated**: Mar 4, 2025

## **Main Features**

### **1️⃣ Execute a Pipeline**
- **Reads the CI/CD YAML configuration** to determine the pipeline structure.
- **Runs the jobs and stages** as defined in the configuration.
- **Handles execution logic**:
    - Runs jobs in **parallel or sequentially**, based on dependencies.
    - Stops execution **immediately on failure** unless `allow_failure` is set.
    - Tracks execution states (`Pending`, `Running`, `Success`, `Failed`, `Canceled`).
- **Stores execution results** for future reporting.
- **Logs execution details** (can be stored separately).

---

### **2️⃣ Report Execution Summaries**
- **Retrieves and summarizes past pipeline runs**.
- **Supports different levels of reporting**:
    - **Pipeline-level summary** (e.g., status of an entire pipeline run).
    - **Stage-level summary** (e.g., status of a specific stage in a run).
    - **Job-level summary** (e.g., status of an individual job in a run).
- **Includes key execution details**:
    - Pipeline name, run number, commit hash.
    - Start time, completion time.
    - Status of pipeline, stages, and jobs.
- **Does not include logs by default**, but logs can be retrieved separately.

---

## **Final Summary**
✅ **Core Features of the CI/CD System**:
1. **Pipeline Execution** – Reads YAML, executes jobs, and stores results.
2. **Execution Reporting** – Provides structured summaries of past runs.

