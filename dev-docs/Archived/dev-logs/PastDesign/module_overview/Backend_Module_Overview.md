## 🛠️ **Backend Module Components**

- **Version:** 1.0
- **Updated:** Mar 1, 2025
- **Author:** Yiwen Wang

### 📌 1. **API Controllers (`api/`)**
Controllers handle **HTTP requests** and interact with services.

| Controller | Path | Description |
|------------|------|-------------|
| `PipelineController` | `/api/v1/pipelines` | Start pipelines, fetch pipelineEntity execution status |
| `ReportController` | `/api/reports` | Fetch all pipelines and execution summaries |
| `ExecutionController` | `/api/executions` | Retrieves logs of jobEntity executions |
| `HealthController` | `/health` | Health check endpoint |

---

### 📌 2. **Services (`service/`)**
Services contain **business logic**.

| Service | Responsibilities |
|---------|------------------|
| `PipelineExecutionService` | Manages the execution of pipelines |
| `ReportService` | Retrieves pipelineEntity reports and execution history |
| `BackendService` | Handles jobEntity status caching with Redis |
| `JobScheduler` | Manages jobEntity queue and sends jobEntities to the worker |

---

### 📌 3. **Repositories (`repository/`)**
Repositories handle **database interactions**.

| Repository | Model | Description |
|------------|---------|-------------|
| `PipelineRepository` | `Pipeline` | Fetches and updates pipelines |
| `StageRepository` | `Stage` | Manages pipelineEntity stageEntities |
| `JobRepository` | `Job` | Manages jobEntities in a pipelineEntity |

---

### 📌 4. **Data Transfer Objects (`dto/`)**
DTOs **prevent direct exposure** of database models in API responses.

| DTO | Maps To | Description |
|-----|--------|-------------|
| `PipelineDTO` | `Pipeline` | Represents a pipelineEntity without exposing database internals |
| `PipelineExecutionSummary` | `PipelineExecution` | Summary of pipelineEntity execution |
| `PipelineStatusResponse` | `PipelineExecution` | Status response for API calls |

---

### 📌 5. **Models (`model/`)**
Models represent **database entities**.

| Model | Table | Description |
|--------|------|-------------|
| `Pipeline` | `pipelines` | Represents a CI/CD pipelineEntity |
| `Stage` | `stageEntities` | Represents a stageEntity in a pipelineEntity |
| `Job` | `jobEntities` | Represents an individual jobEntity |
| `PipelineExecution` | `pipeline_execution` | Tracks active pipelineEntity executions |
| `JobExecution` | `job_execution` | Tracks execution of individual jobEntities |

---

### 📌 6. **Clients (`client/`)**
Clients interact with **external services**.

| Client | External Service                                         | Description |
|--------|----------------------------------------------------------|-------------|
| `WorkerClient` | Worker Service (`http://localhost:8081/api/jobEntities`) | Sends jobEntities for execution |

---

### 📌 7. **Schedulers (`scheduler/`)**
Schedulers **process jobEntity queues** asynchronously.

| Scheduler | Description |
|-----------|-------------|
| `JobScheduler` | Manages jobEntity execution queue and sends jobEntities to WorkerClient |

---

### 📌 8. **Configurations (`config/`)**
Handles application settings (DB, Redis, etc.).

| Config | Description |
|--------|-------------|
| `application.yml` | Defines database, Redis, logging, and service configurations |
| `Flyway migration` | Ensures database schema consistency |

---

## 🔄 **Backend Module Interaction**
### 1️⃣ **User/API Client requests a pipelineEntity execution**
- `PipelineController` receives a `POST /api/v1/pipelines/run` request.
- Calls `PipelineExecutionService.startPipeline(pipelineName)`.
- Returns a `PipelineDTO` with execution details.

### 2️⃣ **Pipeline execution starts**
- `PipelineExecutionService` retrieves pipelineEntity details from `PipelineRepository`.
- Initializes a `PipelineExecution` and stores it in memory.

### 3️⃣ **Jobs are scheduled**
- `JobScheduler` adds jobEntities to an execution queue.
- `WorkerClient` sends jobEntity details to **Worker Service** (`http://localhost:8081/api/jobEntities`).

### 4️⃣ **Job execution status updates**
- Worker service executes jobEntities and sends status updates.
- `BackendService` caches jobEntity execution status in Redis.

### 5️⃣ **Users check pipelineEntity status**
- `PipelineController` handles `GET /api/v1/pipelines/{pipelineName}/status`.
- Returns execution status via `PipelineStatusResponse`.

### 6️⃣ **Users retrieve reports**
- `ReportController` handles `GET /api/reports/pipelines` for execution history.
- `ReportService` fetches pipelineEntity execution details.

---

## 📡 **Backend Module Interaction with Other Modules**
The backend module interacts with:
1. **Database (PostgreSQL)**
    - Stores **pipelines, jobEntities, and execution history**.
2. **Redis**
    - Caches **jobEntity execution statuses** to improve performance.
3. **Worker Service**
    - Sends jobEntities for **execution on worker nodes**.
4. **CLI modules**
    - CLI calls backend to run command.

---

## 🚀 **Conclusion**
- The **backend module** efficiently manages **pipelineEntity execution** and **jobEntity scheduling**.
- Uses **Spring Boot**, **JPA**, **Redis**, and **REST APIs** for **scalability and performance**.
- Interacts with **Worker Service** for **jobEntity execution**.
- Implements **DTOs** to **separate API and database models**.

---

