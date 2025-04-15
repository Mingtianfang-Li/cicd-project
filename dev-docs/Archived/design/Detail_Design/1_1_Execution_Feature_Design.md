# **CI/CD Execution Feature Design**
* **Author**: Yiwen Wang
* **Version**: 1.1
* **Last Updated**: Mar 4, 2025

## **Overview**

The execution feature is responsible for running CI/CD pipelines by reading the YAML configuration and executing the jobs in a structured manner. The execution process ensures that jobs and stages run in the correct sequence while enforcing dependencies, failure rules, and logging execution states.

## **1. Core Responsibilities**

1. **Execute pipelines, stages, and jobs** in the correct sequence.
2. **Enforce dependency rules** between jobs and stages.
3. **Manage execution states** (`Pending`, `Running`, `Success`, `Failed`, `Canceled`).
4. **Handle failures**:
    - Stop execution immediately when a job fails (unless `allow_failure` is set).
    - Drop pending jobs and stages when a failure occurs.
5. **Prevent duplicate executions** by identifying duplicate requests and keeping the oldest.
6. **Log execution status** separately for debugging and monitoring purposes.
7. **Detect and prevent dependency cycles** before execution.
8. **Support local execution mode** and offline storage.
9. **Enable configuration overrides from CLI commands.**

---

## **2. Execution States**

Each execution entity (Pipeline, Stage, Job) transitions through predefined states:

| State      | Description                                          |
| ---------- | ---------------------------------------------------- |
| `Pending`  | The execution is scheduled but has not started.      |
| `Running`  | Execution is in progress.                            |
| `Success`  | Execution completed successfully.                    |
| `Failed`   | Execution encountered a failure.                     |
| `Canceled` | The job/stage was dropped due to an earlier failure. |

Stages and jobs share these states and follow the same logic.

### **2.1. Job Pending State Explanation**

A job remains in the `Pending` state when:

- It has **dependencies** that are not yet met (waiting for dependent jobs to finish).
- It is **queued for execution** but waiting for available system resources (e.g., CPU, memory, or concurrency limits).
- The **previous stage has not completed yet**, meaning the job is queued but cannot start execution.
- The **pipeline execution is scheduled**, but the job has not started running yet.

A job transitions from `Pending` to `Running` once all required conditions are met.

### **2.2. When is a Job Canceled?**

A **job or stage is marked as ****`Canceled`** when:

1. **A failure occurs earlier in the pipeline**, and this job/stage was **waiting to run**.
2. The **pipeline execution stops**, and all remaining jobs **are dropped** before execution.
3. Those jobs/stages that were waiting in **Pending state** are marked as `Canceled`.

### **2.3. What Happens When a Job is Not Allowed to Fail?**

If a **job is not allowed to fail** (`allow_failure = false`) and it **fails**, then:

1. The **entire pipeline execution is marked as Failed**.
2. **All pending jobs and stages that have not started yet are dropped**.
3. Those jobs and stages remain in the **Pending** state but are never executed.
4. **The pipeline stops immediately** (no further execution).

Example Scenario:

- **Job B fails**, and it is not allowed to fail.
- **Job C and Job D are canceled** since the pipeline stops immediately.

| Job     | Status                         |
| ------- | ------------------------------ |
| `Job A` | ✅ Success                      |
| `Job B` | ❌ Failed (not allowed to fail) |
| `Job C` | ⚠️ Canceled                    |
| `Job D` | ⚠️ Canceled                    |

However, if `Job B` was allowed to fail (`allow_failure = true`), the pipeline would continue executing the next jobs without marking the pipeline as failed.

---

## **3. Dependency Cycle Detection**

A **dependency cycle** occurs when a job is dependent on itself either directly or indirectly through a chain of dependencies. The system must detect and prevent execution if cycles exist.

### **3.1. How to Detect Dependency Cycles**

- **Use Graph Cycle Detection Algorithms**: Represent the job dependencies as a Directed Graph and use **Depth-First Search (DFS) with cycle detection** (or **Kahn’s Algorithm for topological sorting**).
- **Process:**
    1. Build a **directed graph** where jobs are nodes and dependencies are directed edges.
    2. Perform **DFS** on each node:
        - If a node is visited twice in the same DFS path, a cycle exists.
    3. Alternatively, apply **Kahn’s Algorithm**:
        - If not all nodes are processed in a **topological sort**, a cycle exists.

### **3.2. Example Cycle Detection Code (Pseudo-Code)**

```python
from collections import defaultdict

def detect_cycle(jobs):
    graph = defaultdict(list)
    in_degree = {job: 0 for job in jobs}
    
    # Build the dependency graph
    for job, dependencies in jobs.items():
        for dep in dependencies:
            graph[dep].append(job)
            in_degree[job] += 1
    
    # Perform Kahn's Algorithm (Topological Sort)
    queue = [job for job in jobs if in_degree[job] == 0]
    processed_jobs = 0
    
    while queue:
        current = queue.pop(0)
        processed_jobs += 1
        
        for dependent in graph[current]:
            in_degree[dependent] -= 1
            if in_degree[dependent] == 0:
                queue.append(dependent)
    
    return processed_jobs != len(jobs)  # If not all jobs were processed, cycle detected
```

If a cycle is detected, the system must **abort execution** and notify the user.

---

## **4. CLI Commands for Execution**

### **4.1. Running a Pipeline**

| Command                                                                                             | Description                                                   |
| --------------------------------------------------------------------------------------------------- | ------------------------------------------------------------- |
| `xx run --repo <repo_url> --pipeline <pipeline_name>`                                               | Runs the specified pipeline from a remote repository.         |
| `xx run --local --repo <repo_path> --pipeline <pipeline_name>`                                      | Runs the pipeline from a local repository.                    |
| `xx run --repo <repo_url> --branch <branch_name> --commit <commit_hash> --pipeline <pipeline_name>` | Runs the pipeline at a specific branch and commit.            |
| `xx run --local --override "global.docker.image=gradle:jdk8" --file <config_file>`                  | Runs a pipeline locally with overridden configuration values. |

### **4.2. Execution Logs**

- Execution logs should be stored separately for debugging purposes.
- Logs should be retrieved using a **separate log retrieval command**, not the report feature.

---

## **5. Execution Optimization & Performance Considerations**

1. **Parallel Execution**: Run jobs in parallel when dependencies allow.
2. **Queue Management**: Implement a job queue for handling pending executions.
3. **Failure Isolation**: Ensure failed jobs do not block independent jobs.
4. **Resource Management**: Optimize CPU and memory usage for efficient execution.
5. **Duplicate Execution Handling**: Check if an identical pipeline execution request exists before starting a new run.

---

## **6. Conclusion**

The execution feature ensures pipelines run efficiently, enforcing correct dependencies and handling failures appropriately. The CLI provides flexible options for execution, and logs are stored separately for reporting and debugging. Additionally, cycle detection prevents invalid pipeline configurations from running.

