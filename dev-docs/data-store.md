# Data Storage Design Document

**Author**: Wenxue Fang, Team 1, CS6510 Spring 2025
**Date**: April 13, 2025

## Overview

This document describes the data storage architecture for the CI/CD Pipeline System. The system uses PostgreSQL as its primary data store to manage pipeline definitions, execution history, and logs.

## Database Configuration

- **Database Engine**: PostgreSQL
- **Configuration Location**: `backend/src/main/resources/application.yml`
- **Migration Scripts**: `backend/src/main/resources/db/migration/V1__init.sql`
- **Connection URL**: `jdbc:postgresql://postgres:5432/cicd_db`

## Entity Relationship Diagram

```
Pipeline 1──┐
  │         │
  │         ↓
  ├───► Stage 1───┐
  │      │        │
  │      │        ↓
  │      ├───► Job 1 ◄─┐
  │      │        │    │
  │      │        │    │ dependencies
  │      │        ↓    │
  ↓      ↓      Job 2 ─┘
PipelineExecution
  │
  ├───► StageExecution
  │       │
  │       ├───► JobExecution
  │       │        │
  │       │        ↓
  │       │     ExecutionLog
  ↓       ↓
ExecutionLog ExecutionLog
```

## Database Schema

### Pipeline Management

#### Pipeline
```sql
CREATE TABLE pipelines (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    repository_url VARCHAR(1024) NOT NULL,
    branch VARCHAR(255) DEFAULT 'main',
    commit_hash VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_pipeline_name ON pipelines(name);
CREATE INDEX idx_pipeline_repo ON pipelines(repository_url);
```

#### Stage
```sql
CREATE TABLE stages (
    id UUID PRIMARY KEY,
    pipeline_id UUID NOT NULL REFERENCES pipelines(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    execution_order INT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_pipeline_stages ON stages(pipeline_id);
```

#### Job
```sql
CREATE TABLE jobs (
    id UUID PRIMARY KEY,
    stage_id UUID NOT NULL REFERENCES stages(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    docker_image VARCHAR(255) NOT NULL,
    allow_failure BOOLEAN DEFAULT FALSE,
    working_dir VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_stage_jobs ON jobs(stage_id);
```

#### Job Dependencies
```sql
CREATE TABLE job_dependencies (
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    depends_on_job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    PRIMARY KEY (job_id, depends_on_job_id)
);

CREATE INDEX idx_job_dependencies_job ON job_dependencies(job_id);
CREATE INDEX idx_job_dependencies_depends_on ON job_dependencies(depends_on_job_id);
```

#### Job Scripts
```sql
CREATE TABLE job_scripts (
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    script TEXT NOT NULL,
    script_order INT NOT NULL,
    PRIMARY KEY (job_id, script_order)
);

CREATE INDEX idx_job_scripts_job_id ON job_scripts(job_id);
```

#### Job Artifacts
```sql
CREATE TABLE job_artifacts (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    artifact_path VARCHAR(1024) NOT NULL
);

CREATE INDEX idx_job_artifacts_job_id ON job_artifacts(job_id);
```

### Execution Tracking

#### Pipeline Execution
```sql
CREATE TABLE pipeline_executions (
    id UUID PRIMARY KEY,
    pipeline_id UUID NOT NULL REFERENCES pipelines(id) ON DELETE CASCADE,
    run_number INT NOT NULL,
    commit_hash VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP,
    completion_time TIMESTAMP,
    is_local BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP
);

CREATE UNIQUE INDEX unique_pipeline_run ON pipeline_executions(pipeline_id, run_number);
CREATE INDEX idx_pipeline_executions ON pipeline_executions(pipeline_id, run_number, commit_hash, is_local);
CREATE INDEX idx_pipeline_executions_status ON pipeline_executions(status);
```

#### Stage Execution
```sql
CREATE TABLE stage_executions (
    id UUID PRIMARY KEY,
    pipeline_execution_id UUID NOT NULL REFERENCES pipeline_executions(id) ON DELETE CASCADE,
    stage_id UUID NOT NULL REFERENCES stages(id) ON DELETE CASCADE,
    execution_order INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP,
    completion_time TIMESTAMP,
    is_local BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP
);

CREATE INDEX idx_stage_executions ON stage_executions(pipeline_execution_id, stage_id, is_local);
CREATE INDEX idx_stage_executions_status ON stage_executions(status);
```

