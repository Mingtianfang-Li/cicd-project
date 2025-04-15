package edu.neu.cs6510.sp25.t1.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Getter;

/**
 * Represents the structure of a CI/CD pipeline configuration.
 * Defines a pipeline and its associated metadata.
 * Supports both nested jobs within stages and separate top-level jobs.
 */
@Getter
public class Pipeline {
  // Getter with lombok
  private final UUID id;
  private final String name;
  private final String repoUrl;
  private final String branch;
  private final String commitHash;
  private final List<Stage> stages;
  private final List<Job> jobs;
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;

  /**
   * Constructs a new Pipeline instance.
   *
   * @param id         Unique pipeline identifier
   * @param name       Pipeline name
   * @param repoUrl    URL or local path of the repository
   * @param branch     Git branch name (default: "main")
   * @param commitHash Git commit hash (optional, latest commit by default)
   * @param stages     List of stages in the pipeline
   * @param jobs       List of jobs (for top-level job format)
   * @param createdAt  Timestamp of pipeline creation
   * @param updatedAt  Timestamp of last update
   */
  @JsonCreator
  public Pipeline(
          @JsonProperty("id") UUID id,
          @JsonProperty("name") String name,
          @JsonProperty("repoUrl") String repoUrl,
          @JsonProperty("branch") String branch,
          @JsonProperty("commitHash") String commitHash,
          @JsonProperty("stages") List<Stage> stages,
          @JsonProperty("jobs") List<Job> jobs,
          @JsonProperty("createdAt") LocalDateTime createdAt,
          @JsonProperty("updatedAt") LocalDateTime updatedAt) {
    this.id = id;
    this.name = name;
    this.repoUrl = repoUrl;
    this.branch = branch != null ? branch : "main"; // Default to "main"
    this.commitHash = commitHash;
    this.stages = stages != null ? stages : List.of();
    this.jobs = jobs != null ? jobs : List.of();
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }
  
  /**
   * Gets all jobs associated with this pipeline, either from stages or from top-level jobs.
   * If using top-level jobs format, associates jobs with their respective stages.
   *
   * @return List of all jobs
   */
  public List<Job> getAllJobs() {
    // If we have nested jobs in stages, use those
    List<Job> allJobs = new ArrayList<>();
    
    // Add jobs from stages
    for (Stage stage : stages) {
      if (stage.getJobs() != null && !stage.getJobs().isEmpty()) {
        allJobs.addAll(stage.getJobs());
      }
    }
    
    // If no nested jobs but we have top-level jobs, use those
    if (allJobs.isEmpty() && !jobs.isEmpty()) {
      allJobs.addAll(jobs);
    }
    
    return allJobs;
  }
  
  /**
   * Groups jobs by stage name, useful when using top-level jobs format.
   *
   * @return Map of stage name to list of jobs
   */
  public Map<String, List<Job>> getJobsByStage() {
    if (!jobs.isEmpty()) {
      // Using top-level jobs format, group by stage
      return jobs.stream()
          .filter(job -> job.getStage() != null && !job.getStage().isEmpty())
          .collect(Collectors.groupingBy(Job::getStage));
    } else {
      // Using nested jobs format, extract from stages
      return stages.stream()
          .filter(stage -> stage.getName() != null && !stage.getName().isEmpty())
          .filter(stage -> stage.getJobs() != null && !stage.getJobs().isEmpty())
          .collect(Collectors.toMap(Stage::getName, Stage::getJobs));
    }
  }
}
