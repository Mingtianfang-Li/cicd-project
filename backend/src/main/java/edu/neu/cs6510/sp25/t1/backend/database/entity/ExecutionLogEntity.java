package edu.neu.cs6510.sp25.t1.backend.database.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an execution log entry stored in the database.
 */
@Getter
@Entity
@Table(name = "execution_logs")
public class ExecutionLogEntity {

  // Getters
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Setter
  @Column(name = "pipeline_execution_id")
  private UUID pipelineExecutionId;

  @Setter
  @Column(name = "stage_execution_id")
  private UUID stageExecutionId;

  @Setter
  @Column(name = "job_execution_id")
  private UUID jobExecutionId;

  // Setters
  @Setter
  @Column(name = "log_text", nullable = false, columnDefinition = "TEXT")
  private String logText;

  @Column(nullable = false, updatable = false)
  private LocalDateTime timestamp;

  /**
   * Default constructor required by JPA.
   */
  protected ExecutionLogEntity() {
    this.timestamp = LocalDateTime.now();
  }

  /**
   * Constructor for creating an ExecutionLogEntity.
   *
   * @param logText             Log message text
   * @param pipelineExecutionId Pipeline execution ID (optional)
   * @param stageExecutionId    Stage execution ID (optional)
   * @param jobExecutionId      Job execution ID (optional)
   */
  public ExecutionLogEntity(String logText, UUID pipelineExecutionId, UUID stageExecutionId, UUID jobExecutionId) {
    this.logText = logText;
    this.pipelineExecutionId = pipelineExecutionId;
    this.stageExecutionId = stageExecutionId;
    this.jobExecutionId = jobExecutionId;
    this.timestamp = LocalDateTime.now();
  }

}
