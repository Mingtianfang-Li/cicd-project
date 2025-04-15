# 🚀 Backend Execution Flow

- Version: 1.0
- Updated with the Project requirements by Feb 28, 2025
- Author: Yiwen Wang

* 1️⃣ CLI sends a RunPipelineRequest to PipelineController (/api/v1/pipelines/run).
* 2️⃣ PipelineExecutionService:
  - Fetches pipelineEntity definition from PipelineRepository
  - Creates a new PipelineExecution entry in PipelineExecutionRepository
  - Calls PipelineExecutor to run the pipelineEntity.
* 3️⃣ PipelineExecutor:
  - Updates execution state (RUNNING → SUCCESS or FAILED).
  - Saves progress to PipelineExecutionRepository.
* 4️⃣ API response is sent back to CLI.
