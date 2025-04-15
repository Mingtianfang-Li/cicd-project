# Pipeline Validation Logic

- **Version:** 2.0
- **Updated:** March 2, 2025
- **Author:** Yiwen Wang
- **Description:** This document outlines the CI/CD pipelineEntity validation process, structured error reporting, and validation constraints.

## **1. Overview**

Pipeline validation ensures that the CI/CD configuration meets structural, dependency, and execution constraints before running. The validation system:

- Ensures correct YAML parsing with **exact line and column tracking**.
- Validates jobEntity dependencies and prevents **cyclic dependencies**.
- Uses structured **error reporting** in `<filename>:<line>:<error-message>` format.
- Uses **`ValidationException`** for handling errors across validators.

## **2. Validation Workflow**

1. **YAML Parsing**

  - Parses the pipelineEntity file into `PipelineConfig`.
  - Extracts **line numbers** using `YamlParser` for better error tracking.
  - Throws `ValidationException` if parsing fails.

2. **Pipeline Structure Validation**

  - Ensures required fields (`pipelineEntity`, `stageEntities`, `jobEntities`) exist.
  - Verifies that each stageEntity contains at least one jobEntity.

3. **Job Validation**

  - Ensures **jobEntity uniqueness** (no duplicate names).
  - Validates required jobEntity attributes: `name`, `stageEntity`, `image`, `script`.
  - Ensures **jobEntities reference existing stageEntities**.
  - Uses `YamlParser.getFieldLineNumber()` to **track field locations**.

4. **Dependency Validation**

  - Ensures jobEntities listed in `needs` reference **existing jobEntities**.

  - Detects **cyclic dependencies** using DFS (Depth-First Search).

  - Reports cycles in the format:

    ```
    pipelineEntity.yaml:12: Cyclic dependency detected: jobA -> jobB -> jobC -> jobA
    ```

5. **Final Validation & Error Handling**

  - Collects all validation errors.
  - Throws `ValidationException` if any issue is found.

## **3. Structured Error Reporting**

All errors are formatted as:

```
<filename>:<line>:<error-message>
```

**Example Errors:**

```
pipelineEntity.yaml:5: Job 'deploy' depends on non-existent jobEntity 'build'
pipelineEntity.yaml:10: Job 'test' must specify an image.
pipelineEntity.yaml:12: Cyclic dependency detected: jobA -> jobB -> jobC -> jobA
```

### **Error Handling in Code**

- **YAML Parsing Errors**

  ```java
  catch (YAMLException e) {
    int line = (e instanceof MarkedYAMLException markedE) ? markedE.getProblemMark().getLine() + 1 : 1;
    throw new ValidationException(yamlFile.getName(), line, "YAML parsing error: " + e.getMessage());
  }
  ```

- **Validation Errors**

  ```java
  if (jobEntity.getImage() == null || jobEntity.getImage().isEmpty()) {
    errors.add(formatError(filename, "image", "Job '" + jobEntity.getName() + "' must specify an image."));
  }
  ```

## **4. Key Classes in Validation**

| Class                   | Responsibility                                         |
| ----------------------- | ------------------------------------------------------ |
| `YamlParser`            | Parses YAML and extracts line numbers                  |
| `JobValidator`          | Validates individual jobEntities and required fields          |
| `PipelineValidator`     | Ensures pipelineEntity structure and detects cycles          |
| `YamlPipelineValidator` | Integrates all validators and runs the full validation |



