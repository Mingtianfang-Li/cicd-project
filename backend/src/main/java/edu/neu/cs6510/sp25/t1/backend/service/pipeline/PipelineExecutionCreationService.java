package edu.neu.cs6510.sp25.t1.backend.service.pipeline;

import java.time.Instant;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.neu.cs6510.sp25.t1.backend.database.entity.JobEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.JobExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.PipelineExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.StageEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.StageExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.database.repository.JobExecutionRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.JobRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.PipelineExecutionRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.PipelineRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.StageExecutionRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.StageRepository;
import edu.neu.cs6510.sp25.t1.common.api.request.PipelineExecutionRequest;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;
import lombok.RequiredArgsConstructor;

/**
 * Service responsible for creating pipeline execution entities.
 * This includes pipeline executions, stage executions, and job executions.
 */
@Service
@RequiredArgsConstructor
public class PipelineExecutionCreationService {
  private final PipelineExecutionRepository pipelineExecutionRepository;
  private final StageExecutionRepository stageExecutionRepository;
  private final JobExecutionRepository jobExecutionRepository;
  private final PipelineRepository pipelineRepository;
  private final StageRepository stageRepository;
  private final JobRepository jobRepository;

  /**
   * Creates a new pipeline execution entity.
   *
   * @param request the pipeline execution request
   * @param pipelineId the ID of the pipeline entity
   * @return the created pipeline execution entity
   */
  public PipelineExecutionEntity createPipelineExecution(PipelineExecutionRequest request, UUID pipelineId) {
    return PipelineExecutionEntity.builder()
            .pipelineId(pipelineId)
            .commitHash(request.getCommitHash())
            .isLocal(request.isLocal())
            .status(ExecutionStatus.PENDING)
            .startTime(Instant.now())
            .build();
  }

  /**
   * Saves a pipeline execution to the database.
   *
   * @param pipelineExecution the pipeline execution entity to save
   * @return the saved pipeline execution entity
   */
  @Transactional
  public PipelineExecutionEntity savePipelineExecution(PipelineExecutionEntity pipelineExecution) {
    try {
      pipelineExecution = pipelineExecutionRepository.saveAndFlush(pipelineExecution); // Save and flush in one operation
      PipelineLogger.info("Successfully saved pipeline execution: " + pipelineExecution.getId());

      // Verify the save was successful
      PipelineExecutionEntity savedEntity = pipelineExecutionRepository.findById(pipelineExecution.getId())
              .orElseThrow(() -> new RuntimeException("Failed to verify pipeline execution was saved"));
      PipelineLogger.info("Verified pipeline execution exists in database: " + savedEntity.getId());

      return pipelineExecution;
    } catch (Exception e) {
      PipelineLogger.error("Error saving pipeline execution: " + e.getMessage());
      throw new RuntimeException("Failed to save pipeline execution: " + e.getMessage(), e);
    }
  }

  /**
   * Creates and saves stage execution entities based on the pipeline YAML configuration.
   *
   * @param pipelineExecutionId ID of the pipeline execution
   * @param pipelineConfig      Parsed pipeline configuration
   * @param stageQueue          Queue to store job UUIDs for each stage execution
   */
  @Transactional
  public void createAndSaveStageExecutions(
      UUID pipelineExecutionId,
      Map<String, Object> pipelineConfig,
      Queue<Queue<UUID>> stageQueue) {
//    // Determine if we're using top-level jobs or nested stages format
//    boolean usingTopLevelJobs = pipelineConfig.containsKey("jobs");
    
    // Get pipeline execution to retrieve pipelineId
    PipelineExecutionEntity pipelineExecution = pipelineExecutionRepository.findById(pipelineExecutionId)
            .orElseThrow(() -> new RuntimeException("Pipeline execution not found: " + pipelineExecutionId));
    
    // Get all stages for this pipeline
    List<StageEntity> pipelineStages = stageRepository.findByPipelineId(pipelineExecution.getPipelineId());
    if (pipelineStages.isEmpty()) {
      PipelineLogger.error("No stage definitions found for pipeline: " + pipelineExecution.getPipelineId());
      throw new RuntimeException("Pipeline stage definitions not found");
    }
    
    // Ensure all stage entities are flushed to the database
    stageRepository.flush();
    
    int stagesCount = pipelineStages.size();
    PipelineLogger.info("Creating " + stagesCount + " stage executions for pipeline: " + pipelineExecutionId);
    
    String commitHash = pipelineExecution.getCommitHash();
    boolean isLocal = pipelineExecution.isLocal();
    PipelineLogger.info("Using commit hash: " + commitHash + " and isLocal: " + isLocal);

    // Create stage executions for all stages
//    for (int order = 0; order < stagesCount; order++) {
//      createStageExecution(pipelineExecutionId, pipelineStages, order, commitHash, isLocal);
//    }
    for (int order = 0; order < stagesCount; order++) {
      Queue<UUID> jobQueue = createStageExecution(pipelineExecutionId, pipelineStages, order, commitHash, isLocal);
      stageQueue.add(jobQueue);
    }
  }
  
