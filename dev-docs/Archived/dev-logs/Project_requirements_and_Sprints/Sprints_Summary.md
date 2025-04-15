# 25Spring CS6510 Team 1 - CI/CD System - Sprints Summary

## Sprint 1: Foundational Design

### **End Goal (Minimum Viable Deliverable)**
- Establish the high-level architecture of the CI/CD system.
- Define the tech stack.
- Establish team processes.
- Begin CLI development and configuration validation.

### **Tasks to be Completed**
#### **High-Level Architecture**
- Create `dev-docs/design/high-level-arch.md`.
- Include:
  - High-level architecture diagram with component responsibilities.
  - Sequence/interaction diagram explaining execution flow.
  - Coverage of:
    - Validating the configuration file.
    - Running a pipelineEntity.
    - Retrieving reports from past pipelineEntity runs.

#### **Tech Stack Proposal**
- Create `dev-docs/design/tech-stack.md`.
- Include:
  - List of chosen technologies (languages, frameworks, libraries, DBs).
  - Justifications for each choice.
  - Table with Pros & Cons of considered technologies.

#### **Team Processes**
- Create `dev-docs/team-process.md`.
- Define:
  - Repository configuration (structure, number of repos, organization).
  - CI/CD setup.
  - Testing coverage.
  - Code style.
  - Static analysis.

#### **CLI Implementation**
- Design and implement CLI client.
- Implement static validation for configuration files.
- Plan for demo:
  - Identify required implementations.
  - Create & assign issues.
  - Estimate issue weights.

---
## Sprint 2: CLI & YAML Configuration Processing

### **End Goal (Minimum Viable Deliverable)**
- CLI with the ability to process pipelineEntity configuration files.
- Validation of pipelineEntity YAML definitions.

### **Tasks to be Completed**
#### **CLI Enhancements**
- CLI must:
  - Be run from the root of a Git repository.
  - Support configuration files stored in `.pipelines/`.
  - Allow specifying config file via `--filename | -f`.
  - Provide `--check` option to validate config files without execution.

#### **Pipeline Configuration File Validation**
- YAML v1.2 format support.
- Validate keys:
  - `pipelineEntity` (required, includes `name`, `stageEntities`).
  - `stageEntities` (required, execution order defined by list order).
  - `jobEntity` (at least one per stageEntity, including `name`, `stageEntity`, `image`, `script`, `needs`).
- Enforce constraints:
  - Unique pipelineEntity names per repo.
  - At least one stageEntity defined.
  - Unique jobEntity names within a stageEntity.
  - No cyclic dependencies.
  - Valid `needs` references.

#### **Error Reporting**
- CLI must:
  - Report errors with `<filename>:<line-number>:<column-number>:<error-message>` format.
  - Detect cycles in jobEntity dependencies.
  - Identify type mismatches in configuration values.

---
## Sprint 3: Pipeline Execution

### **End Goal (Minimum Viable Deliverable)**
- CLI commands for dry-run and local execution.
- Sequential execution of jobEntities.

### **Tasks to be Completed**
#### **CLI Execution Commands**
- Implement:
  - `dry-run`: Simulates execution without running jobEntities.
  - `run`: Executes pipelineEntity locally.

#### **Execution Flow**
- No parallel execution (sequential processing only).
- Ensure `needs` dependencies are obeyed.

#### **Demo Preparation**
- Record a video demo including screen recording and voice-over.
- Store and share demo via Discord’s `#general` channel.

---
## Sprint 4: Basic Reporting

### **End Goal (Minimum Viable Deliverable)**
- CLI command for reporting past pipelineEntity runs.
- Basic pipelineEntity run summaries.

### **Tasks to be Completed**
#### **Reporting Implementation**
- Implement CLI `report` command.
- Generate:
  - Summary of pipelineEntity run for repository (L4.1, L4.2).

#### **Scope Considerations**
- Ignore:
  - `allow failures` feature.
  - Multiple configuration files per repo.
  - Job output storage & artifact uploads.

---
## Sprint 5: Advanced Reporting & Container Execution

### **End Goal (Minimum Viable Deliverable)**
- Extended reporting for stageEntity/jobEntity-level summaries.
- Pipeline execution inside Docker containers.
- Reports stored in a persistent data store.

### **Tasks to be Completed**
#### **Advanced Reporting**
- Extend CLI `report` command to include:
  - Stage-level pipelineEntity run summary (L4.3).
  - Job-level pipelineEntity run summary (L4.4).

#### **Container Execution**
- Refactor to replace mocked/shell escape Docker calls with proper Docker integration via:
  - Official language libraries (Go/Python).
  - Direct interaction with Docker Service REST API.

#### **Persistent Data Storage**
- Implement reports with:
  - SQL/NoSQL storage for historical access.

#### **Scope Considerations**
- Ignore:
  - `allow failures` feature.
  - Multiple configuration files per repo.
  - Job output storage & artifact uploads.

---


