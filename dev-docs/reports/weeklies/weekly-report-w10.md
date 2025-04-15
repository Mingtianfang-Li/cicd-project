
# Week 9

# Completed tasks

> List completed (DONE) tasks, include
> 1. A link to the Issue
> 2. The total weight (points or T-shirt size) allocated to the issue

| Task                                                                                       | Weight |
|--------------------------------------------------------------------------------------------|--------|
| [Remote run: acheive --repo method](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/256) | 2      |
| [Docker execution](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/257)                  | 3      |
| [Run current services in k8s](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/257)       | 3      |

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

| Task | Weight | Assignee |
| ---- |--|--|
| [](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/) |  |  |
| [](https://github.com/CS6510-SEA-SP25/t1-cicd/issues/) |  |  |

Since next week we won't get new features, we need to complete the missing piece in previous working. 
And make sure meet the submission requirement.

We will go through the requirement and review based on five parts listed on repository submission.



# What worked this week?
* Restructured YAML file: move workingDir
* Docker execution: we struggle a lot on solve mounting issue. Instead of mounting according to the file structure of worker container, cli component create a shared volume and then map the job execution folder in the shared volume to the execution of container.
* Remote run: --repo command will clone a remote git repository and run locally.

# What did not work this week?

Save commit hash information in database. We will fix this part in next week


# Design updates
k8s: run backend, worker in one pod, but keep database out of pod
