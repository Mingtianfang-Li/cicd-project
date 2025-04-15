# Repository Structure
* **Author**: Yiwen Wang
* **Version**: 1.0
* **Last Updated**: Mar 4, 2025

The system will be structured as a Gradle multi-module project within a single monorepo, detail file name may change in the future:

```bash
project-root/
  ├── backend/
  │   ├── api/
  │   │   ├── controller/
  │   │   │   ├── JobController.java
  │   │   │   ├── PipelineController.java
  │   │   │   ├── StageController.java
  │   │   ├── service/
  │   │   │   ├── JobService.java
  │   │   │   ├── PipelineService.java
  │   │   │   ├── StageService.java
  │   │   ├── request/
  │   │   │   ├── JobExecutionRequest.java
  │   │   │   ├── PipelineExecutionRequest.java
  │   │   │   ├── JobStatusUpdate.java
  │   │   ├── response/
  │   │   │   ├── JobExecutionResponse.java
  │   │   │   ├── PipelineExecutionResponse.java
  │   ├── database/
  │   │   ├── entity/
  │   │   │   ├── JobEntity.java
  │   │   │   ├── PipelineEntity.java
  │   │   │   ├── StageEntity.java
  │   │   ├── repository/
  │   │   │   ├── JobRepository.java
  │   │   │   ├── PipelineRepository.java
  │   │   │   ├── StageRepository.java
  │   │   ├── dto/
  │   │   │   ├── JobDTO.java
  │   │   │   ├── PipelineDTO.java
  │   │   │   ├── StageDTO.java
  │   ├── service/
  │   │   ├── execution/
  │   │   │   ├── JobExecutionService.java
  │   │   │   ├── PipelineExecutionService.java
  │   │   │   ├── StageExecutionService.java
  │   ├── config/
  │   │   ├── AppConfig.java
  │   │   ├── DatabaseConfig.java

  ├── worker/
  │   ├── api/
  │   │   ├── controller/
  │   │   │   ├── WorkerController.java
  │   │   ├── client/
  │   │   │   ├── WorkerBackendClient.java
  │   ├── executor/
  │   │   ├── JobExecutor.java
  │   │   ├── StageExecutor.java
  │   ├── manager/
  │   │   ├── DockerManager.java
  │   ├── config/
  │   │   ├── WorkerConfig.java

  ├── cli/
  │   ├── command/
  │   │   ├── RunCommand.java
  │   │   ├── CheckCommand.java
  │   ├── validation/
  │   │   ├── error/
  │   │   │   ├── ErrorHandler.java
  │   │   │   ├── ValidationException.java
  │   │   ├── parser/
  │   │   │   ├── YamlParser.java
  │   │   ├── validator/
  │   │   │   ├── PipelineValidator.java
  │   │   │   ├── YamlPipelineValidator.java
  │   ├── config/
  │   │   ├── CliConfig.java

  ├── common/
  │   ├── api/
  │   │   ├── request/
  │   │   │   ├── JobExecutionRequest.java
  │   │   │   ├── PipelineExecutionRequest.java
  │   │   │   ├── JobStatusUpdate.java
  │   │   ├── response/
  │   │   │   ├── JobExecutionResponse.java
  │   │   │   ├── PipelineExecutionResponse.java
  │   ├── model/
  │   │   ├── JobExecution.java
  │   │   ├── StageExecution.java
  │   │   ├── PipelineExecution.java
  │   │   ├── PipelineConfig.java
  │   │   ├── JobConfig.java
  │   ├── logging/
  │   │   ├── CicdLogger.java
  │   ├── validation/
  │   │   ├── YamlSchemaValidator.java
  │   ├── enums/
  │   │   ├── ExecutionStatus.java
  │   ├── config/
  │   │   ├── GlobalConfig.java

```

- update: report feature proposed structure, detail file name may change in the future:
```bash
📂 project-root
│
├── 📂 backend
│   ├── 📂 api
│   │   ├── 📂 controller
│   │   │   ├── ReportController.java  # Handles API requests for fetching reports
│   │   ├── 📂 request
│   │   │   ├── ReportRequest.java  # Represents request body for fetching reports
│   │   ├── 📂 response
│   │   │   ├── PipelineReportResponse.java  # Response DTO for pipeline-level report
│   │   │   ├── StageReportResponse.java  # Response DTO for stage-level report
│   │   │   ├── JobReportResponse.java  # Response DTO for job-level report
│   │   ├── 📂 client
│   │   │   ├── ReportClient.java  # Optional: If fetching reports from an external service
│   │
│   ├── 📂 service
│   │   ├── ReportService.java  # Service layer handling report retrieval and processing
│   │   ├── PipelineReportService.java  # Fetches past pipeline runs summary
│   │   ├── StageReportService.java  # Fetches stage-specific reports
│   │   ├── JobReportService.java  # Fetches job-specific reports
│   │
│   ├── 📂 repository
│   │   ├── PipelineReportRepository.java  # Fetch pipeline reports from DB
│   │   ├── StageReportRepository.java  # Fetch stage reports from DB
│   │   ├── JobReportRepository.java  # Fetch job reports from DB
│   │
│   ├── 📂 model
│   │   ├── PipelineReport.java  # Entity representing a pipeline report
│   │   ├── StageReport.java  # Entity representing a stage report
│   │   ├── JobReport.java  # Entity representing a job report
│   │
│   ├── 📂 aggregator
│   │   ├── PipelineReportAggregator.java  # Aggregates job & stage reports to pipeline level
│   │   ├── StageReportAggregator.java  # Aggregates job reports to stage level
│   │   ├── JobReportAggregator.java  # Aggregates job execution logs
│
│
├── 📂 cli
│   ├── 📂 command
│   │   ├── ReportCommand.java  # Main CLI command for fetching reports
│   │   ├── PipelineReportCommand.java  # CLI command for fetching pipeline reports
│   │   ├── StageReportCommand.java  # CLI command for fetching stage reports
│   │   ├── JobReportCommand.java  # CLI command for fetching job reports
│   │
│   ├── 📂 service
│   │   ├── ReportService.java  # Calls backend API to retrieve reports
│   │
│   ├── 📂 api
│   │   ├── ReportClient.java  # Sends requests to backend API to fetch reports
│
│
├── 📂 worker
│   ├── 📂 api
│   │   ├── WorkerReportClient.java  # Sends job execution status updates
│
│
├── 📂 common
│   ├── 📂 model
│   │   ├── ReportStatus.java  # Enum for Success, Failed, Canceled statuses
│   │
│   ├── 📂 dto
│   │   ├── PipelineReportDTO.java  # DTO for pipeline report data transfer
│   │   ├── StageReportDTO.java  # DTO for stage report data transfer
│   │   ├── JobReportDTO.java  # DTO for job report data transfer

```