# **Git Repository Detection and Default Handling in CI/CD CLI**
* **Author**: Yiwen Wang
* **Version**: 1.0
* **Last Updated**: Mar 4, 2025


## **Overview**
To ensure that a pipeline is inside a valid Git repository, the CLI must verify that the working directory is a Git repository, determine the repository details, and fetch default values when necessary. This document outlines the design for detecting a valid Git repository and handling default values for repository location, branch, and commit hash.

---

## **1. Ensuring the Directory is a Git Repository**
The CLI must verify that it is running inside a Git repository by performing the following checks:

1. **Verify that the current directory is inside a Git repository:**
   ```sh
   git rev-parse --is-inside-work-tree
   ```
    - If this returns `true`, the directory is a valid Git repository.
    - If it returns an error, the CLI must exit with an error message.

2. **Ensure the repository contains a `.git` directory:**
   ```sh
   test -d .git
   ```
    - If the `.git` folder does not exist in the root directory, return an error.

3. **Verify that the repository contains a `.pipelines/` folder:**
   ```sh
   test -d .pipelines
   ```
    - If the `.pipelines/` folder does not exist, return an error indicating that no pipeline configuration is found.

---

## **2. Handling Default Values**
When the user does not specify `--repo`, `--branch`, or `--commit`, the CLI must determine the appropriate default values:

### **2.1. Determining the Repository Location**
- If `--repo` is **not provided**, assume the **current working directory**.
- If the **current directory is not a Git repo**, return an **error**.

### **2.2. Determining the Git Branch**
- If `--branch` is **not provided**, determine the branch using:
  ```sh
  git symbolic-ref --short HEAD
  ```
    - If this command fails, assume `main` as the default branch.

### **2.3. Determining the Commit Hash**
- If `--commit` is **not provided**, retrieve the latest commit hash on the branch:
  ```sh
  git rev-parse HEAD
  ```
    - If no commits exist, return an **error**.

### **2.4. Fetching the Repository Remote URL (For Remote Runs)**
- If `--repo` is not provided and the execution requires a remote repository, fetch the origin URL:
  ```sh
  git config --get remote.origin.url
  ```
    - If no remote URL exists, return an **error**.

---

## **3. Implementing Default Handling in Java (Gradle-Based Project)**
The following Java class provides methods to detect a valid Git repository and fetch default values if not provided by the user.

```java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

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
            throw new IllegalStateException("Error: Missing .pipelines directory in the repository root.");
        }
    }
}
```

---

## **4. CLI Execution Flow for Running a Pipeline**

1. **Run Pipeline Command**
    - User executes: `xx run --pipeline build`
    - CLI starts processing.

2. **Check if Inside a Git Repository**
    - Calls `GitUtils.validateRepo();`
    - Ensures the working directory is a valid Git repo.
    - Ensures the `.pipelines/` folder exists.
    - If validation fails, execution stops with an error.

3. **Fetch Git Information**
    - If `--repo` is not provided, use the current directory.
    - If `--branch` is not provided, detect the current branch (`git symbolic-ref --short HEAD`, default: `main`).
    - If `--commit` is not provided, use the latest commit (`git rev-parse HEAD`).
    - If running remotely, fetch the remote URL (`git config --get remote.origin.url`).

4. **Parse and Validate the YAML Configuration**
    - Locate `.pipelines/pipeline.yaml` (or `--file` argument).
    - Parse the YAML file and validate its syntax.
    - Check for **dependency cycles** in job execution order.
    - If invalid, return an error and stop execution.

5. **Execute the Pipeline**
    - Start executing **stages in sequence**.
    - Run **jobs within stages**, respecting dependencies and parallel execution rules.
    - Handle **job failures**, considering `allow_failure` settings.
    - **Log execution status** for monitoring purposes.

6. **Store Execution Results**
    - Save pipeline run details for **reporting purposes**.
    - If successful, return a **success message**.
    - If failed, return an **error message** with logs.

---

## **5. Example CLI Execution Flow**
**Example Command (Without Defaults Provided):**
```sh
xx run --pipeline build
```
**Resolved Values Before Execution:**
```json
{
  "repo": "/home/user/my_project",
  "branch": "main",
  "commit": "a1b2c3d4e5"
}
```

---

## **6. Conclusion**
This document outlines how the CLI determines whether the current directory is a valid Git repository, fetches default values for missing parameters, and ensures that CI/CD configuration files exist in the `.pipelines/` directory. Additionally, it defines the full execution flow for running a pipeline, ensuring correctness and efficiency.

