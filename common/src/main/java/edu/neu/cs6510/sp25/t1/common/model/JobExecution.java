package edu.neu.cs6510.sp25.t1.common.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import lombok.Getter;

/**
 * Represents a job execution instance within a pipeline stage.
 * This class tracks the execution status of a job, including its start time,
 * completion time, dependencies, and whether it should be executed in Docker
 * or locally in a non-containerized environment.
 */
@Getter
public class JobExecution {

  private final UUID id;
  private final UUID stageExecutionId;
  private final UUID jobId;
  private final String commitHash;
  private final boolean isLocal;
  private ExecutionStatus status;
  private final Instant startTime;
  private Instant completionTime;
  private Instant lastUpdated;
  private final boolean allowFailure;
  private final List<String> script;
  private final List<String> dependencies;
//  private List<String> artifacts;

  private String logs;

  /**
   * Constructs a new JobExecution instance.
   *
   * @param stageExecutionId The ID of the stage execution.
   * @param jobId            The ID of the job being executed.
   * @param commitHash       The commit hash of the repository state for execution.
   * @param isLocal          Whether this job execution is local or remote.
   * @param image            The Docker image used for execution.
   * @param script           The script to be executed.
   * @param dependencies     The dependencies required before execution.
   * @param allowFailure     Whether the job is allowed to fail.
   */
  public JobExecution(UUID stageExecutionId, UUID jobId, String commitHash, boolean isLocal,
                      String image, List<String> script, List<String> dependencies, boolean allowFailure) {
    this.id = UUID.randomUUID();  // Generate new unique ID
    this.stageExecutionId = stageExecutionId;
    this.jobId = jobId;
    this.commitHash = commitHash;
    this.isLocal = isLocal;
    this.status = ExecutionStatus.PENDING;
    this.startTime = Instant.now();
    this.completionTime = null;  // Initially null
    this.lastUpdated = Instant.now();
    this.allowFailure = allowFailure;
    this.script = script;
    this.dependencies = dependencies;
    this.logs = "";
  }

  /**
   * Updates the execution status of this entity.
   *
   * @param newState the new execution status to be set
   */
  public void updateState(ExecutionStatus newState) {
    this.status = newState;
    this.lastUpdated = Instant.now();
    if (newState == ExecutionStatus.SUCCESS || newState == ExecutionStatus.FAILED || newState == ExecutionStatus.CANCELED) {
      this.completionTime = Instant.now();
    }
  }

  /**
   * Appends logs to the job execution.
   *
   * @param logText The logs to append.
   */
  public void appendLogs(String logText) {
    this.logs += logText + "\n";
  }

  /**
   * Gets whether this job execution is local or remote.
   *
   * @return True if local, false if remote.
   */
  public boolean isLocal() {
    return isLocal;
  }

//  /**
//   * Updates the list of artifacts for this job execution.
//   *
//   * @param artifactPaths The paths of uploaded artifacts.
//   */
//  public void setArtifacts(List<String> artifactPaths) {
//    this.artifacts = artifactPaths;
//    this.lastUpdated = Instant.now();
//  }


}
