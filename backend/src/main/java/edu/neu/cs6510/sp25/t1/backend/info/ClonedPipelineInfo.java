package edu.neu.cs6510.sp25.t1.backend.info;

import lombok.Getter;

import java.util.UUID;

/**
 * Represents metadata about a cloned pipeline repository.
 * This includes the absolute path to the located pipeline YAML file
 * and the UUID used to identify the clone.
 */
@Getter
public class ClonedPipelineInfo {
  private final String yamlPath;
  private final UUID uuid;

  /**
   * Constructs a new {@code ClonedPipelineInfo}.
   *
   * @param yamlPath the absolute path to the pipeline YAML file
   * @param uuid the UUID identifying the cloned repository directory
   */
  public ClonedPipelineInfo(String yamlPath, UUID uuid) {
    this.yamlPath = yamlPath;
    this.uuid = uuid;
  }

}
