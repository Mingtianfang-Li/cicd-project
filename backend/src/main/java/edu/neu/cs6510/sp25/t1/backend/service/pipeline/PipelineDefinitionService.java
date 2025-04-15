package edu.neu.cs6510.sp25.t1.backend.service.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.neu.cs6510.sp25.t1.backend.database.entity.JobEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.PipelineEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.StageEntity;
import edu.neu.cs6510.sp25.t1.backend.database.repository.JobRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.JobScriptRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.PipelineRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.StageRepository;
import edu.neu.cs6510.sp25.t1.common.api.request.PipelineExecutionRequest;
import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;
import lombok.RequiredArgsConstructor;

/**
 * Service responsible for creating and managing pipeline definition entities.
 * This includes pipelines, stages, and jobs.
 */
@Service
@RequiredArgsConstructor
public class PipelineDefinitionService {
  private final PipelineRepository pipelineRepository;
  private final StageRepository stageRepository;
  private final JobRepository jobRepository;
  private final JobScriptRepository jobScriptRepository;

  /**
   * Creates or retrieves an existing pipeline entity based on the request.
   *
   * @param request        the pipeline execution request
   * @param pipelineConfig the pipeline configuration from YAML
   * @return the ID of the pipeline entity
   */
  @Transactional
  public UUID createOrGetPipelineEntity(PipelineExecutionRequest request, Map<String, Object> pipelineConfig) {
    // Debug log the request
    PipelineLogger.info("createOrGetPipelineEntity called with pipelineId: " + request.getPipelineId());
    PipelineLogger.info("Repo: " + request.getRepo() + ", Branch: " + request.getBranch());
    PipelineLogger.info("CommitHash: " + request.getCommitHash() + ", FilePath: " + request.getFilePath());

    // Check if a pipeline ID was provided in the request
    if (request.getPipelineId() != null) {
      // Check if the pipeline exists in the database
      if (pipelineRepository.existsById(request.getPipelineId())) {
        PipelineLogger.info("Using existing pipeline with ID: " + request.getPipelineId());
        return request.getPipelineId();
      } else {
        PipelineLogger.info("Pipeline ID provided but not found in database: " + request.getPipelineId());
      }
    } else {
      PipelineLogger.info("No pipeline ID provided in request, will create new entity");
    }

    // Extract pipeline properties from config
    String name = extractPipelineName(pipelineConfig);
    String repoUrl = extractRepositoryUrl(request, pipelineConfig);
    String branch = extractBranch(request, pipelineConfig);

    PipelineLogger.info("Building pipeline entity with name: " + name);
    PipelineLogger.info("Repository URL: " + repoUrl);
    PipelineLogger.info("Branch: " + branch);
    PipelineLogger.info("Commit Hash: " + request.getCommitHash());

    // Create and save pipeline entity
    PipelineEntity pipeline = PipelineEntity.builder()
        .name(name)
        .repositoryUrl(repoUrl)
        .branch(branch)
        .commitHash(request.getCommitHash())
        .build();

    try {
      pipeline = pipelineRepository.saveAndFlush(pipeline);

      // Verify the save by retrieving it
      if (pipelineRepository.existsById(pipeline.getId())) {
        PipelineLogger.info("Successfully created and verified pipeline entity with ID: " + pipeline.getId());
      } else {
        PipelineLogger.error("Failed to verify pipeline entity was saved: " + pipeline.getId());
      }
    } catch (Exception e) {
      PipelineLogger.error("Error saving pipeline entity: " + e.getMessage());
      throw e;
    }

    return pipeline.getId();
  }

