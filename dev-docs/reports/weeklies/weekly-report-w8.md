
# Week 8

> Fill in the week number (e.g., 1, 2, 3 etc.) for this report.


# Completed tasks

> List completed (DONE) tasks, include
> 1. A link to the Issue
> 2. The total weight (points or T-shirt size) allocated to the issue

| Task                                                                                                 | Weight |
|------------------------------------------------------------------------------------------------------| ------ |
| [fix docker](https://github.com/CS6510-SEA-SP25/t1-cicd/pull/226)                                    |   2     |
| [fix backend](https://github.com/CS6510-SEA-SP25/t1-cicd/pull/236)                                   |   2     |
| [connect worker with db](https://github.com/CS6510-SEA-SP25/t1-cicd/pull/232)                        |   2     |
| [seperate backend and worker](https://github.com/CS6510-SEA-SP25/t1-cicd/pull/231)                   |   2     |
| [adding rmq worker](https://github.com/CS6510-SEA-SP25/t1-cicd/pull/234)                             |   2     |
| [adding rmq backend](https://github.com/CS6510-SEA-SP25/t1-cicd/pull/238)                            |   2     |

# Carry over tasks

> List all issues that were planned for this week but did not get DONE
> Include
> 1. A link to the Issue
> 2. The total weight (points or T-shirt size) allocated to the issue
> 3. The team member assigned to the task. This has to be 1 person!

| Task | Weight | Assignee |
| ---- | ------ | -------- |
| [](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/) | 0 | Name |

There are no carry over tasks at the moment. Our team successfully delivered the Beta version this week. Collaboration was smooth, and everyone contributed effectively to complete the planned work on time.


### Todo
* Modify Report Service
    Enhance the report service to gather all reports generated from Docker executions and save them in a centralized location for easy retrieval and display.
* Modify Message Sequence
    Refactor the message flow to ensure all jobs are processed in the correct sequence. This includes handling job dependencies and ensuring the execution order aligns with pipeline and stage configurations

**Documentation**
* HOW TO document

### Checklist

**Run Command**
* All Entity save to db
* Backend Sending message through RMQ
* Worker Recieve message from RMQ
* Worker Retrieve from db
* Worker Trigger Docker to execute

# New tasks

For the upcoming sprint, the team will focus on the following key tasks aimed at improving system reliability and performance:

| Task | Weight | Assignee |
| ---- | ------ | -------- |
| [Implement Report Service](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/)  
Enhance the report service to collect, store, and display pipeline, stage, and job summaries effectively. | 3 | Name |
| [Refine Message Queue Sequence](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/)  
Ensure messages are correctly sequenced to maintain accurate job execution order across stages and workers. | 3 | Name |
| [Implement Async Methods in CLI](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/)  
Add asynchronous capabilities to CLI commands to support non-blocking operations and improve user experience. | 2 | Name |
| [Change Bind Mount to Docker Volume](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/)  
Refactor the Docker setup to use Docker volumes instead of bind mounts for better portability, isolation, and data management. | 2 | Name |
.

# What worked this week?

This week, the team demonstrated excellent collaboration and commitment, which contributed to successfully delivering the Beta version. Key highlights include:

- **Pair Programming**  
  Team members frequently engaged in pair programming, which helped improve code quality, knowledge sharing, and faster problem-solving.

- **Frequent Meetings**  
  Regular meetings kept everyone aligned, allowing us to quickly identify and resolve issues as they arose.

- **Strong Commitment**  
  Everyone stayed focused on the sprint goals, worked hard, and remained dedicated to completing their assigned tasks on time.

- **Effective Communication**  
  Communication across the team was clear and consistent, fostering a collaborative and productive working environment.

As a result, we successfully delivered the Beta version as planned!



# What did not work this week?


# Design updates

This week, we made several important design updates to improve system scalability and maintainability:

- **Implemented RabbitMQ for Messaging**  
  RabbitMQ (RMQ) was integrated to handle message passing between the backend and the worker services. This allows for asynchronous communication and decouples the components for better scalability.

- **Separated Worker and Backend Services**  
  The worker and backend were split into distinct services. This separation of concerns ensures that each component can scale independently and simplifies the architecture.

- **Worker Communicates Directly with the Database**  
  The worker service now retrieves job information and stores execution results directly in the database, reducing dependencies and streamlining data flow.

These updates align with our goal of creating a modular and efficient CI/CD system.


