package edu.neu.cs6510.sp25.t1.common.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Represents a request to update the status of a job execution.
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobStatusUpdate {
  private UUID jobExecutionId;
  private ExecutionStatus status;
  private String logs;

  /**
   * JSON mapping for deserialization.
   * @param jobExecutionId the ID of the job execution
   */
  @JsonProperty("jobExecutionId")
  public void setJobExecutionId(UUID jobExecutionId) {
    this.jobExecutionId = jobExecutionId;
  }

  @JsonProperty("status")
  public void setStatus(ExecutionStatus status) {
    this.status = status;
  }

  @JsonProperty("logs")
  public void setLogs(String logs) {
    this.logs = logs;
  }
}
