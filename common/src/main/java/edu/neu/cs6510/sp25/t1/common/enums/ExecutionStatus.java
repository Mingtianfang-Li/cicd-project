package edu.neu.cs6510.sp25.t1.common.enums;

import lombok.Getter;

/**
 * Enum representing the possible execution states of a pipeline, stage, or job.
 */
@Getter
public enum ExecutionStatus {
  PENDING("Execution is scheduled but has not started."),
  RUNNING("Execution is in progress."),
  SUCCESS("Execution completed successfully."),
  FAILED("Execution encountered a failure."),
  CANCELED("The job/stage was dropped due to an earlier failure.");
  // Getters with lombok
  private final String description;

  /**
   * Constructor for ExecutionStatus.
   *
   * @param description Description of the execution status.
   */
  ExecutionStatus(String description) {
    this.description = description;
  }

}
