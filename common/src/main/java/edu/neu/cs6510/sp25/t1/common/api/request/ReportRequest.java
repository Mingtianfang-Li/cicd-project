package edu.neu.cs6510.sp25.t1.common.api.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

/**
 * Represents a request to report the status of a job execution.
 */
@Getter
public class ReportRequest {
  private final String pipelineName;
  private final String stageName;
  private final String jobName;
  private final int runNumber;

  /**
   * Constructor for ReportRequest.
   *
   * @param pipelineName The pipeline name
   * @param stageName    The stage name
   * @param jobName      The job name
   * @param runNumber    The run number
   */
  @JsonCreator
  public ReportRequest(
          @JsonProperty("pipelineName") String pipelineName,
          @JsonProperty("stageName") String stageName,
          @JsonProperty("jobName") String jobName,
          @JsonProperty("runNumber") int runNumber) {
    this.pipelineName = pipelineName;
    this.stageName = stageName;
    this.jobName = jobName;
    this.runNumber = runNumber;
  }
}
