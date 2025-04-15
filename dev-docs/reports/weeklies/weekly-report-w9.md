
# Week 9

# Completed tasks

> List completed (DONE) tasks, include
> 1. A link to the Issue
> 2. The total weight (points or T-shirt size) allocated to the issue

| Task                                                                                                                         | Weight |
|------------------------------------------------------------------------------------------------------------------------------| ------ |
| [Feature: Implement StatusService to Return Detailed Pipeline Status](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/248) |   2     |
| [Feature: Introduce MinIO integration into the worker service to store Docker job logs ](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/249)                              |   2     |
| [Enhance CLI Report Querying Capabilities](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/250)                            |   2     |

# Carry over tasks

> List all issues that were planned for this week but did not get DONE
> Include
> 1. A link to the Issue
> 2. The total weight (points or T-shirt size) allocated to the issue
> 3. The team member assigned to the task. This has to be 1 person!

| Task | Weight | Assignee |
| ---- | ------ | -------- |
| [](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/) | 0 | Name |

# New tasks

> List all the issues that the team is planning to work for next sprint
> Include
> 1. A link to the Issue
> 2. The total weight (points or T-shirt size) allocated to the issue
> 3. The team member assigned to the task. This has to be 1 person!

| Task | Weight | Assignee    |
| ---- |---|-------------|
| [Add retry logic for network failures in Docker executor](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/) | 2 | Wenxue Fang |
| [Add authentication/authorization middleware to backend](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/) | 2 | Wenxue Fang |

# What worked this week?

> Structured Backend Enhancements: Implementing StatusService greatly improved traceability by delivering detailed pipeline status reports. This helped with debugging and visualizing execution flows.
> Effective Logging: Integrating MinIO for Docker job logs centralized log access and decoupled log storage from the container lifecycle. Lightweight and easy to run locally.
> Flexible CLI Querying: Adding structured, multi-format report queries enhanced our team’s ability to quickly inspect historical pipeline executions. --format json was particularly useful for tooling integration.

# What did not work this week?

> MinIO not auto-started: MinIO must be manually launched with Docker every time, which caused issues during development and CI testing. We plan to improve this with automated local bootstrap or embedded test support.
> Slow MinIO Startup: MinIO takes a long time to start up, which slows down development and testing. We will explore ways to speed up MinIO startup time for better efficiency.
> Status aggregation in deploy stage: While StatusService correctly retrieves and aggregates pipeline execution data, we encountered cases where the deploy stage was marked as FAILED despite some jobs (like build-jar) succeeding. This inconsistency stems from partial job failures within a stage, which we will handle more explicitly in future status aggregation logic.

# Design updates

> Implemented a new StatusService in the backend to support detailed pipeline execution inspection. This service aggregates job-level status data and provides a comprehensive view of pipeline execution progress. The StatusService is designed to be extensible and can be easily integrated with other services for enhanced monitoring and reporting capabilities.
> Introduced MinIO integration to store logs persistently.
> Extended CLI report tooling to allow filtering by pipeline, run number, stage, and job with configurable output formatting. This feature enables users to quickly query and analyze historical pipeline executions for debugging and performance tuning.