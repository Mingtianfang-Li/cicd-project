package edu.neu.cs6510.sp25.t1.common.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Getter;

/**
 * Represents a response to a stage report request.
 */
@Getter
public class StageReportResponse {
  // Getters with lombok
  private final String stageName;
  private final List<ExecutionRecord> executions;

  /**
   * Constructs a new StageReportResponse based on its definition.
   *
   * @param stageName  The stage name
   * @param executions The list of job executions
   */
  @JsonCreator
  public StageReportResponse(
          @JsonProperty("stageName") String stageName,
          @JsonProperty("executions") List<ExecutionRecord> executions) {
    this.stageName = stageName;
    this.executions = executions;
  }

  /**
   * Represents a record of a past execution of the stage.
   */
  @Getter
  public static class ExecutionRecord {
    // Getters with lombok
    private final String executionId;
    private final String status;

    /**
     * Constructs an ExecutionRecord.
     *
     * @param executionId The unique identifier of the stage execution.
     * @param status      The status of the stage execution (e.g., SUCCESS, FAILED, CANCELED).
     */
    @JsonCreator
    public ExecutionRecord(
            @JsonProperty("executionId") String executionId,
            @JsonProperty("status") String status) {
      this.executionId = executionId;
      this.status = status;
    }
  }
}