  /**
   * Creates stage and job entities based on pipeline configuration.
   *
   * @param pipelineId     the pipeline ID
   * @param pipelineConfig the parsed pipeline configuration
   * @param rootPath       the root path for job scripts
   */
  @Transactional
  public void createPipelineDefinition(UUID pipelineId, Map<String, Object> pipelineConfig, String rootPath) {
    PipelineLogger.info("Creating stage and job definitions for pipeline: " + pipelineId);

    // Verify the pipeline exists first
    PipelineEntity pipeline = pipelineRepository.findById(pipelineId)
        .orElseThrow(() -> {
          PipelineLogger.error("Pipeline not found with ID: " + pipelineId);
          return new RuntimeException("Pipeline not found: " + pipelineId);
        });

    PipelineLogger.info("Found pipeline entity with ID: " + pipelineId + ", name: " + pipeline.getName());

    boolean usingTopLevelJobs = pipelineConfig.containsKey("jobs");

    if (usingTopLevelJobs) {
      // Handle top-level jobs format
      PipelineLogger.info("Using top-level jobs format");
      createPipelineDefinitionWithTopLevelJobs(pipelineId, pipelineConfig, rootPath);
    } else {
      // Handle nested stage-jobs format
      PipelineLogger.info("Using nested stage-jobs format");
      createPipelineDefinitionWithNestedJobs(pipelineId, pipelineConfig);
    }
  }

  /**
   * Creates pipeline definition using the top-level jobs format.
   *
   * @param pipelineId     the pipeline ID
   * @param pipelineConfig the pipeline configuration
   * @param rootPath       the root path for job scripts
   */
  @Transactional
  private void createPipelineDefinitionWithTopLevelJobs(UUID pipelineId, Map<String, Object> pipelineConfig,
      String rootPath) {
    // Extract stages (as simple strings) from config
    List<String> stageNames = extractStageNamesFromConfig(pipelineConfig);

    if (stageNames.isEmpty()) {
      PipelineLogger.error("No valid stages found in pipeline configuration!");
      throw new RuntimeException("Pipeline must contain at least one valid stage.");
    }

    PipelineLogger.info("Found " + stageNames.size() + " stages in pipeline configuration");

    // Create stage entities for each stage name
    Map<String, UUID> stageNameToIdMap = new HashMap<>();
    for (int order = 0; order < stageNames.size(); order++) {
      String stageName = stageNames.get(order);
      UUID stageId = createStageEntity(pipelineId, stageName, order);
      stageNameToIdMap.put(stageName, stageId);
    }

    // Now handle the jobs
    List<Map<String, Object>> jobs = extractJobsFromConfig(pipelineConfig);

    if (jobs.isEmpty()) {
      PipelineLogger.warn("No jobs found in pipeline configuration!");
      return;
    }

    PipelineLogger.info("Found " + jobs.size() + " jobs in pipeline configuration");

    // Create job entities for each job
    for (Map<String, Object> jobConfig : jobs) {
      String stageName = (String) jobConfig.get("stage");
      if (stageName == null || !stageNameToIdMap.containsKey(stageName)) {
        PipelineLogger.error("Job references unknown stage: " + stageName);
        throw new RuntimeException("Job references unknown stage: " + stageName);
      }

      UUID stageId = stageNameToIdMap.get(stageName);
      createJobFromConfig(stageId, jobConfig, rootPath);
    }
  }

  /**
   * Creates pipeline definition using the nested stage-jobs format.
   *
   * @param pipelineId     the pipeline ID
   * @param pipelineConfig the pipeline configuration
   */
  @Transactional
  private void createPipelineDefinitionWithNestedJobs(UUID pipelineId, Map<String, Object> pipelineConfig) {
    List<Map<String, Object>> stages = extractNestedStagesFromConfig(pipelineConfig);

    if (stages.isEmpty()) {
      PipelineLogger.error("No valid stages found in pipeline configuration!");
      throw new RuntimeException("Pipeline must contain at least one valid stage.");
    }

    PipelineLogger.info("Found " + stages.size() + " stages in pipeline configuration");

    for (int order = 0; order < stages.size(); order++) {
      Map<String, Object> stageConfig = stages.get(order);
      createStageWithJobs(pipelineId, stageConfig, order);
    }
  }

  /**
   * Extract stage names from pipeline configuration (for top-level format).
   *
   * @param pipelineConfig the pipeline configuration
   * @return list of stage names
   */
  @SuppressWarnings("unchecked")
  private List<String> extractStageNamesFromConfig(Map<String, Object> pipelineConfig) {
    Object stagesObj = pipelineConfig.get("stages");
    if (!(stagesObj instanceof List<?> rawStages)) {
      PipelineLogger.error("Invalid pipeline structure: 'stages' must be a list.");
      throw new RuntimeException("Invalid pipeline configuration: 'stages' key is missing or incorrect.");
    }

    List<String> stageNames = new ArrayList<>();

    for (Object stageObj : rawStages) {
      if (stageObj instanceof String) {
        // Direct string stage name
        stageNames.add((String) stageObj);
      } else if (stageObj instanceof Map) {
        // Map with name property
        Map<String, Object> stageMap = (Map<String, Object>) stageObj;
        if (stageMap.containsKey("name")) {
          stageNames.add((String) stageMap.get("name"));
        }
      }
    }

    return stageNames;
  }

