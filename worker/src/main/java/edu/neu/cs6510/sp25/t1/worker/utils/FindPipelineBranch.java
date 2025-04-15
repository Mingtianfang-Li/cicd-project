package edu.neu.cs6510.sp25.t1.worker.utils;

import edu.neu.cs6510.sp25.t1.backend.database.repository.PipelineExecutionRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.PipelineRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.StageExecutionRepository;
import edu.neu.cs6510.sp25.t1.common.dto.JobExecutionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Utility class to help retrieve the Git branch for a pipeline from a JobExecutionDTO.
 */
@Component
@RequiredArgsConstructor
public class FindPipelineBranch {
  private final StageExecutionRepository stageExecutionRepository;
  private final PipelineExecutionRepository pipelineExecutionRepository;
  private final PipelineRepository pipelineRepository;

  /**
   * Gets the Git branch name associated with the pipeline this job belongs to.
   *
   * @param jobExecutionDTO the job execution DTO
   * @return the Git branch name
   */
  public String getBranch(JobExecutionDTO jobExecutionDTO) {
    var stageExecution = stageExecutionRepository.findById(jobExecutionDTO.getStageExecutionId())
        .orElseThrow(() -> new IllegalStateException("StageExecution not found"));

    var pipelineExecution = pipelineExecutionRepository.findById(stageExecution.getPipelineExecutionId())
        .orElseThrow(() -> new IllegalStateException("PipelineExecution not found"));

    var pipeline = pipelineRepository.findById(pipelineExecution.getPipelineId())
        .orElseThrow(() -> new IllegalStateException("Pipeline not found"));

    return pipeline.getBranch(); // Make sure Pipeline entity has `getBranch()`
  }
}
