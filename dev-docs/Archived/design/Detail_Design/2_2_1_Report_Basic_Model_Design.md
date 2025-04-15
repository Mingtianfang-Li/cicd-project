# Report Feature Data Models
* **Author**: Yiwen Wang
* **Version**: 1.0
* **Last Updated**: Mar 4, 2025

## **Overview**
The report feature is responsible for summarizing past pipeline executions. It provides different levels of reporting, including pipeline, stage, and job execution summaries.

## **1. Pipeline Report Model**
Represents a summary of all past executions for a pipeline.

| Attribute | Type | Mandatory? | Description |
|-----------|------|------------|-------------|
| `id` | UUID | ✅ Yes | Unique identifier for the pipeline report |
| `pipeline_id` | UUID | ✅ Yes | Foreign key reference to Pipeline |
| `pipeline_name` | String | ✅ Yes | Name of the pipeline |
| `run_number` | Integer | ✅ Yes | Counter for pipeline runs |
| `commit_hash` | String | ✅ Yes | Git commit hash of the pipeline execution |
| `status` | Enum | ✅ Yes | Execution status (`Success`, `Failed`, `Canceled`) |
| `start_time` | DateTime | ✅ Yes | Start timestamp of execution |
| `completion_time` | DateTime | ✅ Yes | Completion timestamp of execution |

---

## **2. Stage Report Model**
Represents a summary of a specific stage in a pipeline execution.

| Attribute | Type | Mandatory? | Description |
|-----------|------|------------|-------------|
| `id` | UUID | ✅ Yes | Unique identifier for the stage report |
| `pipeline_report_id` | UUID | ✅ Yes | Foreign key reference to PipelineReport |
| `stage_id` | UUID | ✅ Yes | Foreign key reference to Stage |
| `stage_name` | String | ✅ Yes | Name of the stage |
| `status` | Enum | ✅ Yes | Execution status (`Success`, `Failed`, `Canceled`) |
| `start_time` | DateTime | ✅ Yes | Start timestamp of stage execution |
| `completion_time` | DateTime | ✅ Yes | Completion timestamp of stage execution |

---

## **3. Job Report Model**
Represents a summary of a specific job in a stage execution.

| Attribute | Type | Mandatory? | Description |
|-----------|------|------------|-------------|
| `id` | UUID | ✅ Yes | Unique identifier for the job report |
| `stage_report_id` | UUID | ✅ Yes | Foreign key reference to StageReport |
| `job_id` | UUID | ✅ Yes | Foreign key reference to Job |
| `job_name` | String | ✅ Yes | Name of the job |
| `status` | Enum | ✅ Yes | Execution status (`Success`, `Failed`, `Canceled`) |
| `allows_failure` | Boolean | ✅ Yes | Whether failure is allowed for the job |
| `start_time` | DateTime | ✅ Yes | Start timestamp of job execution |
| `completion_time` | DateTime | ✅ Yes | Completion timestamp of job execution |

---

## **4. Report Querying and Retrieval**
The system must support retrieving reports based on different query parameters:
- **Retrieve all pipelines that have reports**
  ```
  xx report
  ```
- **Retrieve all executions of a specific pipeline**
  ```
  xx report --repo https://github.com/company/project --pipeline code-review
  ```
- **Retrieve a specific execution summary**
  ```
  xx report --repo https://github.com/company/project --pipeline code-review --run 2
  ```
- **Retrieve a specific stage summary**
  ```
  xx report --repo https://github.com/company/project --pipeline code-review --stage build --run 2
  ```
- **Retrieve a specific job summary**
  ```
  xx report --repo https://github.com/company/project --pipeline code-review --stage build --job compile --run 2
  ```

---

## **Conclusion**
The report feature provides structured summaries of past pipeline executions at different levels (pipeline, stage, job). Reports focus on **status tracking and execution timelines**, without including logs by default. Logs can be retrieved separately if needed.

