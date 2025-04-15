package edu.neu.cs6510.sp25.t1.backend.database.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an execution instance of a stage in a CI/CD pipeline.
 * Tracks execution status, metadata, and timing details.
 */
@Entity
@Table(name = "stage_executions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StageExecutionEntity {

  /**
   * Unique identifier for the stage execution.
   */
  @Id
  @GeneratedValue
  private UUID id;

  /**
   * Foreign key reference to the stage being executed.
   */
  @Column(name = "stage_id")
  private UUID stageId;

  /**
   * Foreign key reference to the pipeline execution this stage belongs to.
   */
  @Column(name = "pipeline_execution_id")
  private UUID pipelineExecutionId;

  /**
   * The execution order of the stage within the pipeline.
   */
  @Column(name = "execution_order")
  private int executionOrder;


  /**
   * Git commit hash associated with this stage execution.
   */
  @Column(name = "commit_hash", length = 40)
  private String commitHash;

  /**
   * Indicates whether this execution is local.
   */
  @Column(name = "is_local")
  private boolean isLocal;

  /**
   * The execution status of the stage.
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private ExecutionStatus status;

  /**
   * Timestamp indicating when the stage execution started.
   */
  @Column(name = "start_time")
  private Instant startTime;

  /**
   * Timestamp indicating when the stage execution was completed.
   */
  @Column(name = "completion_time")
  private Instant completionTime;

  /**
   * List of job executions associated with this stage execution
   */
  @OneToMany(mappedBy = "stageExecution", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private List<JobExecutionEntity> jobs;

  /**
   * Lifecycle hook to set default timestamps before persisting.
   */
  @PrePersist
  protected void onCreate() {
    this.startTime = Instant.now();
    this.status = ExecutionStatus.PENDING;
  }

  /**
   * Updates the execution status and completion timestamp.
   *
   * @param newState the new execution status to set
   */
  public void updateState(ExecutionStatus newState) {
    this.status = newState;
    if (newState == ExecutionStatus.SUCCESS || newState == ExecutionStatus.FAILED || newState == ExecutionStatus.CANCELED) {
      this.completionTime = Instant.now();
    }
  }
}
