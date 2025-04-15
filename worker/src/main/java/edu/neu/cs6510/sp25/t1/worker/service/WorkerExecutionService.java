package edu.neu.cs6510.sp25.t1.worker.service;

import edu.neu.cs6510.sp25.t1.common.dto.JobExecutionDTO;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import edu.neu.cs6510.sp25.t1.worker.execution.DockerExecutor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for executing jobs and reporting status to the backend.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkerExecutionService {
  private final DockerExecutor dockerExecutor;
  private final JobDataService jobDataService;
  private final ArtifactService artifactService;

  /**
   * Executes a job and updates its status in the database.
   *
   * @param job The job to execute
   */
  public void executeJob(JobExecutionDTO job) {

    if (job == null || job.getId() == null) {
      log.error("Received invalid job for execution");
      return;
    }

    log.info("Starting execution for job: {}", job.getId());

    try {

      // Execute the job in Docker
      ExecutionStatus result = dockerExecutor.execute(job);

      // Process the execution result
      handleExecutionResult(job, result);
    } catch (Exception e) {
      log.error("Exception during job execution {}: {}", job.getId(), e.getMessage(), e);
      // Update status to FAILED on exception
      jobDataService.updateJobStatus(job.getId(), ExecutionStatus.FAILED,
          "Job execution failed with error: " + e.getMessage());
    }
  }

  /**
   * Handles the result of job execution and updates the database accordingly.
   *
   * @param job    The job that was executed
   * @param result The execution result
   */
  private void handleExecutionResult(JobExecutionDTO job, ExecutionStatus result) {
    if (result == ExecutionStatus.FAILED && job.isAllowFailure()) {
      // Special case: job failed but is configured to allow failure
      log.warn("Job {} failed, but is allowed to fail. Reporting SUCCESS instead.", job.getId());
      jobDataService.updateJobStatus(job.getId(), ExecutionStatus.SUCCESS,
          "Job failed but allowFailure=true. Execution completed.");
    } else {
      // Standard case: report actual status
      log.info("Job {} completed with status: {}", job.getId(), result);
      jobDataService.updateJobStatus(job.getId(), result,
          "Job execution completed with status: " + result);
    }

    // Process artifacts if present
    if (job.getJob() != null && job.getJob().getArtifacts() != null && !job.getJob().getArtifacts().isEmpty()) {
      log.info("Job {} has artifacts to process: {}", job.getId(), job.getJob().getArtifacts());
      artifactService.processArtifacts(job.getId(), job.getJob().getWorkingDir(), job.getJob().getArtifacts());
    }
  }
}
