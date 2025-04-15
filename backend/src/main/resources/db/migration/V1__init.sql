--  This file contains the SQL script to create the initial schema for the database.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Drop tables in the correct dependency order
DROP TABLE IF EXISTS execution_logs CASCADE;
DROP TABLE IF EXISTS artifacts CASCADE;
DROP TABLE IF EXISTS job_executions CASCADE;
DROP TABLE IF EXISTS stage_executions CASCADE;
DROP TABLE IF EXISTS pipeline_executions CASCADE;
DROP TABLE IF EXISTS job_dependencies CASCADE;
DROP TABLE IF EXISTS job_scripts CASCADE;
DROP TABLE IF EXISTS jobs CASCADE;
DROP TABLE IF EXISTS stages CASCADE;
DROP TABLE IF EXISTS pipelines CASCADE;

-- Table for storing pipeline definitions
CREATE TABLE IF NOT EXISTS pipelines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    repository_url TEXT NOT NULL,
    branch VARCHAR(255) DEFAULT 'main',
    commit_hash VARCHAR(40) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- Table for storing stage definitions
CREATE TABLE IF NOT EXISTS stages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    execution_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pipeline_id) REFERENCES pipelines(id) ON DELETE CASCADE
    );

-- Table for storing job definitions
CREATE TABLE IF NOT EXISTS jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stage_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    docker_image VARCHAR(255) NOT NULL DEFAULT 'docker.io/library/alpine:latest',
    allow_failure BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stage_id) REFERENCES stages(id) ON DELETE CASCADE
    );

-- Table for storing job scripts
CREATE TABLE IF NOT EXISTS job_scripts (
    job_id UUID NOT NULL,
    script TEXT NOT NULL,
    PRIMARY KEY (job_id, script),
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
    );

-- Table for storing job dependencies
CREATE TABLE IF NOT EXISTS job_dependencies (
    job_id UUID NOT NULL,
    depends_on_job_id UUID NOT NULL,
    PRIMARY KEY (job_id, depends_on_job_id),
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (depends_on_job_id) REFERENCES jobs(id) ON DELETE CASCADE
    );

-- Table for storing job artifacts
CREATE TABLE IF NOT EXISTS job_artifacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL,
    artifact_path TEXT NOT NULL, -- Stores file or directory path
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
    );

-- Create a sequence for per-pipeline run numbers before table creation
CREATE SEQUENCE IF NOT EXISTS pipeline_run_seq START 1;

-- Table for storing pipeline executions
CREATE TABLE IF NOT EXISTS pipeline_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline_id UUID NOT NULL,
    run_number INT NOT NULL DEFAULT nextval('pipeline_run_seq'),
    commit_hash VARCHAR(40) NOT NULL,
    is_local BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED', 'CANCELED')),
                                                   start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completion_time TIMESTAMP DEFAULT NULL,
    FOREIGN KEY (pipeline_id) REFERENCES pipelines(id) ON DELETE CASCADE
);

-- Assign ownership of the sequence to ensure correct auto-incrementing
ALTER SEQUENCE pipeline_run_seq OWNED BY pipeline_executions.run_number;


-- Table for storing stage executions
CREATE TABLE IF NOT EXISTS stage_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline_execution_id UUID NOT NULL,
    stage_id UUID NOT NULL,
    execution_order INT NOT NULL,
    commit_hash VARCHAR(40) NOT NULL,
    is_local BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED', 'CANCELED')),
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completion_time TIMESTAMP DEFAULT NULL,
    FOREIGN KEY (pipeline_execution_id) REFERENCES pipeline_executions(id) ON DELETE CASCADE,
    FOREIGN KEY (stage_id) REFERENCES stages(id) ON DELETE CASCADE
    );



-- Table for storing job executions
CREATE TABLE IF NOT EXISTS job_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stage_execution_id UUID NOT NULL,
    job_id UUID NOT NULL,
    commit_hash VARCHAR(40) NOT NULL,
    is_local BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED', 'CANCELED')),
    allows_failure BOOLEAN NOT NULL DEFAULT FALSE,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completion_time TIMESTAMP DEFAULT NULL,
    FOREIGN KEY (stage_execution_id) REFERENCES stage_executions(id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
    );

-- Table for storing execution logs
CREATE TABLE IF NOT EXISTS execution_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline_execution_id UUID NULL,
    stage_execution_id UUID NULL,
    job_execution_id UUID NULL,
    log_text TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pipeline_execution_id) REFERENCES pipeline_executions(id) ON DELETE CASCADE,
    FOREIGN KEY (stage_execution_id) REFERENCES stage_executions(id) ON DELETE CASCADE,
    FOREIGN KEY (job_execution_id) REFERENCES job_executions(id) ON DELETE CASCADE
    );

-- Indexes for fast retrieval
CREATE INDEX idx_pipeline_name ON pipelines(name);
CREATE INDEX idx_pipeline_repo ON pipelines(repository_url);
CREATE INDEX idx_pipeline_executions ON pipeline_executions(pipeline_id, run_number, commit_hash, is_local);
CREATE INDEX idx_stage_executions ON stage_executions(pipeline_execution_id, stage_id, commit_hash, is_local);
CREATE INDEX idx_job_executions ON job_executions(stage_execution_id, job_id, commit_hash, is_local);
CREATE INDEX idx_execution_logs ON execution_logs(pipeline_execution_id, stage_execution_id, job_execution_id);
CREATE INDEX idx_job_scripts_job_id ON job_scripts(job_id);
CREATE INDEX idx_job_dependencies_job ON job_dependencies(job_id);
CREATE INDEX idx_job_dependencies_depends_on ON job_dependencies(depends_on_job_id);
CREATE INDEX idx_job_artifacts_job_id ON job_artifacts(job_id);
CREATE UNIQUE INDEX unique_pipeline_run ON pipeline_executions (pipeline_id, run_number);