#### Job Execution
```sql
CREATE TABLE job_executions (
    id UUID PRIMARY KEY,
    stage_execution_id UUID NOT NULL REFERENCES stage_executions(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP,
    completion_time TIMESTAMP,
    allows_failure BOOLEAN DEFAULT FALSE,
    is_local BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP
);

CREATE INDEX idx_job_executions ON job_executions(stage_execution_id, job_id, is_local);
CREATE INDEX idx_job_executions_status ON job_executions(status);
CREATE INDEX idx_job_executions_start_time ON job_executions(start_time);
```

#### Execution Logs
```sql
CREATE TABLE execution_logs (
    id UUID PRIMARY KEY,
    pipeline_execution_id UUID REFERENCES pipeline_executions(id) ON DELETE CASCADE,
    stage_execution_id UUID REFERENCES stage_executions(id) ON DELETE CASCADE,
    job_execution_id UUID REFERENCES job_executions(id) ON DELETE CASCADE,
    log_text TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_execution_logs ON execution_logs(pipeline_execution_id, stage_execution_id, job_execution_id);
CREATE INDEX idx_execution_logs_timestamp ON execution_logs(timestamp);
```

## JPA Entities

### Entity Classes

1. **PipelineEntity**: Represents a pipeline definition
   - Location: `backend/src/main/java/edu/neu/cs6510/sp25/t1/backend/database/entity/PipelineEntity.java`
   - Maps to `pipelines` table

2. **StageEntity**: Represents a stage within a pipeline
   - Location: `backend/src/main/java/edu/neu/cs6510/sp25/t1/backend/database/entity/StageEntity.java`
   - Maps to `stages` table

3. **JobEntity**: Represents a job within a stage
   - Location: `backend/src/main/java/edu/neu/cs6510/sp25/t1/backend/database/entity/JobEntity.java`
   - Maps to `jobs` table

4. **JobDependencyEntity**: Represents a dependency between jobs
   - Location: `backend/src/main/java/edu/neu/cs6510/sp25/t1/backend/database/entity/JobDependencyEntity.java`
   - Maps to `job_dependencies` table

5. **PipelineExecutionEntity**: Tracks pipeline execution instance
   - Location: `backend/src/main/java/edu/neu/cs6510/sp25/t1/backend/database/entity/PipelineExecutionEntity.java`
   - Maps to `pipeline_executions` table

6. **StageExecutionEntity**: Tracks stage execution instance
   - Location: `backend/src/main/java/edu/neu/cs6510/sp25/t1/backend/database/entity/StageExecutionEntity.java`
   - Maps to `stage_executions` table

7. **JobExecutionEntity**: Tracks job execution instance
   - Location: `backend/src/main/java/edu/neu/cs6510/sp25/t1/backend/database/entity/JobExecutionEntity.java`
   - Maps to `job_executions` table

8. **ExecutionLogEntity**: Stores execution logs
   - Location: `backend/src/main/java/edu/neu/cs6510/sp25/t1/backend/database/entity/ExecutionLogEntity.java`
   - Maps to `execution_logs` table

### Repository Interfaces

1. **PipelineRepository**: Repository for pipeline operations
   - Location: `backend/src/main/java/edu/neu/cs6510/sp25/t1/backend/database/repository/PipelineRepository.java`

2. **StageRepository**: Repository for stage operations
   - Location: `backend/src/main/java/edu/neu/cs6510/sp25/t1/backend/database/repository/StageRepository.java`

3. **JobRepository**: Repository for job operations
   - Location: `backend/src/main/java/edu/neu/cs6510/sp25/t1/backend/database/repository/JobRepository.java`

4. **JobScriptRepository**: Repository for job script operations
   - Location: `backend/src/main/java/edu/neu/cs6510/sp25/t1/backend/database/repository/JobScriptRepository.java`

5. **PipelineExecutionRepository**: Repository for pipeline execution operations
   - Location: `backend/src/main/java/edu/neu/cs6510/sp25/t1/backend/database/repository/PipelineExecutionRepository.java`

6. **StageExecutionRepository**: Repository for stage execution operations
   - Location: `backend/src/main/java/edu/neu/cs6510/sp25/t1/backend/database/repository/StageExecutionRepository.java`

7. **JobExecutionRepository**: Repository for job execution operations
   - Location: `backend/src/main/java/edu/neu/cs6510/sp25/t1/backend/database/repository/JobExecutionRepository.java`

