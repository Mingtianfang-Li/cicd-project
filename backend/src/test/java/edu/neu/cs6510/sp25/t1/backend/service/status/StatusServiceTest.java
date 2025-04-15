package edu.neu.cs6510.sp25.t1.backend.service.status;

import edu.neu.cs6510.sp25.t1.backend.database.entity.ExecutionLogEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.JobEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.JobExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.PipelineEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.PipelineExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.StageEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.StageExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.database.repository.*;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatusServiceTest {

  private PipelineRepository pipelineRepository;
  private PipelineExecutionRepository pipelineExecutionRepository;
  private StageRepository stageRepository;
  private StageExecutionRepository stageExecutionRepository;
  private JobRepository jobRepository;
  private JobExecutionRepository jobExecutionRepository;
  private StatusService statusService;

  @BeforeEach
  void setUp() {
    pipelineRepository = mock(PipelineRepository.class);
    pipelineExecutionRepository = mock(PipelineExecutionRepository.class);
    stageRepository = mock(StageRepository.class);
    stageExecutionRepository = mock(StageExecutionRepository.class);
    jobRepository = mock(JobRepository.class);
    jobExecutionRepository = mock(JobExecutionRepository.class);

    statusService = new StatusService(
        pipelineRepository,
        pipelineExecutionRepository,
        stageRepository,
        stageExecutionRepository,
        jobRepository,
        jobExecutionRepository
    );
  }

  @Test
  void testGetStatusForPipeline_success() {
    // Arrange
    UUID pipelineId = UUID.randomUUID();
    UUID pipelineExecutionId = UUID.randomUUID();
    UUID stageId = UUID.randomUUID();
    UUID jobId = UUID.randomUUID();

    PipelineEntity pipeline = new PipelineEntity();
    pipeline.setId(pipelineId);
    pipeline.setName("demo");

    PipelineExecutionEntity pipelineExecution = new PipelineExecutionEntity();
    pipelineExecution.setId(pipelineExecutionId);
    pipelineExecution.setPipelineId(pipelineId);

    StageEntity stage = new StageEntity();
    stage.setId(stageId);
    stage.setName("Build");
    stage.setPipelineId(pipelineId);

    StageExecutionEntity stageExecution = new StageExecutionEntity();
    stageExecution.setId(UUID.randomUUID());
    stageExecution.setStageId(stageId);
    stageExecution.setPipelineExecutionId(pipelineExecutionId);
    stageExecution.setStatus(ExecutionStatus.SUCCESS);

    JobEntity job = new JobEntity();
    job.setId(jobId);
    job.setName("Compile");
    job.setStageId(stageId);

    JobExecutionEntity jobExecution = new JobExecutionEntity();
    jobExecution.setId(UUID.randomUUID());
    jobExecution.setJobId(jobId);
    jobExecution.setStatus(ExecutionStatus.SUCCESS);

    when(pipelineRepository.findByName("demo")).thenReturn(Optional.of(pipeline));
    when(pipelineExecutionRepository.findByPipelineId(pipelineId)).thenReturn(Optional.of(pipelineExecution));
    when(stageRepository.findByPipelineId(pipelineId)).thenReturn(List.of(stage));
    when(stageExecutionRepository.findByStageIdAndPipelineExecutionId(stageId, pipelineExecutionId)).thenReturn(Optional.of(stageExecution));
    when(jobRepository.findByStageId(stageId)).thenReturn(List.of(job));
    when(jobExecutionRepository.findByJobId(jobId)).thenReturn(Optional.of(jobExecution));

    // Act
    Map<String, Object> result = statusService.getStatusForPipeline("demo");

    // Assert
    assertEquals("demo", result.get("pipeline"));
    assertEquals(ExecutionStatus.SUCCESS, result.get("pipelineStatus"));

    List<Map<String, Object>> stages = (List<Map<String, Object>>) result.get("stageResult");
    assertEquals(1, stages.size());
    assertEquals("Build", stages.get(0).get("stage"));
    assertEquals(ExecutionStatus.SUCCESS, stages.get(0).get("stageExecutionStatus"));

    List<Map<String, Object>> jobs = (List<Map<String, Object>>) stages.get(0).get("jobs");
    assertEquals(1, jobs.size());
    assertEquals("Compile", jobs.get(0).get("job"));
    assertEquals(ExecutionStatus.SUCCESS, jobs.get(0).get("jobExecutionStatus"));
  }

  @Test
  void testPipelineNotFound() {
    when(pipelineRepository.findByName("nonexistent")).thenReturn(Optional.empty());

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
        statusService.getStatusForPipeline("nonexistent"));

    assertTrue(ex.getMessage().contains("Pipeline not found"));
  }

  @Test
  void testPipelineExecutionNotFound() {
    UUID pipelineId = UUID.randomUUID();
    PipelineEntity pipeline = new PipelineEntity();
    pipeline.setId(pipelineId);
    pipeline.setName("demo");

    when(pipelineRepository.findByName("demo")).thenReturn(Optional.of(pipeline));
    when(pipelineExecutionRepository.findByPipelineId(pipelineId)).thenReturn(Optional.empty());

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
        statusService.getStatusForPipeline("demo"));

    assertTrue(ex.getMessage().contains("PipelineExecution not found"));
  }

  @Test
  void testStageExecutionNotFound() {
    UUID pipelineId = UUID.randomUUID();
    UUID pipelineExecutionId = UUID.randomUUID();
    UUID stageId = UUID.randomUUID();

    PipelineEntity pipeline = new PipelineEntity();
    pipeline.setId(pipelineId);
    pipeline.setName("demo");

    PipelineExecutionEntity pipelineExecution = new PipelineExecutionEntity();
    pipelineExecution.setId(pipelineExecutionId);
    pipelineExecution.setPipelineId(pipelineId);

    StageEntity stage = new StageEntity();
    stage.setId(stageId);
    stage.setName("Build");
    stage.setPipelineId(pipelineId);

    when(pipelineRepository.findByName("demo")).thenReturn(Optional.of(pipeline));
    when(pipelineExecutionRepository.findByPipelineId(pipelineId)).thenReturn(Optional.of(pipelineExecution));
    when(stageRepository.findByPipelineId(pipelineId)).thenReturn(List.of(stage));
    when(stageExecutionRepository.findByStageIdAndPipelineExecutionId(stageId, pipelineExecutionId)).thenReturn(Optional.empty());

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
        statusService.getStatusForPipeline("demo"));

    assertTrue(ex.getMessage().contains("StageExecution not found"));
  }

  @Test
  void testJobExecutionNotFound() {
    UUID pipelineId = UUID.randomUUID();
    UUID pipelineExecutionId = UUID.randomUUID();
    UUID stageId = UUID.randomUUID();
    UUID jobId = UUID.randomUUID();

    PipelineEntity pipeline = new PipelineEntity();
    pipeline.setId(pipelineId);
    pipeline.setName("demo");

    PipelineExecutionEntity pipelineExecution = new PipelineExecutionEntity();
    pipelineExecution.setId(pipelineExecutionId);
    pipelineExecution.setPipelineId(pipelineId);

    StageEntity stage = new StageEntity();
    stage.setId(stageId);
    stage.setName("Build");
    stage.setPipelineId(pipelineId);

    StageExecutionEntity stageExecution = new StageExecutionEntity();
    stageExecution.setId(UUID.randomUUID());
    stageExecution.setStageId(stageId);
    stageExecution.setPipelineExecutionId(pipelineExecutionId);
    stageExecution.setStatus(ExecutionStatus.SUCCESS);

    JobEntity job = new JobEntity();
    job.setId(jobId);
    job.setName("Compile");
    job.setStageId(stageId);

    when(pipelineRepository.findByName("demo")).thenReturn(Optional.of(pipeline));
    when(pipelineExecutionRepository.findByPipelineId(pipelineId)).thenReturn(Optional.of(pipelineExecution));
    when(stageRepository.findByPipelineId(pipelineId)).thenReturn(List.of(stage));
    when(stageExecutionRepository.findByStageIdAndPipelineExecutionId(stageId, pipelineExecutionId)).thenReturn(Optional.of(stageExecution));
    when(jobRepository.findByStageId(stageId)).thenReturn(List.of(job));
    when(jobExecutionRepository.findByJobId(jobId)).thenReturn(Optional.empty());

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
        statusService.getStatusForPipeline("demo"));

    assertTrue(ex.getMessage().contains("JobExecution not found"));
  }
}