  /**
   * Extract jobs from pipeline configuration (for top-level format).
   *
   * @param pipelineConfig the pipeline configuration
   * @return list of job configurations
   */
  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> extractJobsFromConfig(Map<String, Object> pipelineConfig) {
    Object jobsObj = pipelineConfig.get("jobs");
    if (!(jobsObj instanceof List<?> rawJobs)) {
      PipelineLogger.warn("No jobs found or 'jobs' is not a list.");
      return List.of();
    }

    return rawJobs.stream()
        .filter(item -> item instanceof Map)
        .map(item -> (Map<String, Object>) item)
        .toList();
  }

  /**
   * Create a stage entity.
   *
   * @param pipelineId the pipeline ID
   * @param stageName  the stage name
   * @param order      the execution order
   * @return the stage ID
   */
  @Transactional
  private UUID createStageEntity(UUID pipelineId, String stageName, int order) {
    PipelineLogger.info("Creating stage with name: " + stageName + ", order: " + order);

    // Create and save the stage entity
    StageEntity stage = StageEntity.builder()
        .pipelineId(pipelineId)
        .name(stageName)
        .executionOrder(order)
        .build();

    try {
      stage = stageRepository.save(stage);
      stageRepository.flush();

      // Verify the stage was saved
      if (stageRepository.existsById(stage.getId())) {
        PipelineLogger.info("Successfully created stage with ID: " + stage.getId());
      } else {
        PipelineLogger.error("Failed to verify stage was saved: " + stage.getId());
      }

      return stage.getId();
    } catch (Exception e) {
      PipelineLogger.error("Error saving stage entity: " + e.getMessage());
      throw e;
    }
  }

  /**
   * Creates a job entity from job configuration.
   *
   * @param stageId   the stage ID
   * @param jobConfig the job configuration
   * @param rootPath  the root path for job scripts
   * @return the job ID
   */
  @Transactional
  private UUID createJobFromConfig(UUID stageId, Map<String, Object> jobConfig, String rootPath) {
    String jobName = (String) jobConfig.get("name");
    PipelineLogger.info("Creating job with name: " + jobName + " for stage: " + stageId);

    // Get docker image (support both "image" and "dockerImage" properties)
    String dockerImage = null;
    if (jobConfig.containsKey("image")) {
      dockerImage = (String) jobConfig.get("image");
    } else if (jobConfig.containsKey("dockerImage")) {
      dockerImage = (String) jobConfig.get("dockerImage");
    } else {
      dockerImage = "docker.io/library/alpine:latest"; // Default
    }

    // Get allow failure flag (support both "allow_failure" and "allowFailure"
    // properties)
    boolean allowFailure = false;
    if (jobConfig.containsKey("allow_failure")) {
      Object allowFailureObj = jobConfig.get("allow_failure");
      allowFailure = parseBoolean(allowFailureObj);
    } else if (jobConfig.containsKey("allowFailure")) {
      Object allowFailureObj = jobConfig.get("allowFailure");
      allowFailure = parseBoolean(allowFailureObj);
    }

    PipelineLogger
        .info("Job details - Name: " + jobName + ", Docker image: " + dockerImage + ", Allow failure: " + allowFailure);

    // Create and save the job entity
    JobEntity job = JobEntity.builder()
        .stageId(stageId)
        .name(jobName)
        .dockerImage(dockerImage)
        .allowFailure(allowFailure)
        .workingDir(rootPath)
        .build();

    try {
      job = jobRepository.save(job);
      jobRepository.flush();

      // Verify the job was saved
      if (jobRepository.existsById(job.getId())) {
        PipelineLogger.info("Successfully created job with ID: " + job.getId());
      } else {
        PipelineLogger.error("Failed to verify job was saved: " + job.getId());
      }

      // Handle job scripts if present
      saveJobScripts(job.getId(), jobConfig);

      return job.getId();
    } catch (Exception e) {
      PipelineLogger.error("Error saving job entity: " + e.getMessage() + " | " + e);
      throw e;
    }
  }

