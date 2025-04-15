# 25Spring CS6510 Team 1 - CI/CD System

- **Title:** Requirements Summary
- **Date:** Feb 27, 2025
- **Author:** Yiwen Wang
- **Version:** 1.0

**Revision History**

|     Date     | Version |      Description       |   Author   |
| :----------: | :-----: | :--------------------: | :--------: |
| Feb 27, 2025 |   1.1   | Summary of the Project | Yiwen Wang |

# Summary of CI/CD System Requirements

Based on both requirement documents, the client wants a custom CI/CD system with the following key features:

- **Local-first approach:** The system must run both on company data centers and developers' local machines without modifications.
- **Git-based configuration:** All pipelineEntity configurations reside in a `.pipelines` directory within the repository.
- **Independent pipelineEntity configurations:** Each configuration file is self-contained and defines a complete pipelineEntity with a unique name.

### Flexible pipelineEntity structure:

- Pipelines are organized into sequential stageEntities.
- Stages contain jobEntities that can run in parallel.
- Jobs can have dependencies but cannot form cycles.
- Jobs specify Docker images for execution environments.

### Powerful CLI tool that allows developers to:

- Validate configuration files.
- Execute dry-runs to preview execution order.
- Run pipelines locally or remotely.
- Get detailed reports on past pipelineEntity executions.
- Override configuration values.
- Specify repos, branches, and commits.

### Job flexibility:

- Jobs can be configured to continue pipelineEntity execution even after failure.
- Jobs can specify artifacts to upload upon completion.
- Artifact paths can use patterns for selection.

### Comprehensive reporting system to track pipelineEntity runs, with status tracking for pipelines, stageEntities, and jobEntities.

## What Are They Trying to Build?

In plain language, the client wants a flexible, developer-friendly CI/CD system that works the same way locally and in production.

They're trying to solve a common developer frustration: *"If it works on my machine, why does it fail in CI?"* Their solution is to create a system where developers can run the exact same CI/CD pipelines locally that would run on the server, using identical configurations and Docker environments.

This allows developers to:

- Debug failing pipelines locally.
- Test changes to CI/CD configurations before committing them.
- Understand exactly how their code will be built and tested.
- Have greater control over their development workflow.

It's like having a portable, standardized test environment that follows the same rules everywhere it runs. This gives developers more confidence in their code and reduces the *"works on my machine"* problem while still maintaining the benefits of automated testing and deployment.