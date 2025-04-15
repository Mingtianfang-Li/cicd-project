# 25Spring CS6510 Team 1 - CI/CD System
- **Title:** Team Process file
- **Date:** Jan 31, 2025
- **Author:** Yiwen Wang, Wenxue Fang
- **Version:** 1.0

**Revision History**
|Date|Version|Description|Author|
|:----:|:----:|:----:|:----:|
|Jan 31, 2025|1.0|Initial release| Yiwen Wang, Wenxue Fang|

# Team Processes

## Deliverables

This document outlines the team’s processes, including repository configuration, file organization, CI/CD setup, testing coverage, and code quality standards.

---

## Repository Configuration

The project is structured across multiple repositories to ensure modular development and maintainability.

### **Repositories**

| **Component** | **Description**                                              |
| ------------- | ------------------------------------------------------------ |
| **CLI**       | Command-line interface for running pipelines locally and on servers. |
| **Backend**   | Java Spring Boot service for handling CI/CD operations.      |
| **Worker**    | Job execution service for running and managing CI/CD tasks.  |

### **File & Folder Organization**

Each repository follows a structured file organization:

```
repo-root/
│── src/                # Source code
│── tests/              # Unit and integration tests
│── docs/               # Documentation
│── scripts/            # Deployment and automation scripts
│── .github/            # CI/CD configurations
│── README.md           # Project description
│── .gitignore          # Ignored files
│── Dockerfile          # Containerization instructions
│── package.json/pom.xml# Dependencies management
```

---

## CI/CD Setup

### **Continuous Integration (CI)**

Each repository has an automated CI process that includes:

1. **Code Review** - Every pull request requires review before merging.
2. **Style Check** - Code is checked against Google Style (Lint).
3. **Unit Tests** - Unit tests are run to ensure correctness.
4. **JavaDoc** - Documentation is generated for Java code.
5. **Analysis Reports** - Static analysis tools ensure code quality.
6. **Merge to Branch** - Successful checks are required before merging.

### **Continuous Deployment (CD)**

1. **Auto Build** - Builds are triggered automatically with version control tagging.
2. **Publish to Docker** - Docker images are created and published.
3. **Publish to GitHub** - Releases are published to GitHub.
4. **Testing Coverage** - Minimum **70% test coverage** is required.
5. **Code Style** - Enforced using **Google Java Style (Lint)**.
6. **Static Analysis** - Uses **PMD** for static code analysis.

---

## **Code Quality Standards**

| **Aspect**           | **Standard**                                    |
| -------------------- | ----------------------------------------------- |
| **Testing Coverage** | Minimum **70%** unit test coverage.             |
| **Code Style**       | Google Java Style (Lint).                       |
| **JavaDoc**          | Required for all public methods and classes.    |
| **Static Analysis**  | Enforced using **PMD** to detect common issues. |

---

## **Next Steps**

- Implement automated **CI/CD pipelines** for all repositories.
- Enforce **code style and static analysis** as part of CI workflows.
- Regularly review and improve **test coverage**.

---

This document defines the development processes and quality standards for the CI/CD system.