## Data Flow

1. **Pipeline Creation**:
   - Pipeline YAML file is parsed into a Pipeline model
   - Models are mapped to entities and persisted to database
   - Stages, jobs, and their relationships are stored in respective tables

2. **Pipeline Execution**:
   - When a pipeline is executed, a new PipelineExecution record is created
   - As stages and jobs run, their execution records are created and updated
   - Logs are continuously written to the ExecutionLog table
   - Status updates flow from JobExecution to StageExecution to PipelineExecution

3. **Report Generation**:
   - Reports are generated by querying execution records
   - JOIN operations link pipeline definitions with execution history
   - Execution logs are retrieved for detailed reporting

## Data Persistence Strategy

1. **Transactional Integrity**:
   - All database operations are wrapped in transactions
   - Uses Spring's `@Transactional` annotation for consistency

2. **Optimistic Locking**:
   - Uses versioning to prevent concurrent modification issues
   - JPA's `@Version` annotation on appropriate entities

3. **Data Validation**:
   - Enforces constraints at both application and database levels
   - Validation annotations on entity fields

4. **Audit Tracking**:
   - All entities track creation and update timestamps
   - Allows for historical analysis and troubleshooting

## Data Migration Strategy

1. **Schema Versioning**:
   - Uses Flyway for database migrations
   - Migration scripts in `backend/src/main/resources/db/migration/`
   - Versioned with V1__, V2__, etc. prefix

2. **Backward Compatibility**:
   - Migrations designed to be non-destructive
   - New features add columns rather than modifying existing ones when possible

3. **Rollback Support**:
   - Critical migrations include rollback scripts
   - Enables recovery from failed migrations

## Database Indexing Strategy

The database schema includes a comprehensive indexing strategy to optimize query performance:

1. **Name-Based Lookup Indexes**:
   - `idx_pipeline_name` on `pipelines(name)` for pipeline name searches
   - `idx_pipeline_repo` on `pipelines(repository_url)` for repository lookups

2. **Relationship Indexes**:
   - `idx_pipeline_stages` on `stages(pipeline_id)` for finding stages in a pipeline
   - `idx_stage_jobs` on `jobs(stage_id)` for finding jobs in a stage
   - `idx_job_dependencies_job` and `idx_job_dependencies_depends_on` for efficient traversal of job dependencies

3. **Compound Indexes for Execution Queries**:
   - `idx_pipeline_executions` on `pipeline_executions(pipeline_id, run_number, commit_hash, is_local)` 
   - `idx_stage_executions` on `stage_executions(pipeline_execution_id, stage_id, is_local)`
   - `idx_job_executions` on `job_executions(stage_execution_id, job_id, is_local)`

4. **Status Indexes for Filtering**:
   - `idx_pipeline_executions_status` on `pipeline_executions(status)`
   - `idx_stage_executions_status` on `stage_executions(status)`
   - `idx_job_executions_status` on `job_executions(status)`

5. **Temporal Indexes for Sorting**:
   - `idx_job_executions_start_time` on `job_executions(start_time)`
   - `idx_execution_logs_timestamp` on `execution_logs(timestamp)`

6. **Composite Indexes for Log Retrieval**:
   - `idx_execution_logs` on `execution_logs(pipeline_execution_id, stage_execution_id, job_execution_id)`

7. **Unique Constraints**:
   - `unique_pipeline_run` on `pipeline_executions(pipeline_id, run_number)` to ensure run numbers are unique per pipeline

These indexes are defined at the database level and reflected in JPA entity annotations, ensuring consistent application across all environments.

## Scaling Considerations

1. **Query Performance Optimization**:
   - Uses pagination for large result sets (e.g., execution logs, historical records)
   - Optimized JPQL queries for complex operations
   - Strategic use of join fetching to reduce N+1 query problems

2. **Connection Pooling**:
   - HikariCP for efficient connection management
   - Configurable pool size based on deployment environment
   - Connection timeout and maximum lifetime settings to prevent connection leaks

3. **Data Partitioning**:
   - Execution logs table can be partitioned by timestamp for efficient storage
   - Pipeline executions could be partitioned by pipeline or date range

4. **Data Archiving**:
   - Long-term strategy includes archiving old execution records to maintain performance
   - Archive tables with the same structure but separate indexing strategy
   - Retention policies based on pipeline importance and regulatory requirements