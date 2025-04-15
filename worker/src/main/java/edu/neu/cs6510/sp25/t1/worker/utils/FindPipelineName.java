package edu.neu.cs6510.sp25.t1.worker.utils;

import edu.neu.cs6510.sp25.t1.backend.database.repository.PipelineExecutionRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.PipelineRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.StageExecutionRepository;
import edu.neu.cs6510.sp25.t1.common.dto.JobExecutionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Utility class to help retrieve pipeline information from a JobExecutionDTO.
 */
@Component
@RequiredArgsConstructor
public class FindPipelineName {
  private final StageExecutionRepository stageExecutionRepository;
  private final PipelineExecutionRepository pipelineExecutionRepository;
  private final PipelineRepository pipelineRepository;

  /**
   * Gets the pipeline name associated with the given job execution.
   * Traverses from job → stage → pipelineExecution → pipeline → name.
   *
   * @param jobExecutionDTO the job execution DTO
   * @return the pipeline name
   */
  public String getPipelineName(JobExecutionDTO jobExecutionDTO) {
    var stageExecution = stageExecutionRepository.findById(jobExecutionDTO.getStageExecutionId())
        .orElseThrow(() -> new IllegalStateException("StageExecution not found"));

    var pipelineExecution = pipelineExecutionRepository.findById(stageExecution.getPipelineExecutionId())
        .orElseThrow(() -> new IllegalStateException("PipelineExecution not found"));

    var pipeline = pipelineRepository.findById(pipelineExecution.getPipelineId())
        .orElseThrow(() -> new IllegalStateException("Pipeline not found"));

    return pipeline.getName();
  }
}
