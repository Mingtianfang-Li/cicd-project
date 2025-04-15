package edu.neu.cs6510.sp25.t1.backend.database.entity;

import java.time.Instant;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an execution instance of a CI/CD pipeline.
 * Tracks the execution status, metadata, and timing details.
 */
@Entity
@Table(name = "pipeline_executions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineExecutionEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "pipeline_id")
  private UUID pipelineId;

  @Column(name = "run_number")
  private int runNumber;

  @Column(name = "commit_hash", length = 40)
  private String commitHash;

  @Column(name = "is_local")
  private boolean isLocal;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private ExecutionStatus status;

  @Column(name = "start_time")
  private Instant startTime;

  @Column(name = "completion_time")
  private Instant completionTime;

  @PrePersist
  protected void onCreate() {
    this.startTime = Instant.now();
    this.status = ExecutionStatus.PENDING;
  }

  public void updateState(ExecutionStatus newState) {
    this.status = newState;
    if (newState == ExecutionStatus.SUCCESS || newState == ExecutionStatus.FAILED || newState == ExecutionStatus.CANCELED) {
      this.completionTime = Instant.now();
    }
  }
}
