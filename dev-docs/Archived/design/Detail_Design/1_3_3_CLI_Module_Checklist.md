# CLI Module Specification

## Overview

The CLI module is designed to manage, execute, and retrieve reports of CI/CD pipelines efficiently.
It follows standard best practices and supports argument formatting, sub-commands, and structured output (plain text, JSON, YAML).

## **CLI Commands and Flags**

### 1. **Run Command (`run`)**

Executes a CI/CD pipeline based on the provided parameters.

#### **Usage Examples**

##### **Remote Repository Pipelines**

1. Running a pipeline from a remote repository (default `pipeline.yaml` file):

   ```sh
   xx run --repo https://github.com/company/project --pipeline demo-ci-pipeline
   ```

2. Running a pipeline from a remote repository with a specific pipeline file:

   ```sh
   xx run --repo https://github.com/company/project --file .pipelines/test-my-cicd-pipeline.yaml
   ```

##### **Local (Own) Repository Pipelines**

1. Running a pipeline from a local repository (default `pipeline.yaml` file):

   ```sh
   xx run --local --repo /home/user/workspace --pipeline my-cicd-pipeline
   ```

2. Running a pipeline from a local repository with a specific pipeline file:

   ```sh
   xx run --local --repo /home/user/workspace --file pipelines/my-cicd-pipeline.yaml
   ```

##### **Other Variations**

- Running a pipeline at a specific branch and commit:

  ```sh
  xx run --repo https://github.com/company/project --branch main --commit 3df7142 --pipeline demo-ci-pipeline
  ```

- Running a local pipeline with overridden configurations:

  ```sh
  xx run --local --override "global.docker.image=gradle:jdk8" --file .pipelines/test-my-cicd-pipeline.yaml
  ```

### 2. **Check Command (`check`)**

Validates a pipeline configuration file without executing it.

#### **Usage Examples**

- Checking the default configuration file:

  ```sh
  xx check
  ```

- Checking a specific configuration file:

  ```sh
  xx check --file .pipelines/test-my-cicd-pipeline.yaml
  ```

### 3. **Dry Run Command (`dry-run`)**

Displays the execution order of a pipeline, including all stages and jobs.

#### **Usage Examples**

- Performing a dry run for a remote repository:

  ```sh
  xx dry-run --repo https://github.com/company/project --pipeline demo-ci-pipeline
  ```

- Performing a dry run for a local repository:

  ```sh
  xx dry-run --local --repo /home/user/workspace --pipeline my-cicd-pipeline
  ```

### 4. **Report Command (`report`)**

Retrieves details about past pipeline executions.

#### **Usage Examples**

- Listing all pipelines with past executions:

  ```sh
  xx report --repo https://github.com/company/project
  ```

- Retrieving a summary of past executions for a specific pipeline:

  ```sh
  xx report --repo https://github.com/company/project --pipeline demo-ci-pipeline
  ```

- Fetching execution details for a specific run:

  ```sh
  xx report --repo https://github.com/company/project --pipeline demo-ci-pipeline --run 2
  ```

- Retrieving stage-level execution details:

  ```sh
  xx report --repo https://github.com/company/project --pipeline demo-ci-pipeline --stage build
  ```

- Fetching job execution details within a stage:

  ```sh
  xx report --repo https://github.com/company/project --pipeline demo-ci-pipeline --stage build --job compile
  ```

## **Command Flags and Their Usage**

| Flag               | Description                                                | Required                                                |
| ------------------ | ---------------------------------------------------------- | ------------------------------------------------------- |
| `--repo` / `-r`    | Specifies the repository (remote URL or local path)        | ✅ (Mandatory unless running in a Git-tracked directory) |
| `--pipeline`       | Specifies the pipeline name to execute                     | ✅ (Mandatory unless using `--file`)                     |
| `--file` / `-f`    | Specifies the pipeline configuration file path             | ✅ (Mandatory unless using `--pipeline`)                 |
| `--branch` / `-br` | Specifies the Git branch to use (defaults to `main`)       | ❌                                                       |
| `--commit` / `-co` | Specifies the Git commit hash (defaults to latest commit)  | ❌                                                       |
| `--local`          | Specifies that execution should occur on the local machine | ❌                                                       |
| `--override`       | Overrides specific pipeline configuration keys             | ❌                                                       |
| `--check`          | Checks the validity of a pipeline configuration file       | ❌                                                       |
| `--dry-run`        | Simulates execution, showing the order of jobs and stages  | ❌                                                       |

## **Error Handling**

1. **Invalid YAML Configuration:**

   ```
   pipeline.yaml:10:22: syntax error, expected a string but found an integer.
   ```

2. **Circular Dependencies:**

   ```
   Error: Detected circular dependency between jobs: compile -> test -> compile.
   ```

3. **Conflicting Flags:**

   ```
   Error: --pipeline and --file cannot be used together.
   ```

