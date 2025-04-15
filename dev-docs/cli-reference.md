# CI/CD CLI Reference

**Author**: Wenxue Fang, Team 1, CS6510 Spring 2025
**Date**: April 13, 2025

## Overview

The CI/CD CLI tool provides a command-line interface for running and managing CI/CD pipelines. It supports validating pipeline configurations, running pipelines, checking execution status, and generating reports.

## Global Options

These options are available for all commands:

- `--repo, -r` - Specify the repository (local path or HTTPS URL). Defaults to the current directory.
- `--branch, -br` - Specify the Git branch. Defaults to 'main'.
- `--commit, -co` - Specify the commit hash. Defaults to the latest commit.
- `--pipeline, -p` - Specify the pipeline name to run.
- `--local` - Run the pipeline locally.
- `--vv, --verbose` - Enable verbose output (detailed logs).
- `--help, -h` - Show help message for the command.

## Main Command

```
cicd-cli [OPTIONS] [COMMAND]
```

Display basic CLI information:
```
cicd-cli 

CI/CD CLI - Ready! Use `--help` for available commands.
```

Get help on available commands:
```
cicd-cli --help

A command-line interface for running and managing CI/CD pipelines.
  -br, --branch=<branch>   Specify the Git branch. Defaults to 'main'.
  -co, --commit=<commit>   Specify the commit hash. Defaults to the latest commit.
  -h, --help               Show this help message and exit.
      --local              Run the pipeline locally.
  -p, --pipeline=<pipeline>
                           Specify the pipeline name to run.
  -r, --repo=<repo>        Specify the repository (local path or HTTPS URL). Defaults to the
                             current directory.
      --vv, --verbose      Enable verbose output (detailed logs).
Commands:
  check     Validates a pipeline configuration file without running it.
  run       Runs a CI/CD pipeline after validating its configuration.
  report    Retrieves pipeline execution reports from the CI/CD backend.
  dry-run   Validates a pipeline file and prints execution order in YAML format.
  status    Fetches the status of a CI/CD pipeline.
```

## Commands

### check

Validates a pipeline configuration file without running it.

```
cicd-cli check [OPTIONS]
```

Options:
- `--file, -f` - Path to the pipeline YAML configuration file. (Required)
- `--verbose` - Enable verbose output.

Example:
```
cicd-cli check --file .pipelines/my-cicd-pipeline.yaml
```

Output:
```
Checking pipeline configuration: .pipelines/my-cicd-pipeline.yaml
Pipeline configuration is valid!
```

### run

Runs a CI/CD pipeline after validating its configuration.

```
cicd-cli run [OPTIONS]
```

Options:
- `--repo, -r` - Specify the repository URL.
- `--branch, -b` - Specify the Git branch. Defaults to 'main'.
- `--commit, -c` - Specify the commit hash. Defaults to latest commit.
- `--file, -f` - Specify the pipeline configuration file.
- `--pipeline, -p` - Specify the pipeline name to run.
- `--local` - Run the pipeline locally.

Example:
```
cicd-cli run --repo https://github.com/user/repo.git --branch develop
```

Output:
```
Pipeline execution started.
Response: {
  "id": "12345",
  "status": "RUNNING",
  "message": "Pipeline execution started successfully"
}
```

### status

Fetches the status of a CI/CD pipeline.

```
cicd-cli status [OPTIONS]
```

Options:
- `--pipeline, -p` - Specify the pipeline name. (Required)

Example:
```
cicd-cli status --pipeline my-pipeline
```

Output:
```
Fetching pipeline status for: my-pipeline
Pipeline Status:
{
  "id": "12345",
  "name": "my-pipeline",
  "status": "RUNNING",
  "startTime": "2025-04-12T10:15:30Z"
}
```

### report

Retrieves pipeline execution reports from the CI/CD backend.

```
cicd-cli report [OPTIONS]
```

Options:
- `--pipeline, -p` - Specify the pipeline name. (Required)
- `--run` - Specify the run number for a detailed report.
- `--stage` - Specify the stage name for a detailed report.
- `--job` - Specify the job name for a detailed report.
- `--format` - Output format (text, json). Defaults to 'text'.

Examples:

Fetch pipeline history:
```
cicd-cli report --pipeline my-pipeline
```

Fetch a specific run:
```
cicd-cli report --pipeline my-pipeline --run 5
```

Fetch a specific stage:
```
cicd-cli report --pipeline my-pipeline --run 5 --stage build
```

Fetch a specific job:
```
cicd-cli report --pipeline my-pipeline --run 5 --stage build --job compile
```

Output (text format):
```
Pipeline ID: 12345
Pipeline Name: my-pipeline
Run Number: 5
Commit Hash: 7a8b9c0d1e2f3g4h5i6j7k8l9m0n1o2p3q4r5s6t
Status: SUCCESS
Start Time: 2025-04-12 10:15:30
Completion Time: 2025-04-12 10:25:45

Stages:
  Stage ID: 123
  Stage Name: build
  Status: SUCCESS
  Start Time: 2025-04-12 10:15:30
  Completion Time: 2025-04-12 10:20:15
  Jobs:
    Job Name: compile
    Executions:
      Execution ID: 456
      Status: SUCCESS
      Allow Failure: false
      Start Time: 2025-04-12 10:15:30
      Completion Time: 2025-04-12 10:20:15
```

### dry-run

Validates a pipeline file and prints execution order in YAML format.

```
cicd-cli dry-run [OPTIONS]
```

Options:
- `--file, -f` - Path to the pipeline YAML configuration file. (Required)

Example:
```
cicd-cli dry-run --file .pipelines/my-cicd-pipeline.yaml
```

Output:
```
Validating pipeline configuration: .pipelines/my-cicd-pipeline.yaml

Execution Plan:
build:
  compile:
    image: maven:3.8-openjdk-11
    script:
    - mvn compile
test:
  unit-test:
    image: maven:3.8-openjdk-11
    script:
    - mvn test
deploy:
  push:
    image: docker:20.10
    script:
    - docker build -t myapp .
    - docker push myapp:latest
```

## Error Handling

All commands return exit code 0 for success and 1 for failure. When a command fails, error messages are displayed to help diagnose the issue:

```
Error: Specified pipeline file does not exist: non-existent.yaml
```

```
Failed to communicate with backend: Connection refused
```

For the `run` command, the CLI will explicitly output "success" or "failed" based on the exit code.