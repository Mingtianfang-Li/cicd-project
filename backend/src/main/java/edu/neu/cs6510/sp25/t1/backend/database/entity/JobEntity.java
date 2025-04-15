package edu.neu.cs6510.sp25.t1.backend.database.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class for jobs.
 */
@Entity
@Table(name = "jobs", indexes = {
        @Index(name = "idx_stage_jobs", columnList = "stage_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "stage_id")
  private UUID stageId;

  @Column(name = "name")
  private String name;

  @Builder.Default
  @Column(name = "docker_image", columnDefinition = "VARCHAR(255) DEFAULT 'docker.io/library/alpine:latest'")
  private String dockerImage = "docker.io/library/alpine:latest";

  @Builder.Default
  @Column(name = "allow_failure", columnDefinition = "BOOLEAN DEFAULT FALSE")
  private boolean allowFailure = false;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "working_dir")
  private String workingDir;

  @ElementCollection
  @CollectionTable(name = "job_scripts", joinColumns = @JoinColumn(name = "job_id"))
  @Column(name = "script")
  private List<String> script;

  @ElementCollection
  @CollectionTable(name = "job_dependencies", joinColumns = @JoinColumn(name = "job_id"))
  @Column(name = "depends_on_job_id")
  private List<UUID> dependencies;

  @ElementCollection
  @CollectionTable(name = "job_artifacts", joinColumns = @JoinColumn(name = "job_id"))
  @Column(name = "artifact_path")
  private List<String> artifacts;

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
