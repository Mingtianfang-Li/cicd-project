package edu.neu.cs6510.sp25.t1.common.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Getter;

/**
 * Represents the response structure for pipeline execution reports.
 */
@Getter
public class PipelineReportResponse {
  // Getters with lombok
  private final String executionId;
  private final String status;
  private final String commitHash;
  private List<StageReportSummary> stages;

  /**
   * Constructor for PipelineReportResponse.
   *
   * @param executionId Unique execution identifier.
   * @param status      Execution status (PENDING, RUNNING, SUCCESS, FAILED,
   *                    CANCELED).
   * @param commitHash  Commit hash associated with this execution.
   */
  @JsonCreator
  public PipelineReportResponse(
      @JsonProperty("executionId") String executionId,
      @JsonProperty("status") String status,
      @JsonProperty("commitHash") String commitHash) {
    this.executionId = executionId;
    this.status = status;
    this.commitHash = commitHash;
  }

  /**
   * Constructor with stage summary.
   *
   * @param executionId Unique execution identifier.
   * @param status      Execution status.
   * @param commitHash  Commit hash.
   * @param stages      Summary of stage executions.
   */
  public PipelineReportResponse(String executionId, String status, String commitHash, List<StageReportSummary> stages) {
    this.executionId = executionId;
    this.status = status;
    this.commitHash = commitHash;
    this.stages = stages;
  }

  /**
   * Represents a summary of a stage execution within a pipeline.
   */
  @Getter
  public static class StageReportSummary {
    private final String stageName;
    private final String status;

    /**
     * Constructor for StageReportSummary.
     *
     * @param stageName Name of the stage.
     * @param status    Status of the stage execution.
     */
    @JsonCreator
    public StageReportSummary(
        @JsonProperty("stageName") String stageName,
        @JsonProperty("status") String status) {
      this.stageName = stageName;
      this.status = status;
    }
  }
}
