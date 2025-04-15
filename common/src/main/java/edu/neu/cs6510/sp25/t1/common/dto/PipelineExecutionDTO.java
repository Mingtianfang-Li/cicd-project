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
 * Data Transfer Object (DTO) for pipeline execution data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineExecutionDTO {

  /**
   * Unique identifier for the pipeline execution.
   */
  private UUID id;

  /**
   * Foreign key reference to the pipeline.
   */
  private UUID pipelineId;

  /**
   * Run number tracking executions of the pipeline.
   */
  private int runNumber;

  /**
   * Git commit hash associated with this execution.
   */
  private String commitHash;

  /**
   * Indicates whether this execution is local.
   */
  private boolean isLocal;

  /**
   * Execution status of the pipeline.
   */
  private ExecutionStatus status;

  /**
   * Timestamp of when the execution started.
   */
  private Instant startTime;

  /**
   * Timestamp of when the execution completed.
   */
  private Instant completionTime;
}