  /**
   * Extract stages from pipeline configuration for nested format.
   *
   * @param pipelineConfig the pipeline configuration
   * @return list of stage configurations
   */
  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> extractNestedStagesFromConfig(Map<String, Object> pipelineConfig) {
    Object stagesObj = pipelineConfig.get("stages");
    if (!(stagesObj instanceof List<?> rawStages)) {
      PipelineLogger.error("Invalid pipeline structure: 'stages' must be a list.");
      throw new RuntimeException("Invalid pipeline configuration: 'stages' key is missing or incorrect.");
    }

    return rawStages.stream()
        .filter(item -> item instanceof Map)
        .map(item -> (Map<String, Object>) item)
        .toList();
  }

  /**
   * Create a stage entity with its jobs.
   *
   * @param pipelineId  the pipeline ID
   * @param stageConfig the stage configuration
   * @param order       the execution order
   */
  @Transactional
  private void createStageWithJobs(UUID pipelineId, Map<String, Object> stageConfig, int order) {
    String stageName = (String) stageConfig.get("name");

    PipelineLogger.info("Creating stage with name: " + stageName + ", order: " + order);

    // Create and save the stage entity
    StageEntity stage = StageEntity.builder()
        .pipelineId(pipelineId)
        .name(stageName)
        .executionOrder(order)
        .build();

    try {
      stage = stageRepository.save(stage);
      stageRepository.flush();

      // Verify the stage was saved
      if (stageRepository.existsById(stage.getId())) {
        PipelineLogger.info("Successfully created stage with ID: " + stage.getId());
      } else {
        PipelineLogger.error("Failed to verify stage was saved: " + stage.getId());
      }

      // Create and save job entities for this stage
      createJobDefinitions(stage.getId(), stageConfig);
    } catch (Exception e) {
      PipelineLogger.error("Error saving stage entity: " + e.getMessage());
      throw e;
    }
  }

  /**
   * Creates job entities based on stage configuration.
   *
   * @param stageId     the stage ID
   * @param stageConfig the stage configuration from YAML
   */
  @SuppressWarnings("unchecked")
  private void createJobDefinitions(UUID stageId, Map<String, Object> stageConfig) {
    // Verify the stage exists first
    StageEntity stage = stageRepository.findById(stageId)
        .orElseThrow(() -> {
          PipelineLogger.error("Stage not found with ID: " + stageId);
          return new RuntimeException("Stage not found: " + stageId);
        });

    PipelineLogger.info("Found stage entity with ID: " + stageId + ", name: " + stage.getName());

    List<Map<String, Object>> jobsConfig = (List<Map<String, Object>>) stageConfig.get("jobs");

    if (jobsConfig == null || jobsConfig.isEmpty()) {
      PipelineLogger.warn("No jobs defined in YAML for stage: " + stageId);
      return;
    }

    PipelineLogger.info("Found " + jobsConfig.size() + " jobs in stage configuration");

    for (Map<String, Object> jobConfig : jobsConfig) {
      createJob(stageId, jobConfig);
    }
  }

  /**
   * Create a job entity.
   *
   * @param stageId   the stage ID
   * @param jobConfig the job configuration
   */
  @Transactional
  private void createJob(UUID stageId, Map<String, Object> jobConfig) {
    String jobName = (String) jobConfig.get("name");
    PipelineLogger.info("Creating job with name: " + jobName + " for stage: " + stageId);

    String dockerImage = extractDockerImage(jobConfig);
    boolean allowFailure = extractAllowFailure(jobConfig);
    String workingDir = (String) jobConfig.get("working_dir");

    PipelineLogger
        .info("Job details - Name: " + jobName + ", Docker image: " + dockerImage + ", Allow failure: " + allowFailure);

    // Create and save the job entity
    JobEntity job = JobEntity.builder()
        .stageId(stageId)
        .name(jobName)
        .dockerImage(dockerImage)
        .allowFailure(allowFailure)
        .workingDir(workingDir)
        .build();

    try {
      job = jobRepository.save(job);
      jobRepository.flush();

      // Verify the job was saved
      if (jobRepository.existsById(job.getId())) {
        PipelineLogger.info("Successfully created job with ID: " + job.getId());
      } else {
        PipelineLogger.error("Failed to verify job was saved: " + job.getId());
      }

      // Handle job scripts if present
      saveJobScripts(job.getId(), jobConfig);
    } catch (Exception e) {
      PipelineLogger.error("Error saving job entity: " + e.getMessage() + " | " + e);
      throw e;
    }
  }

