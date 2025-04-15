package edu.neu.cs6510.sp25.t1.common.api.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.UUID;

/**
 * Represents a request to execute a pipeline.
 */
@Getter
public class PipelineExecutionRequest {
  private final UUID pipelineId;
  private final String repo;
  private final String branch;
  private final String commitHash;
  private final boolean isLocal;
  private final int runNumber;
  private final String filePath;

  /**
   * Constructs a new PipelineExecutionRequest based on its definition.
   */
  /**
   * Constructs a new PipelineExecutionRequest.
   *
   * @param pipelineId the unique identifier of the pipeline
   * @param repo the repository associated with the pipeline
   * @param branch the branch of the repository to execute the pipeline on
   * @param commitHash the commit hash to execute the pipeline for
   * @param isLocal a flag indicating whether the pipeline execution is local
   * @param runNumber the run number of the pipeline execution
   * @param filePath the file path associated with the pipeline execution
   */
  @JsonCreator
  public PipelineExecutionRequest(
          @JsonProperty("pipelineId") UUID pipelineId,
          @JsonProperty("repo") String repo,
          @JsonProperty("branch") String branch,
          @JsonProperty("commitHash") String commitHash,
          @JsonProperty("isLocal") boolean isLocal,
          @JsonProperty("runNumber") int runNumber,
          @JsonProperty("filePath") String filePath) {
    this.pipelineId = pipelineId;
    this.repo = repo;
    this.branch = branch;
    this.commitHash = commitHash;
    this.isLocal = isLocal;
    this.runNumber = runNumber;
    this.filePath = filePath;
  }
}
