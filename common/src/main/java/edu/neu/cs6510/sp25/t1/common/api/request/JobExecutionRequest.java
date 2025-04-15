package edu.neu.cs6510.sp25.t1.common.api.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

import lombok.Getter;

/**
 * Represents a request to execute a job.
 */
@Getter
public class JobExecutionRequest {
  private final UUID jobId;
  private final UUID stageExecutionId;
  private final String commitHash;
  private final boolean isLocal;
  private final String dockerImage;
  private final List<String> commands;
  private final String jobName;

  /**
   * Constructs a new JobExecutionRequest based on its definition.
   *
   * @param jobId            The unique identifier for the job
   * @param stageExecutionId The ID of the stage execution
   * @param commitHash       The Git commit hash for this execution
   * @param isLocal          Indicates if the execution is local
   * @param dockerImage      The Docker image to use for the job
   * @param commands         The list of commands to execute
   * @param jobName          The name of the job
   */
  @JsonCreator
  public JobExecutionRequest(
          @JsonProperty("jobId") UUID jobId,
          @JsonProperty("stageExecutionId") UUID stageExecutionId,
          @JsonProperty("commitHash") String commitHash,
          @JsonProperty("isLocal") boolean isLocal,
          @JsonProperty("dockerImage") String dockerImage,
          @JsonProperty("commands") List<String> commands,
          @JsonProperty("jobName") String jobName) {
    this.jobId = jobId;
    this.stageExecutionId = stageExecutionId;
    this.commitHash = commitHash;
    this.isLocal = isLocal;
    this.dockerImage = dockerImage;
    this.commands = commands;
    this.jobName = jobName;
  }

  /**
   * Gets the unique identifier for the job.
   *
   * @return The job ID
   */
  @JsonProperty("isLocal")
  public boolean isLocal() {
    return isLocal;
  }
}