  /**
   * Extract docker image from job configuration.
   *
   * @param jobConfig the job configuration
   * @return the docker image
   */
  private String extractDockerImage(Map<String, Object> jobConfig) {
    return jobConfig.containsKey("image")
        ? (String) jobConfig.get("image")
        : "docker.io/library/alpine:latest";
  }

  /**
   * Extract allow failure flag from job configuration.
   *
   * @param jobConfig the job configuration
   * @return true if failure is allowed, false otherwise
   */
  private boolean extractAllowFailure(Map<String, Object> jobConfig) {
    if (!jobConfig.containsKey("allow_failure")) {
      return false;
    }

    Object allowFailureObj = jobConfig.get("allow_failure");
    if (allowFailureObj instanceof Boolean) {
      return (Boolean) allowFailureObj;
    } else if (allowFailureObj instanceof String) {
      return Boolean.parseBoolean((String) allowFailureObj);
    }

    return false;
  }

  /**
   * Save job scripts.
   *
   * @param jobId     the job ID
   * @param jobConfig the job configuration
   */
  @SuppressWarnings("unchecked")
  private void saveJobScripts(UUID jobId, Map<String, Object> jobConfig) {
    if (!jobConfig.containsKey("script")) {
      PipelineLogger.warn("No scripts defined for job: " + jobId);
      return;
    }

    Object scriptObj = jobConfig.get("script");
    if (scriptObj instanceof String) {
      // Single script line
      String script = (String) scriptObj;
      PipelineLogger.info("Adding script to job " + jobId + ": " +
          (script.length() > 30 ? script.substring(0, 30) + "..." : script));
      jobScriptRepository.saveScript(jobId, script);
    } else if (scriptObj instanceof List) {
      // Multiple script lines
      List<String> scripts = ((List<?>) scriptObj).stream()
          .filter(s -> s instanceof String)
          .map(s -> (String) s)
          .toList();

      PipelineLogger.info("Adding " + scripts.size() + " script lines to job " + jobId);
      for (String script : scripts) {
        jobScriptRepository.saveScript(jobId, script);
      }
    }
  }

  /**
   * Parse a boolean value from an object.
   * 
   * @param value the object to parse
   * @return the boolean value
   */
  private boolean parseBoolean(Object value) {
    if (value instanceof Boolean) {
      return (Boolean) value;
    } else if (value instanceof String) {
      return Boolean.parseBoolean((String) value);
    }
    return false;
  }

  /**
   * Extract name from pipeline configuration.
   *
   * @param pipelineConfig the pipeline configuration
   * @return the pipeline name
   */
  private String extractPipelineName(Map<String, Object> pipelineConfig) {
    return pipelineConfig.containsKey("name")
        ? (String) pipelineConfig.get("name")
        : "pipeline-" + UUID.randomUUID().toString().substring(0, 8);
  }

  /**
   * Extract repository URL from request or pipeline configuration.
   *
   * @param request        the pipeline execution request
   * @param pipelineConfig the pipeline configuration
   * @return the repository URL
   */
  private String extractRepositoryUrl(PipelineExecutionRequest request, Map<String, Object> pipelineConfig) {
    return request.getRepo() != null
        ? request.getRepo()
        : (pipelineConfig.containsKey("repository")
            ? (String) pipelineConfig.get("repository")
            : "local-repository");
  }

  /**
   * Extract branch from request or pipeline configuration.
   *
   * @param request        the pipeline execution request
   * @param pipelineConfig the pipeline configuration
   * @return the branch name
   */
  private String extractBranch(PipelineExecutionRequest request, Map<String, Object> pipelineConfig) {
    return request.getBranch() != null
        ? request.getBranch()
        : (pipelineConfig.containsKey("branch")
            ? (String) pipelineConfig.get("branch")
            : "main");
  }
}