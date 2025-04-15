# **CI/CD Report Feature Design**
* **Author**: Yiwen Wang
* **Version**: 1.0
* **Last Updated**: Mar 4, 2025

## **Overview**
The report feature is responsible for summarizing past executions of pipelines, stages, and jobs. It enables users to query past runs and obtain execution details such as status, timestamps, and failure handling. Reports do not include execution logs by default but allow retrieval of logs separately if required.

## **1. Core Responsibilities**
1. **Store and retrieve execution summaries** at the pipeline, stage, and job levels.
2. **Support querying** based on different parameters such as pipeline name, run number, stage name, and job name.
3. **Calculate execution status** based on pipeline, stage, and job outcomes.
4. **Ensure efficiency** by indexing key fields for fast retrieval.
5. **Support local and remote reports**, allowing users to retrieve local execution results independently from remote ones.

---

## **2. Report Querying & Retrieval**
### **2.1. Querying Available Reports**
| Query | CLI Command |
|-------|------------|
| Retrieve all pipelines with reports | `xx report` |
| Retrieve all executions for a pipeline | `xx report --repo <repo_url> --pipeline <pipeline_name>` |
| Retrieve a specific pipeline execution | `xx report --repo <repo_url> --pipeline <pipeline_name> --run <run_number>` |
| Retrieve a stage summary for a pipeline execution | `xx report --repo <repo_url> --pipeline <pipeline_name> --stage <stage_name> --run <run_number>` |
| Retrieve a job summary within a stage execution | `xx report --repo <repo_url> --pipeline <pipeline_name> --stage <stage_name> --job <job_name> --run <run_number>` |

### **2.2. Report Output Format**
Reports should be readable and user-friendly. While JSON is structured and useful for integrations, a more human-readable tabular format can be used for CLI outputs.

#### **Pipeline Execution Summary (Human-Readable Format)**
```
Pipeline: code-review
Run Number: 2
Commit Hash: 3df7142
Status: FAILED
Start Time: 2024-03-01 12:00:00 UTC
Completion Time: 2024-03-01 12:30:00 UTC

Stages:
  - Build     | SUCCESS  | 12:00:00 - 12:10:00
  - Test      | FAILED   | 12:10:00 - 12:30:00
```

#### **Stage Execution Summary**
```
Pipeline: code-review
Run Number: 2
Stage: Build
Status: SUCCESS
Start Time: 12:00:00 UTC
Completion Time: 12:10:00 UTC

Jobs:
  - Compile   | SUCCESS  | 12:00:00 - 12:05:00 | Allows Failure: No
```

This format provides a cleaner view for users when viewed in a terminal.

---

## **3. Execution Flow for Report Generation**
### **3.1. Report Generation Process**
1. **User requests a report via CLI** (e.g., `xx report --pipeline code-review --run 2`).
2. **System validates input parameters** (checks if the pipeline exists, run number is valid, etc.).
3. **Retrieve execution data**:
    - If querying a pipeline, fetch all associated stage and job statuses.
    - If querying a stage, fetch only relevant stage and job data.
    - If querying a job, fetch only job-level execution details.
4. **Format data** based on CLI preferences:
    - If JSON output is required (for API integration), return structured JSON.
    - If CLI mode is used, return a **human-readable table**.
5. **Display or return the report** to the user.

### **3.2. Execution Flow Diagram**
```
User Request → Validate Input → Retrieve Execution Data → Format Report → Display Output
```

---

## **4. Report Storage & Performance Considerations**
1. **Database Indexing**: Index `pipeline_id`, `run_number`, and `commit_hash` for fast retrieval.
2. **Caching**: Frequently accessed reports should be cached in Redis or an in-memory store.
3. **Archival Strategy**: Old reports should be archived periodically to optimize database performance.
4. **Scalability**: Support distributed storage and querying for large-scale CI/CD systems.

---

## **5. Conclusion**
The report feature enables structured retrieval of past executions, providing insights at the **pipeline, stage, and job levels**. The design ensures **efficient querying, storage, and scalability** to handle frequent report requests. The CLI should return a **human-readable format by default**, while structured JSON can be an option for integrations.

