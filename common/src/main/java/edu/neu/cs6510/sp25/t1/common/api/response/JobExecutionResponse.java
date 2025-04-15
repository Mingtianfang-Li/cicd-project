package edu.neu.cs6510.sp25.t1.common.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a response to a job execution request.
 */
public class JobExecutionResponse {
  private final String jobExecutionId;
  private final String status;

  /**
   * Constructs a new JobExecutionResponse based on its definition.
   *
   * @param jobExecutionId The unique identifier for this job execution
   * @param status         The status of the job execution
   */
  @JsonCreator
  public JobExecutionResponse(
          @JsonProperty("jobExecutionId") String jobExecutionId,
          @JsonProperty("status") String status) {
    this.jobExecutionId = jobExecutionId;
    this.status = status;
  }


  /**
   * Getter for jobExecutionId.
   *
   * @return the jobExecutionId
   */
  public String getJobExecutionId() {
    return jobExecutionId;
  }

  /**
   * Getter for status.
   *
   * @return the status
   */
  public String getStatus() {
    return status;
  }
}
