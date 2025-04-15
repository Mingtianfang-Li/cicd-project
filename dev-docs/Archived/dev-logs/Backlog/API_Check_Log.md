# CI/CD CLI Command Testing Summary
**Version:** `1.0`  
**Last Updated:** `March 3 2025`  
**Author:** Yiwen

## ✅ Worked Command Lines
These commands were executed successfully and produced expected results:

### **Check Configuration File**
```bash
cicd check -f .pipelines/my-cicd-pipeline.yaml
```
**Output:** Passed pipeline validation.

### **Dry Run (Local)**
```bash
cicd dry-run -f .pipelines/my-cicd-pipeline.yaml
```
**Output:** Displayed pipeline execution plan in valid YAML.

### **Check Configuration File (Remote Repo)**
```bash
cicd check --repo https://github.com/YiwenW312/cicdSample.git --branch main -f .pipelines/my-cicd-pipeline.yaml
```
**Output:** Passed pipeline validation.

### **Dry Run (Remote Repo)**
```bash
cicd dry-run --repo https://github.com/YiwenW312/cicdSample.git --branch main -f .pipelines/my-cicd-pipeline.yaml
```
**Output:** Displayed pipeline execution plan in valid YAML.

---
## 🟡 Not Tested Command Lines
These commands were not present in the logs, and their functionality remains unverified:

### **Executing a pipeline with a commit hash**
```bash
cicd run --repo https://github.com/YiwenW312/cicdSample.git --branch main --commit 3df7142 -f .pipelines/my-cicd-pipeline.yaml
```

### **Viewing pipeline execution report by run ID (if supported)**
```bash
cicd report --repo https://github.com/YiwenW312/cicdSample.git --pipeline demo-ci-pipeline --run 1
```

---
## ❌ Failed Command Lines & Fix Suggestions

### **1. Remote Pipeline Execution**
#### **Command:**
```bash
cicd run --repo https://github.com/YiwenW312/cicdSample.git --branch main -f .pipelines/my-cicd-pipeline.yaml
```
#### **Error:**
```
API Error: 404 - Not Found
{"timestamp":"2025-03-03T22:38:03.566+00:00","status":404,"error":"Not Found","path":"/api/v1/pipelines/run"}
```
#### **Cause:**
- The backend server might not be running or is not correctly set up to handle requests.
- The API endpoint `/api/v1/pipelines/run` does not exist or is misconfigured.

#### **Possible Fixes:**
1. **Ensure backend services are running**:
   ```bash
   curl -X GET http://localhost:8080/api/v1/pipelines
   ```
    - If the API is down, restart the backend.

2. **Verify API endpoint paths**:
    - Check if `/api/v1/pipelines/run` exists in the backend server.
    - If the API changed, update the CLI code accordingly.

---

### **2. Local Pipeline Execution**
#### **Command:**
```bash
cicd run --local --repo https://github.com/YiwenW312/cicdSample.git --branch main -f .pipelines/my-cicd-pipeline.yaml
```
#### **Error:**
```
API Error: 404 - Not Found
{"timestamp":"2025-03-03T22:38:10.548+00:00","status":404,"error":"Not Found","path":"/api/v1/execution/state"}
✅ Local pipeline execution completed.
```
#### **Cause:**
- CLI is attempting to report execution state to a backend that does not exist in local mode.
- The API is missing the endpoint `/api/v1/execution/state`.

#### **Possible Fixes:**
1. **Disable API state reporting in local runs**:
    - Modify `RunCommand` to avoid API calls when `--local` is used.

2. **Ensure the backend API is accessible locally**:
    - If local execution still requires API communication, the API should be started on localhost.

---

### **3. Running a pipeline without `-f`**
#### **Command:**
```bash
cicd run --repo https://github.com/YiwenW312/cicdSample.git --branch main
```
#### **Error:**
```
[ERROR] Missing required parameter: --file <pipeline.yaml>
```
#### **Cause:**
- The CLI requires a pipeline configuration file but does not default to `.pipelines/pipeline.yaml`.

#### **Possible Fixes:**
1. **Set a default file in `RunCommand`**:
    - If `-f` is not provided, assume `.pipelines/pipeline.yaml`.

---

### **4. Running a pipeline using `--pipeline`**
#### **Command:**
```bash
cicd run --local --repo ./workspace/project --branch feature1 --pipeline test-pipeline
```
#### **Error:**
```
Unknown options: '--pipeline', 'test-pipeline'
```
#### **Cause:**
- The CLI does not support `--pipeline`; only `-f` (filename) is recognized.

#### **Possible Fixes:**
1. **Allow `--pipeline` option**:
    - Modify `BaseCommand` to support `--pipeline` and map it to a YAML file.
    - Example: If `--pipeline test-pipeline` is given, load `.pipelines/test-pipeline.yaml`.

2. **Ensure users use `-f` instead**:
    - Update the documentation to specify:
      ```bash
      cicd run --local --repo ./workspace/project --branch feature1 -f .pipelines/test-my-cicd-pipeline.yaml
      ```

