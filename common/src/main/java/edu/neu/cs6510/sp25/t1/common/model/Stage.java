package edu.neu.cs6510.sp25.t1.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import edu.neu.cs6510.sp25.t1.common.model.deserializer.StageDeserializer;

/**
 * Represents a stage in a CI/CD pipeline configuration.
 */
@Getter
@JsonDeserialize(using = StageDeserializer.class)
public class Stage {
  // Getter with lombok
  private final UUID id;
  private final String name;
  private final UUID pipelineId;
  private final int executionOrder;
  private final List<Job> jobs;
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;

  /**
   * Constructor for Stage.
   *
   * @param id             Unique identifier for the stage
   * @param name           Stage name
   * @param pipelineId     Reference to the pipeline this stage belongs to
   * @param executionOrder Execution order of the stage
   * @param jobs           List of jobs in the stage
   * @param createdAt      Timestamp of stage creation
   * @param updatedAt      Timestamp of last update
   */
  @JsonCreator
  public Stage(
          @JsonProperty("id") UUID id,
          @JsonProperty("name") String name,
          @JsonProperty("pipelineId") UUID pipelineId,
          @JsonProperty("executionOrder") int executionOrder,
          @JsonProperty("jobs") List<Job> jobs,
          @JsonProperty("createdAt") LocalDateTime createdAt,
          @JsonProperty("updatedAt") LocalDateTime updatedAt) {
    this.id = id;
    this.name = name;
    this.pipelineId = pipelineId;
    this.executionOrder = executionOrder;
    this.jobs = (jobs != null) ? jobs : List.of(); // Ensure jobs is never null
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }
  
  /**
   * Simple constructor for string deserialization - used when stage name is provided as a string.
   * 
   * @param name The name of the stage
   */
  public Stage(String name) {
    this.id = null;
    this.name = name;
    this.pipelineId = null;
    this.executionOrder = 0;
    this.jobs = List.of();
    this.createdAt = null;
    this.updatedAt = null;
  }
}
