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
 * Represents a CI/CD pipeline configuration in the system.
 */
@Entity
@Table(name = "pipelines", indexes = {
        @Index(name = "idx_pipeline_name", columnList = "name"),
        @Index(name = "idx_pipeline_repo", columnList = "repository_url")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineEntity {
  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "name")
  private String name;

  @Column(name = "repository_url")
  private String repositoryUrl;

  @Builder.Default
  @Column(name = "branch", columnDefinition = "VARCHAR(255) DEFAULT 'main'")
  private String branch = "main";

  @Column(name = "commit_hash", length = 40)
  private String commitHash;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
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