---

### **5. Report Command Failing**
#### **Command:**
```bash
cicd report --repo https://github.com/YiwenW312/cicdSample.git --pipeline demo-ci-pipeline
```
#### **Error:**
```
Unmatched arguments from index 0: 'report', '--repo', 'https://github.com/YiwenW312/cicdSample.git', '--pipeline', 'demo-ci-pipeline'
```
#### **Cause:**
- The CLI does not have a `report` command implemented.

#### **Possible Fixes:**
1. **Implement `report` command**:
    - Add a `ReportCommand` to retrieve and display past runs.
    - Use `/api/v1/reports` as the API endpoint.

2. **Update CLI help output**:
    - If reporting is not implemented, remove it from the documentation.

---


### **6. Run remotely by file name Command:**
```bash
cicd run -f .pipelines/my-cicd-pipeline.yaml
```

#### **Error:**
```bash
Command Started: RunCommand
Running remotely | Run ID: c6bc7741-e8bc-4965-ab90-21b03f7325aa
14:43:42.146 [main] ERROR edu.neu.cs6510.sp25.t1.cli.api.CliBackendClient -- API Error: 404 -
14:43:42.159 [main] ERROR edu.neu.cs6510.sp25.t1.cli.api.CliBackendClient -- API Error: 404 -
14:43:42.159 [main] ERROR edu.neu.cs6510.sp25.t1.cli.api.CliBackendClient -- Failed to update execution state: API Error: 404 -
{"timestamp":"2025-03-03T22:43:42.157+00:00","status":404,"error":"Not Found","path":"/api/v1/execution/state"}
❌ Pipeline execution failed: API Error: 404 -
{"timestamp":"2025-03-03T22:43:42.137+00:00","status":404,"error":"Not Found","path":"/api/v1/pipelines/run"}
```

#### **Cause:**
- The error **404 - Not Found** indicates that the backend API could not find the pipeline execution endpoint (`/api/v1/pipelines/run`).
- Possible reasons:
   1. The backend service is not running or incorrectly configured.
   2. The pipeline name in `.pipelines/pipeline.yaml` does not exist in the backend.
   3. The API path `/api/v1/pipelines/run` might be incorrect or not exposed.

#### **Suggested Fix:**
- **Ensure Backend is Running:**  
  Verify that the CI/CD backend service is up and accessible by running:
  ```bash
  curl -X GET http://localhost:8080/api/v1/pipelines
  ```
  If this command fails, start the backend service.

- **Validate Pipeline Existence:**  
  Check that `.pipelines/pipeline.yaml` has a valid pipeline name and it exists in the backend database.

- **Check API Endpoint Configuration:**  
  Confirm that the API endpoint `/api/v1/pipelines/run` is correctly configured in the backend.

---

### **2. Remote run branch main Command without file name:**
```bash
cicd run --repo https://github.com/YiwenW312/cicdSample.git --branch main
```

#### **Error:**
```bash
Command Started: RunCommand
[ERROR] Missing required parameter: --file <my-cicd-pipeline.yaml>
14:43:52.634 [main] ERROR edu.neu.cs6510.sp25.t1.cli.commands.RunCommand -- Missing required parameter: --file <my-cicd-pipeline.yaml>
```

#### **Cause:**
- The command is missing the required `--file` parameter to specify which pipeline configuration file to use.
- The CLI does not automatically detect the pipeline file from the repository; it needs explicit input.

#### **Suggested Fix:**
- Include the `--file` parameter:
  ```bash
  cicd run --repo https://github.com/YiwenW312/cicdSample.git --branch main -f .pipelines/my-cicd-pipeline.yaml
  ```
- If using a default pipeline file, ensure the CLI is set up to detect `.pipelines/pipeline.yaml` automatically.

---

### **3. Fetch all Report Command:**
```bash
cicd report
```

#### **Error:**
```bash
Unmatched argument at index 0: 'report'
Usage: cli [-hvV] [COMMAND]
A CI/CD Command-Line Tool
  -h, --help      Show this help message and exit.
  -v, --verbose   Enable verbose output.
  -V, --version   Print version information and exit.
Commands:
  run      Execute a pipeline (locally or via backend)
  check    Validate the pipeline configuration file.
  dry-run  Simulate pipeline execution without running jobs.
```

#### **Cause:**
- The `report` command does not exist or is not implemented in the CLI.
- The CLI only recognizes `run`, `check`, and `dry-run` as valid commands.

#### **Suggested Fix:**
- If `report` is not yet implemented, it needs to be added to the CLI.
- If it exists but is not recognized, check the CLI version and available commands:
  ```bash
  cicd --help
  ```
- If `report` should exist, try specifying a pipeline:
  ```bash
  cicd report --repo https://github.com/YiwenW312/cicdSample.git --pipeline demo-ci-pipeline
  ```
- If the CLI is expected to support reporting but does not, verify if the feature is implemented in the backend.

---

