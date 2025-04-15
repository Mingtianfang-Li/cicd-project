# Basic Model Design
* **Author**: Yiwen Wang
* **Version**: 1.0
* **Last Updated**: Mar 4, 2025

## Overview
- This is the design for the job, pipeline, stage and their executions.

## Data Model Overview

### 1. Job Model
Represents the smallest task in a CI/CD pipeline.

| Attribute | Type | Mandatory? | Description |
|-----------|------|------------|-------------|
| `id` | UUID | ✅ Yes | Unique identifier for the job |
| `name` | String | ✅ Yes | Unique name within a stage |
| `stage_id` | UUID | ✅ Yes | Foreign key reference to Stage |
| `docker_image` | String | ❌ No | Docker image for the job (default can be used) |
| `commands` | List[String] | ✅ Yes | Commands to execute in the job |
| `dependencies` | List[UUID] | ❌ No | Job dependencies (other job IDs) |
| `allow_failure` | Boolean | ❌ No | Whether the job can fail without stopping the pipeline (default: false) |
| `artifacts` | List[String] | ❌ No | Files or folders to be saved |
| `created_at` | DateTime | ✅ Yes | Timestamp of job creation |
| `updated_at` | DateTime | ✅ Yes | Timestamp of last update |

---

### 2. Stage Model
Represents a group of jobs within a pipeline.

| Attribute | Type | Mandatory? | Description |
|-----------|------|------------|-------------|
| `id` | UUID | ✅ Yes | Unique identifier for the stage |
| `name` | String | ✅ Yes | Unique stage name within a pipeline |
| `pipeline_id` | UUID | ✅ Yes | Foreign key reference to Pipeline |
| `order` | Integer | ✅ Yes | Execution order within the pipeline |
| `created_at` | DateTime | ✅ Yes | Timestamp of stage creation |
| `updated_at` | DateTime | ✅ Yes | Timestamp of last update |

---

### 3. Pipeline Model
Represents a sequence of stages and jobs.

| Attribute | Type | Mandatory? | Description |
|-----------|------|------------|-------------|
| `id` | UUID | ✅ Yes | Unique identifier for the pipeline |
| `name` | String | ✅ Yes | Unique name for the pipeline within a repository |
| `repo_url` | String | ✅ Yes | URL or local path of the repository |
| `branch` | String | ❌ No | Git branch name (default: "main") |
| `commit_hash` | String | ❌ No | Git commit hash (default: latest commit) |
| `created_at` | DateTime | ✅ Yes | Timestamp of pipeline creation |
| `updated_at` | DateTime | ✅ Yes | Timestamp of last update |

---

### 4. Job Execution Model
Represents the execution details of a job.

| Attribute | Type | Mandatory? | Description |
|-----------|------|------------|-------------|
| `id` | UUID | ✅ Yes | Unique identifier for the job execution |
| `job_id` | UUID | ✅ Yes | Foreign key reference to Job |
| `stage_execution_id` | UUID | ✅ Yes | Foreign key reference to StageExecution |
| `status` | Enum | ✅ Yes | Execution status (`Pending`, `Running`, `Success`, `Failed`, `Canceled`) |
| `start_time` | DateTime | ❌ No | Job execution start time |
| `end_time` | DateTime | ❌ No | Job execution end time |
| `logs` | String | ❌ No | Log output of job execution |

---

### 5. Stage Execution Model
Represents the execution details of a stage.

| Attribute | Type | Mandatory? | Description |
|-----------|------|------------|-------------|
| `id` | UUID | ✅ Yes | Unique identifier for the stage execution |
| `stage_id` | UUID | ✅ Yes | Foreign key reference to Stage |
| `pipeline_execution_id` | UUID | ✅ Yes | Foreign key reference to PipelineExecution |
| `status` | Enum | ✅ Yes | Execution status (`Pending`, `Running`, `Success`, `Failed`, `Canceled`) |
| `start_time` | DateTime | ❌ No | Stage execution start time |
| `end_time` | DateTime | ❌ No | Stage execution end time |

---

### 6. Pipeline Execution Model
Represents an execution instance of a pipeline.

| Attribute | Type | Mandatory? | Description |
|-----------|------|------------|-------------|
| `id` | UUID | ✅ Yes | Unique identifier for the pipeline execution |
| `pipeline_id` | UUID | ✅ Yes | Foreign key reference to Pipeline |
| `status` | Enum | ✅ Yes | Execution status (`Pending`, `Running`, `Success`, `Failed`, `Canceled`) |
| `start_time` | DateTime | ❌ No | Pipeline execution start time |
| `end_time` | DateTime | ❌ No | Pipeline execution end time |
| `run_number` | Integer | ✅ Yes | Unique run number for the pipeline |
| `commit_hash` | String | ✅ Yes | Git commit hash for this execution |

---

### 7. Execution Logs Model (Optional)
- if needed, add a --logs for debugging purposes
- If logs need to be stored separately:

| Attribute | Type | Mandatory? | Description |
|-----------|------|------------|-------------|
| `id` | UUID | ✅ Yes | Unique identifier |
| `execution_id` | UUID | ✅ Yes | Foreign key reference to JobExecution, StageExecution, or PipelineExecution |
| `log_text` | Text | ❌ No | Execution log content |
| `timestamp` | DateTime | ✅ Yes | Log timestamp |



