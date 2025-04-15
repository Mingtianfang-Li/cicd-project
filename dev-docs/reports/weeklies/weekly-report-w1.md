
# Week 1

# Completed tasks

> List completed (DONE) tasks, include
> 1. A link to the Issue
> 2. The total weight (points or T-shirt size) allocated to the issue

| Task | Weight | Assignee |
| ---- | ------ | -------- |
|  [HLD Design Doc](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/2)  |  2      |      Mingtianfang Li    |
|  [Tech Stack Proposal](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/3)     |  2      |      Shenqian Wen    |
|  [Team Processe Design Doc](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/4)     |  2      |      Wenxue Fang    |
|  [Weekly Report, Readme, doc research, check & format](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/5)     |  2      |      Yiwen Wang    |



# Carry over tasks

> List all issues that were planned for this week but did not get DONE
> Include
> 1. A link to the Issue
> 2. The total weight (points or T-shirt size) allocated to the issue
> 3. The team member assigned to the task. This has to be 1 person!

| Task | Weight |
| ---- | ------ |
| N/A    |   N/A     |


# New tasks

> List all the issues that the team is planning to work for next sprint
> Include
> 1. A link to the Issue
> 2. The total weight (points or T-shirt size) allocated to the issue
> 3. The team member assigned to the task. This has to be 1 person!

- We want to demo:
## Backend API Demo

### 1. Overview of Backend APIs
- Introduction to the backend system.
- Key functionalities of the APIs.
- Tech stack used (e.g., Java, Spring Boot, Kotlin, Groovy, etc.).

### 2. API Endpoints Demo
- **Show API request formats:**
  - HTTP methods: `GET`, `POST`, `PUT`, `DELETE`
  - Request body, query parameters.
- **Demo using tools:**
  - Postman, Swagger UI, or cURL.

### 3. Success & Failure Response Handling
- **Successful API Call:**
  - Example:
    ```json
    {
      "status": "success",
      "message": "Data retrieved successfully",
      "data": {
        "id": 123,
        "name": "John Doe"
      }
    }
    ```
  - HTTP Status: `200 OK`

- **Failure Cases:**
  - Invalid Input (`400 Bad Request`):
    ```json
    {
      "status": "error",
      "message": "Invalid request parameters"
    }
    ```
  - Unauthorized (`401 Unauthorized`):
    ```json
    {
      "status": "error",
      "message": "Authentication required"
    }
    ```
  - Resource Not Found (`404 Not Found`):
    ```json
    {
      "status": "error",
      "message": "Resource not found"
    }
    ```
  - Internal Server Error (`500 Internal Server Error`):
    ```json
    {
      "status": "error",
      "message": "Unexpected server error"
    }
    ```

### 4. Logs & Debugging (Optional)
- Show API logs or monitoring (if available).
- Mention debugging tools or observability practices (logging frameworks, APM tools).

### 5. Q&A & Next Steps
- Address questions from the audience.
- Discuss potential improvements or roadmap items.


| Task | Weight | Assignee |
| ---- | ------ | -------- |
|   [Initialize CLI Project](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/7)  |   2     |    Yiwen Wang      |
|   [Implement "Run Pipeline" Command](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/8)  |   2     |     Wenxue Fang     |
|   [Implement "Check Status" Command](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/9)  |   2     |    Shenqian Wen      |
|   [Implement "Retrieve Logs" Command](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/10)  |   2     |    Mingtianfang Li      |
|   [ Implement Continuous Integration (CI) Workflow](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/14)  |   1     |    Mingtianfang Li      |


# What worked this week?

- Successfully scheduled a meeting to discuss the design document and next week's tasks, ensuring alignment and clarity among team members. 
- During the meeting, we also completed the design document, setting a solid foundation for the next phase of work.

# What did not work this week?

- Found out a little late about the deadline is Feb 1, but handled it by having meeting done smoothly
- Asked about the clarification on Friday, maybe earlier next time. 

# Design updates

1. We created and agreed High Level arch design documents.
2. We created and agreed the tech stack proposal.
3. We agreed on the team process.
4. We initialized the readme and unified all doc format. 
