# **Git Repository Detection and Pipeline Configuration Validation in CI/CD CLI**

**Author**: Yiwen Wang
 **Version**: 1.0
 **Last Updated**: Mar 4, 2025

------

## **1. Ensuring the Directory is a Git Repository**

The CLI must verify that it is running inside a **valid Git repository** by performing the following checks:

### **1.1. Verify if Inside a Git Repository**

```sh
git rev-parse --is-inside-work-tree
```

- **If `true`** → The directory is a valid Git repository.
- **If error** → CLI must exit with an error message.

### **1.2. Ensure the Repository Contains a `.git` Directory**

```sh
test -d .git
```

- **If `.git` folder is missing** → Return an error.

### **1.3. Ensure the `.pipelines/` Folder Exists**

```sh
test -d .pipelines
```

- **If `.pipelines/` is missing** → Return an error: *"No pipeline configuration found."*

------

## **2. Handling Default Values**

### **2.1. Default Repository Location**

- If `--repo` is **not provided**, assume **current working directory**.
- If **not in a Git repository**, return an **error**.

### **2.2. Default Git Branch**

- If `--branch` is **not provided**, fetch the branch:

```sh
git symbolic-ref --short HEAD
```

- If command fails, assume default: **`main`**.

### **2.3. Default Git Commit Hash**

- If `--commit` is **not provided**, fetch latest commit:

```sh
git rev-parse HEAD
```

- If no commits exist, return an **error**.

### **2.4. Fetch Remote URL for Remote Runs**

- If `--repo` is **not provided** (for remote execution), fetch:

```sh
git config --get remote.origin.url
```

- If no remote exists, return an **error**.

------

## **3. Pipeline Configuration Validation**

### **3.1. YAML Parsing & Syntax Validation**

- Use **Jackson YAMLMapper** for parsing.
- Ensure file is valid **YAML v1.2**.
- Return error with **file, line, and column number** if parsing fails.
- Default pipeline file: `.pipelines/pipeline.yaml` (unless `--file` is provided).

### **3.2. Pipeline-Level Validation**

| Key        | Requirement                                       |
| ---------- | ------------------------------------------------- |
| `pipeline` | Root key must be **`"pipeline"`** (Required)      |
| `name`     | Unique pipeline name within repository (Required) |
| `stages`   | List of **at least one stage** (Required)         |

**Errors:**

- `"Error: Missing 'pipeline' root key"`
- `"Error: Pipeline name is required"`
- `"Error: Duplicate pipeline name found"`
- `"Error: At least one stage must be defined"`

### **3.3. Stage-Level Validation**

| Key                    | Requirement                              |
| ---------------------- | ---------------------------------------- |
| **At least one stage** | Must have at least **one stage** defined |
| **Execution Order**    | Stages execute **in the order listed**   |

**Errors:**

- `"Error: No stages defined in the pipeline"`
- `"Error: Stage order does not match job dependencies"`

### **3.4. Job-Level Validation**

| Key      | Requirement                                      |
| -------- | ------------------------------------------------ |
| `name`   | Unique within the stage (Required)               |
| `stage`  | Must match a **defined stage** (Required)        |
| `image`  | Docker image used for execution (Required)       |
| `script` | YAML list of **at least one command** (Required) |

**Example Job Configuration:**

```yaml
jobs:
  - name: build
    stage: build
    image: gradle:jdk8
    script:
      - ./gradlew build
```

**Errors:**

- `"Error: Missing required fields (name, stage, image, script)."`
- `"Error: Job references undefined stage."`
- `"Error: 'script' must contain at least one command"`

### **3.5. Dependency & Execution Order Validation**

| Validation                     | Requirement                           |
| ------------------------------ | ------------------------------------- |
| **Job Dependencies (`needs`)** | Must be a valid list of existing jobs |
| **No Cyclic Dependencies**     | Detect cycles in job execution order  |

**Example Dependency Configuration:**

```yaml
jobs:
  - name: build
    stage: build
    image: gradle:jdk8
    script:
      - ./gradlew build

  - name: test
    stage: test
    image: gradle:jdk8
    script:
      - ./gradlew test
    needs:
      - build
```

**Errors:**

- `"Error: Job 'test' references missing dependency 'deploy'"`
- `"Error: Cyclic dependency detected: build → test → build"`

------

## **4. Java Implementation**

### **4.1. Git Repository Validation (`GitUtils.java`)**

```java
import java.io.*;
import java.nio.file.*;

public class GitUtils {
    public static boolean isInsideGitRepo() {
        return executeShellCommand("git rev-parse --is-inside-work-tree").equals("true");
    }
    public static boolean hasPipelinesFolder() {
        return Files.exists(Paths.get(".pipelines"));
    }
    public static String getCurrentBranch() {
        String branch = executeShellCommand("git symbolic-ref --short HEAD");
        return branch.isEmpty() ? "main" : branch;
    }
    public static String getLatestCommitHash() {
        return executeShellCommand("git rev-parse HEAD");
    }
    public static String getRemoteUrl() {
        return executeShellCommand("git config --get remote.origin.url");
    }
    private static String executeShellCommand(String command) {
        try {
            Process process = new ProcessBuilder("bash", "-c", command).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine().trim();
        } catch (IOException e) {
            return "";
        }
    }
    public static void validateRepo() {
        if (!isInsideGitRepo()) {
            throw new IllegalStateException("Error: Not inside a valid Git repository.");
        }
        if (!hasPipelinesFolder()) {
            throw new IllegalStateException("Error: Missing .pipelines directory.");
        }
    }
}
```

------

## **5. Conclusion**

This document provides: ✅ **Git repository validation**
 ✅ **Pipeline YAML parsing**
 ✅ **Stage & Job validation**
 ✅ **Dependency checks**
 ✅ **Error reporting format**

