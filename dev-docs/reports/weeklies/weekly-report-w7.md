
# Week 7

> Fill in the week number (e.g., 1, 2, 3 etc.) for this report.


# Completed tasks

> List completed (DONE) tasks, include
> 1. A link to the Issue
> 2. The total weight (points or T-shirt size) allocated to the issue

| Task                                                                                                 | Weight |
|------------------------------------------------------------------------------------------------------| ------ |
| [fix Pipeline, stage, job entities](https://github.com/CS6510-SEA-SP25/t1-cicd/pull/217)             |   2     |
| [backend, worker error handler](https://github.com/CS6510-SEA-SP25/t1-cicd/pull/214)                 |   2     |
| [add --verbose](https://github.com/CS6510-SEA-SP25/t1-cicd/pull/215)                                 |   2     |
| [correct logic of processing data in backend  ](https://github.com/CS6510-SEA-SP25/t1-cicd/pull/216) |   2     |

# Carry over tasks

> List all issues that were planned for this week but did not get DONE
> Include
> 1. A link to the Issue
> 2. The total weight (points or T-shirt size) allocated to the issue
> 3. The team member assigned to the task. This has to be 1 person!

| Task | Weight | Assignee |
| ---- | ------ | -------- |
| [](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/) | 0 | Name |

Following our lengthy discussion, regrouping, and weekend Hackathon, we still have several pending items. We're currently working independently on local machines and plan to merge all code on Monday to address as many TODOs and checklist items as possible.
### Todo
**Configuration**
* configuration files in a folder, each of them is independent
* global section define properties for the whole pipeline
    * pipline name, stages, jobs(required include)
* Stages have a default but the configuration file can override
* Jobs should be able to use or override global keys

**Documentation**
* HOW TO document

### Checklist

**Report**
* Show all pipeline run e.g L4.1 xx --local report
* Show pipeline run summary(give pipeline name and run number) e.g. L4.2
* Show stage summary(all stage or given run number) e.g L4.3
* Show job summary(give pipeline, stage, job and run number) e.g L4.4

**Run Command**
* Job execition should not be always running

**Validation**
* Implement unique pipeline name validation within repositories

# New tasks

> List all the issues that the team is planning to work for next sprint
> Include
> 1. A link to the Issue
> 2. The total weight (points or T-shirt size) allocated to the issue
> 3. The team member assigned to the task. This has to be 1 person!

| Task | Weight | Assignee |
| ---- | ------ | -------- |
| [](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/) | 0 | Name |

We're experiencing issues with executing jobs on Docker and need to investigate solutions.
We've developed two potential solutions, and team members will work in parallel to test both approaches. Resolving this Docker execution problem is our highest priority as it's essential for moving the project forward.

# What worked this week?

> In this section list part of the team's process that you believe worked well. "Worked Well" means helped the team be more efficient and/or effective. Try to explain **why** these actions worked well.


Yaml file can be successfully parsed and send to database, backend can retrieved correct data from the database.

API works except docker failed it send failure back to backend


# What did not work this week?

we cannot execute the docker image successfully when do the job execution

> In this section list part of the team's process that you believe did **not** work well. "Not Worked Well" means that the team found these actions to **not have a good effect** on the team's effectiveness. Try to explain **why** these actions did not work well.

We couldn't successfully execute Docker images during job execution. Additionally, communication continues to be challenging. Effective teamwork requires respect and clear communication. A key lesson from past few weeks is the importance of finding mutually agreed-upon plan. This is prerequisite to keep work on the right track. Complete agreement isn't always possible, but at least, everyone on the team should be able to explain what we're working on right now.


# Design updates

> If changes have been made to the overall design approach for the project this week, list the updates here. Link to documents (or updates to documents) that describe in detail what these changes are.

Error Handler in Backend and Worker, rather than in common (like how validation works)

