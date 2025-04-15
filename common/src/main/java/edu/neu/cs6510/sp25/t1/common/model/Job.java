package edu.neu.cs6510.sp25.t1.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Getter;

/**
 * Represents a job in a CI/CD pipeline configuration.
 * Static configuration data.
 * Supports both nested format (with stageId) and top-level format (with stage name).
 */
@Getter
public class Job {

  // Getters with lombok
  private final UUID id;  // This will be generated when saving to the database

  private final UUID stageId;
  
  private final String stage; // Stage name for top-level job format

  private final String name;

  private final String dockerImage;
  
  private final String image; // Alternative name for dockerImage

  private final List<String> script;

  private final List<String> dependencies;  // Store as job names instead of UUIDs

  private final boolean allowFailure;
  
  private final Boolean allow_failure; // Alternative name for allowFailure

  private final List<String> artifacts;
  
  private final String workingDir; // Working directory for job execution
  
  private final String working_dir; // Alternative name for workingDir

  private final LocalDateTime createdAt;

  private final LocalDateTime updatedAt;

  /**
   * Constructor for Job with support for both nested and top-level formats.
   *
   * @param id            Job ID (UUID)
   * @param stageId       ID of the stage this job belongs to
   * @param stage         Name of the stage this job belongs to (for top-level format)
   * @param name          Job name
   * @param dockerImage   Docker image used for execution
   * @param image         Alternative name for dockerImage
   * @param script        List of commands to run (should be stored separately in DB)
   * @param dependencies  Job dependencies (other job names, will be resolved to UUIDs later)
   * @param allowFailure  Whether the job can fail without failing the pipeline
   * @param allow_failure Alternative name for allowFailure
   * @param artifacts     List of artifacts produced by the job
   * @param workingDir    Working directory for job execution
   * @param working_dir   Alternative name for workingDir
   * @param createdAt     Timestamp when the job was created
   * @param updatedAt     Timestamp when the job was last updated
   */
  @JsonCreator
  public Job(
          @JsonProperty("id") UUID id,
          @JsonProperty("stageId") UUID stageId,
          @JsonProperty("stage") String stage,
          @JsonProperty("name") String name,
          @JsonProperty("dockerImage") String dockerImage,
          @JsonProperty("image") String image,
          @JsonProperty("script") Object script,
          @JsonProperty("dependencies") Object dependencies,
          @JsonProperty("allowFailure") Boolean allowFailure,
          @JsonProperty("allow_failure") Boolean allow_failure,
          @JsonProperty("artifacts") List<String> artifacts,
          @JsonProperty("workingDir") String workingDir,
          @JsonProperty("working_dir") String working_dir,
          @JsonProperty("createdAt") LocalDateTime createdAt,
          @JsonProperty("updatedAt") LocalDateTime updatedAt) {
    this.id = id;
    this.stageId = stageId;
    this.stage = stage;
    this.name = name;
    
    // Handle alternative property names
    this.dockerImage = dockerImage != null ? dockerImage : image;
    this.image = this.dockerImage;
    
    // Convert script from string or list to list
    this.script = convertToStringList(script);
    
    // Convert dependencies from string or list to list
    this.dependencies = convertToStringList(dependencies);
    
    // Handle alternative property names for boolean values
    this.allowFailure = allowFailure != null ? allowFailure : (allow_failure != null ? allow_failure : false);
    this.allow_failure = this.allowFailure;
    
    this.artifacts = artifacts != null ? artifacts : List.of();
    
    // Handle alternative property names for working directory
    this.workingDir = workingDir != null ? workingDir : working_dir;
    this.working_dir = this.workingDir;
    
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }
  
  /**
   * Helper method to convert an object to a list of strings.
   * Handles cases where the input is a single string, a list of strings, or null.
   *
   * @param obj The object to convert
   * @return A list of strings
   */
  @SuppressWarnings("unchecked")
  private List<String> convertToStringList(Object obj) {
    if (obj == null) {
      return List.of();
    } else if (obj instanceof String) {
      return List.of((String) obj);
    } else if (obj instanceof List) {
      return (List<String>) obj;
    } else {
      return List.of(obj.toString());
    }
  }
}