  /**
   * Create a stage execution entity.
   *
   * @param pipelineExecutionId the pipeline execution ID
   * @param pipelineStages the list of stage entities
   * @param order the execution order
   * @param commitHash the commit hash
   * @param isLocal whether the execution is local
   * 
   * @return a queue of job UUIDs associated with the created stage execution
   */
  @Transactional
  private Queue<UUID> createStageExecution(UUID pipelineExecutionId, List<StageEntity> pipelineStages, int order,
      String commitHash, boolean isLocal) {

    Queue<UUID> jobQueue = new LinkedList<>();

    // Find the matching stage entity for this order
    int finalOrder = order;
    StageEntity matchingStage = pipelineStages.stream()
            .filter(stage -> stage.getExecutionOrder() == finalOrder)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Stage definition not found for order: " + finalOrder));
    
    // Create the stage execution entity
    StageExecutionEntity stageExecution = StageExecutionEntity.builder()
            .pipelineExecutionId(pipelineExecutionId)
            .stageId(matchingStage.getId())  // Use actual stage ID 
            .executionOrder(order)
            .commitHash(commitHash)  // Use commit hash from pipeline execution
            .isLocal(isLocal)        // Use isLocal from pipeline execution
            .status(ExecutionStatus.PENDING)
            .startTime(Instant.now())
            .build();

    try {
      // Save the stage execution
      stageExecution = stageExecutionRepository.saveAndFlush(stageExecution);
      PipelineLogger.info("Saved stage execution with ID: " + stageExecution.getId() + " for stage: " + matchingStage.getId());

      // Make sure all jobs for this stage are already created
      jobRepository.flush();
      
      // Extract and create job executions
      List<JobExecutionEntity> jobs = createJobExecutions(matchingStage.getId(), stageExecution);

      // Save job executions
      if (!jobs.isEmpty()) {
        PipelineLogger.info("Saving " + jobs.size() + " jobs for stage: " + stageExecution.getId());
        jobs = jobExecutionRepository.saveAll(jobs).stream().toList();
        jobExecutionRepository.flush();

        PipelineLogger.info("Saved stage execution: " + stageExecution.getId() + " with " + jobs.size() + " jobs.");

        // add to queue
        jobs.forEach(jobExecution -> jobQueue.add(jobExecution.getId()));
        PipelineLogger.info("Collected job UUIDs for stage execution " + stageExecution.getId() + ": " + jobQueue);
      } else {
        PipelineLogger.warn("No jobs defined for stage: " + stageExecution.getId());
      }
    } catch (Exception e) {
      PipelineLogger.error("Error saving stage execution: " + e.getMessage() + " | " + e);
      throw e;
    }

    return jobQueue;
  }

  /**
   * Creates job execution entities for a stage.
   *
   * @param stageId       ID of the stage entity
   * @param stageExecution stage execution entity
   * @return list of job execution entities
   */
  @Transactional
  private List<JobExecutionEntity> createJobExecutions(UUID stageId, StageExecutionEntity stageExecution) {
    // Get all jobs for this stage
    List<JobEntity> stageJobs = jobRepository.findByStageId(stageId);
    
    if (stageJobs.isEmpty()) {
      PipelineLogger.warn("No job definitions found for stage: " + stageId);
      return List.of();
    }

    // Create job executions by ensuring job entities are saved first
    return stageJobs.stream().map(job -> {
      if (job.getId() == null) {
        PipelineLogger.info("Saving job entity before creating execution for job: " + job.getName());
        job = jobRepository.saveAndFlush(job);
      } else {
        if (!jobRepository.existsById(job.getId())) {
          PipelineLogger.warn("Job entity does not exist in database, saving it now: " + job.getId());
          job = jobRepository.saveAndFlush(job);
        }
      }

      return JobExecutionEntity.builder()
              .stageExecution(stageExecution)
              .jobId(job.getId())  // Use actual job ID
              .commitHash(stageExecution.getCommitHash())
              .isLocal(stageExecution.isLocal())
              .allowFailure(job.isAllowFailure())
              .status(ExecutionStatus.PENDING)
              .startTime(Instant.now())
              .build();
    }).collect(java.util.stream.Collectors.toList());
  }

  /**
   * Verify all entities were saved correctly.
   *
   * @param pipelineId the pipeline ID
   * @param pipelineExecutionId the pipeline execution ID
   */
  public void verifyEntitiesSaved(UUID pipelineId, UUID pipelineExecutionId) {
    try {
      // Check pipeline exists
      if (!pipelineRepository.existsById(pipelineId)) {
        PipelineLogger.error("Pipeline entity was not saved correctly: " + pipelineId);
      } else {
        PipelineLogger.info("Successfully verified pipeline entity: " + pipelineId);
      }

      // Check pipeline execution exists
      if (!pipelineExecutionRepository.existsById(pipelineExecutionId)) {
        PipelineLogger.error("Pipeline execution entity was not saved correctly: " + pipelineExecutionId);
      } else {
        PipelineLogger.info("Successfully verified pipeline execution entity: " + pipelineExecutionId);
      }

      // Check stages exist
      List<StageEntity> stages = stageRepository.findByPipelineId(pipelineId);
      PipelineLogger.info("Found " + stages.size() + " stages for pipeline: " + pipelineId);

      // Check jobs exist for each stage
      for (StageEntity stage : stages) {
        List<JobEntity> jobs = jobRepository.findByStageId(stage.getId());
        PipelineLogger.info("Found " + jobs.size() + " jobs for stage: " + stage.getId());
      }
      
      // Check stage executions exist
      List<StageExecutionEntity> stageExecutions = stageExecutionRepository.findByPipelineExecutionId(pipelineExecutionId);
      PipelineLogger.info("Found " + stageExecutions.size() + " stage executions for pipeline execution: " + pipelineExecutionId);
      
      // Check job executions exist for each stage execution
      for (StageExecutionEntity stageExecution : stageExecutions) {
        List<JobExecutionEntity> jobExecutions = jobExecutionRepository.findByStageExecution(stageExecution);
        PipelineLogger.info("Found " + jobExecutions.size() + " job executions for stage execution: " + stageExecution.getId());
      }
    } catch (Exception e) {
      PipelineLogger.error("Error verifying saved entities: " + e.getMessage());
    }
  }
}