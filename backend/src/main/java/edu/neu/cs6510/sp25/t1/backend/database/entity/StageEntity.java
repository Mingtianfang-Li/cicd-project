package edu.neu.cs6510.sp25.t1.backend.database.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a stage in a CI/CD pipeline configuration.
 * A stage consists of multiple jobs and is executed as part of a pipeline.
 */
@Entity
@Table(name = "stages", indexes = {
        @Index(name = "idx_pipeline_stages", columnList = "pipeline_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StageEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "pipeline_id")
  private UUID pipelineId;

  @Column(name = "name")
  private String name;

  @Column(name = "execution_order", columnDefinition = "INT DEFAULT 0")
  private int executionOrder;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }
}
