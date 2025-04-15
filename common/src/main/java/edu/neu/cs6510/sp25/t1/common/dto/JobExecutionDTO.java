package edu.neu.cs6510.sp25.t1.common.dto;

import java.time.Instant;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for job execution data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobExecutionDTO {

  /**
   * Unique identifier for the job execution.
   */
  private UUID id;

  /**
   * Foreign key reference to the stage execution.
   */
  private UUID stageExecutionId;

  /**
   * Foreign key reference to the job being executed.
   */
  private UUID jobId;

  /**
   * The commit hash of the repository state for execution.
   */
  private String commitHash;

  /**
   * Indicates if the job execution was local or remote.
   */
  private boolean isLocal;

  /**
   * Execution status of the job.
   */
  private ExecutionStatus status;

  /**
   * Timestamp of when the job execution started.
   */
  private Instant startTime;

  /**
   * Timestamp of when the job execution completed.
   */
  private Instant completionTime;

  /**
   * Indicates whether the job execution is allowed to fail.
   */
  private boolean allowFailure;

  /**
   * The full Job details associated with this execution.
   */
  private JobDTO job;
}
