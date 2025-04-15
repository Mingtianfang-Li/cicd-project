# 25Spring CS6510 Team 1 - CI/CD System - Phase 1 Journal 
- Version: 1.0
- Updated with the Project requirements by Feb 28, 2025
- Author: Yiwen Wang
## Phase 1: Core Infrastructure - Minimal Viable Product (MVP)

1. CLI Module (completed)
   - Implement a simple command-line interface.
   - Support commands to run pipelines locally.
   - Provide basic logs for execution.
2. Backend Module (completed)
   - Implement basic API endpoints for triggering pipelineEntity execution.
   - Provide local execution support without external dependencies.
3. Worker Module(completed)
   - Implement jobEntity execution logic.
   - Enable jobEntity isolation using Docker.
4. Reporting(completed)
   - Store basic execution logs. 
   - Generate simple reports for pipelineEntity execution.

* **Key Validation Step:** (End to End integration is not completed, other completed)
      - Ensure a **basic CI/CD pipelineEntity runs successfully** end-to-end using **CLI + Backend + Worker.**
      - Use unit and integration tests to validate.


## Feb 28, 2025
1. completed 1-4 with unit testing, validated integration for: backend-cli, backend-worker
2. TODO: integration test for end to end. 