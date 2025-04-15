# CLI Module Overview

- **Version:** 1.0
- **Updated:** Mar 1, 2025
- **Author:** Yiwen Wang

## 1. **Introduction**
The `cli` module is a **command-line interface (CLI) tool** that interacts with the backend module to manage CI/CD pipelines. It provides functionalities like:

- Checking pipelineEntity configurations
- Simulating (dry-run) pipelineEntity execution
- Running pipelines
- Fetching pipelineEntity execution reports

## 2. **Module Structure**
The CLI module consists of the following **main components**:

### 🏗 **Package Structure**
```
cli
│── api
│   └── CliBackendClient.java
│── commands
│   ├── BaseCommand.java
│   ├── CheckCommand.java
│   ├── DryRunCommand.java
│   ├── ReportCommand.java
│   ├── RunCommand.java
│── CliApp.java
```

### 📦 **Package Details**
| Package             | Purpose |
|---------------------|---------|
| **`api`**          | Handles backend communication using HTTP requests via `OkHttpClient`. |
| **`commands`**     | Implements CLI commands using Picocli. Each command corresponds to a backend action. |
| **Root (`CliApp`)**| Main entry point that registers CLI commands and executes them. |

---

## 3. **How CLI Interacts with Other Modules**

### 🔄 **CLI ↔ Backend Module**
- CLI interacts with the **backend API** via `CliBackendClient`, which:
    - Sends HTTP requests to backend endpoints (`/api/v1/pipelines`).
    - Parses JSON/YAML responses.
    - Handles errors and network issues.

- Example:  
  When the user runs `cli run --pipelineEntity my-pipelineEntity`, the following happens:
    1. `RunCommand` calls `CliBackendClient.runPipeline(request)`.
    2. `CliBackendClient` sends an HTTP POST request to `/api/v1/pipelines/run`.
    3. The backend starts execution and responds with status updates.
    4. CLI displays the result to the user.

### 🔄 **CLI ↔ Common Module**
- CLI **uses shared models** from the `common` module:
    - `RunPipelineRequest` (for submitting pipelineEntity runs)
    - `PipelineCheckResponse` (for validating pipelineEntity configuration)

- **Why?**
    - The common module ensures **data consistency** across CLI and Backend.
    - Both modules use the same request/response objects, preventing API mismatches.

---

## 4. **How CLI Commands Work**
Each CLI command corresponds to a backend API action:

| Command         | Backend Endpoint               | Description |
|---------------|------------------------------|------------|
| `cli check`  | `/api/v1/pipelines/check`     | Validates a pipelineEntity configuration file. |
| `cli dry-run` | `/api/v1/pipelines/dry-run`  | Simulates pipelineEntity execution. |
| `cli run`     | `/api/v1/pipelines/run`      | Starts a pipelineEntity execution. |
| `cli report`  | `/api/v1/pipelines`          | Retrieves execution history. |

---

## 5. **Running the CLI**
1️⃣ **Build the CLI module**
```sh
./gradlew :cli:build
```

2️⃣ **Run the CLI tool**
```sh
java -jar cli/build/libs/cli.jar --help
```

3️⃣ **Example Usage**
```sh
# Check pipelineEntity configuration
cli check my-config.yml

# Simulate execution (dry-run)
cli dry-run my-config.yml --output json

# Run a pipelineEntity
cli run --pipelineEntity my-pipelineEntity

# Get execution history
cli report --pipelineEntity my-pipelineEntity --output yaml
```

---

## 6. **Future Improvements**
✅ Support **custom backend URL** via CLI flag (`--server-url`)  
✅ Add **authentication support** (JWT tokens)  
✅ Improve **error handling** for failed API calls

---

## Conclusion
The CLI module provides a **user-friendly interface** for interacting with the CI/CD backend. It uses `common` models for **data consistency** and `backend` endpoints for **pipelineEntity execution and reporting**. Future enhancements will improve usability and security.

