
package edu.neu.cs6510.sp25.t1.backend.service.report;


import edu.neu.cs6510.sp25.t1.backend.database.entity.JobExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.PipelineExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.StageExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.database.repository.JobExecutionRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.PipelineExecutionRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.StageExecutionRepository;
import edu.neu.cs6510.sp25.t1.common.dto.JobReportDTO;
import edu.neu.cs6510.sp25.t1.common.dto.PipelineReportDTO;
import edu.neu.cs6510.sp25.t1.common.dto.StageReportDTO;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;




class ReportServiceTest {

  private PipelineExecutionRepository pipelineExecutionRepository;
  private StageExecutionRepository stageExecutionRepository;
  private JobExecutionRepository jobExecutionRepository;
  private ReportService reportService;

  @BeforeEach
  void setUp() {
    pipelineExecutionRepository = mock(PipelineExecutionRepository.class);
    stageExecutionRepository = mock(StageExecutionRepository.class);
    jobExecutionRepository = mock(JobExecutionRepository.class);

    reportService = new ReportService(
        pipelineExecutionRepository,
        stageExecutionRepository,
        jobExecutionRepository
    );
  }

  @Test
  void testGetAvailablePipelines() {
    PipelineExecutionEntity execution = new PipelineExecutionEntity();
    execution.setPipelineId(UUID.randomUUID());

    when(pipelineExecutionRepository.findAll()).thenReturn(List.of(execution));
    when(pipelineExecutionRepository.findPipelineNameByPipelineId(any())).thenReturn(Optional.of("DemoPipeline"));

    List<String> result = reportService.getAvailablePipelines();
    assertEquals(1, result.size());
    assertEquals("DemoPipeline", result.get(0));
  }


  @Test
  void testGetPipelineReports() {
    // Use fixed UUIDs for matching
    UUID pipelineId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID pipelineExecId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    UUID stageExecId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    UUID stageId = UUID.fromString("44444444-4444-4444-4444-444444444444");

    // Create pipeline execution
    PipelineExecutionEntity exec = new PipelineExecutionEntity();
    exec.setId(pipelineExecId);
    exec.setPipelineId(pipelineId);
    exec.setRunNumber(1);
    exec.setCommitHash("abc123");
    Instant start = Instant.parse("2024-04-01T10:00:00Z");
    Instant end = Instant.parse("2024-04-01T10:05:00Z");
    exec.setStartTime(start);
    exec.setCompletionTime(end);

    // Create stage execution
    StageExecutionEntity stage = new StageExecutionEntity();
    stage.setId(stageExecId);
    stage.setStageId(stageId);
    stage.setPipelineExecutionId(pipelineExecId);
    stage.setStatus(ExecutionStatus.SUCCESS);
    stage.setStartTime(start);
    stage.setCompletionTime(end);

    // Mocking repository calls
    when(pipelineExecutionRepository.findByPipelineNameOrderByStartTimeDesc("DemoPipeline"))
        .thenReturn(List.of(exec));
    when(pipelineExecutionRepository.findPipelineNameByPipelineId(pipelineId))
        .thenReturn(Optional.of("DemoPipeline"));
    when(stageExecutionRepository.findByPipelineExecutionId(pipelineExecId))
        .thenReturn(List.of(stage));
    when(stageExecutionRepository.findById(stageExecId))
        .thenReturn(Optional.of(stage));
    when(pipelineExecutionRepository.findById(pipelineExecId))
        .thenReturn(Optional.of(exec));
    when(stageExecutionRepository.findStageNameByStageId(stageId))
        .thenReturn(Optional.of("Build"));
    when(jobExecutionRepository.findByStageExecution(stage))
        .thenReturn(List.of());

    // Act
    List<PipelineReportDTO> reports = reportService.getPipelineReports("DemoPipeline");

    // Assert
    assertEquals(1, reports.size());
    PipelineReportDTO report = reports.get(0);
    assertEquals(1, report.getRunNumber());
    assertEquals("abc123", report.getCommitHash());
    assertEquals(ExecutionStatus.SUCCESS, report.getStatus());
    assertEquals(start, report.getStartTime());
    assertEquals(end, report.getCompletionTime());

    List<StageReportDTO> stages = report.getStages();
    assertEquals(1, stages.size());
    assertEquals("Build", stages.get(0).getName());
    assertEquals(ExecutionStatus.SUCCESS, stages.get(0).getStatus());
    assertTrue(stages.get(0).getJobs().isEmpty());
  }



  @Test
  void testGetPipelineRunSummary_pipelineNotFound() {
    when(pipelineExecutionRepository.findPipelineIdByName("Unknown")).thenReturn(Optional.empty());
    assertThrows(IllegalArgumentException.class,
        () -> reportService.getPipelineRunSummary("Unknown", 1));
  }

  @Test
  void testGetStageReport_stageNotFound() {
    UUID pipelineId = UUID.randomUUID();
    UUID execId = UUID.randomUUID();

    PipelineExecutionEntity exec = new PipelineExecutionEntity();
    exec.setId(execId);
    exec.setPipelineId(pipelineId);
    exec.setRunNumber(1);

    when(pipelineExecutionRepository.findPipelineIdByName("Demo")).thenReturn(Optional.of(pipelineId));
    when(pipelineExecutionRepository.findByPipelineIdAndRunNumber(pipelineId, 1)).thenReturn(Optional.of(exec));
    when(stageExecutionRepository.findByPipelineExecutionIdAndStageNameOrderByStartTimeDesc(execId, "Build")).thenReturn(List.of());

    assertThrows(IllegalArgumentException.class,
        () -> reportService.getStageReport("Demo", 1, "Build"));
  }

  @Test
  void testGetJobReport_jobNotFound() {
    UUID pipelineId = UUID.randomUUID();
    UUID execId = UUID.randomUUID();
    UUID stageExecId = UUID.randomUUID();

    PipelineExecutionEntity exec = new PipelineExecutionEntity();
    exec.setId(execId);
    exec.setPipelineId(pipelineId);
    exec.setRunNumber(1);
    exec.setCommitHash("abc");

    StageExecutionEntity stage = new StageExecutionEntity();
    stage.setId(stageExecId);
    stage.setPipelineExecutionId(execId);

    when(pipelineExecutionRepository.findPipelineIdByName("Demo")).thenReturn(Optional.of(pipelineId));
    when(pipelineExecutionRepository.findByPipelineIdAndRunNumber(pipelineId, 1)).thenReturn(Optional.of(exec));
    when(stageExecutionRepository.findByPipelineExecutionIdAndStageNameOrderByStartTimeDesc(execId, "Build")).thenReturn(List.of(stage));
    when(stageExecutionRepository.findById(stageExecId)).thenReturn(Optional.of(stage));
    when(jobExecutionRepository.findByStageExecutionAndJobNameOrderByStartTimeDesc(stageExecId, "Compile")).thenReturn(List.of());

    assertThrows(IllegalArgumentException.class,
        () -> reportService.getJobReport("Demo", 1, "Build", "Compile"));
  }

  @Test
  void testGetStageReports() {
    UUID pipelineId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID execId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    UUID stageExecId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    UUID stageId = UUID.fromString("44444444-4444-4444-4444-444444444444");

    PipelineExecutionEntity exec = new PipelineExecutionEntity();
    exec.setId(execId);
    exec.setPipelineId(pipelineId);
    exec.setRunNumber(1);
    exec.setCommitHash("abc123");
    Instant start = Instant.parse("2024-04-01T10:00:00Z");
    Instant end = Instant.parse("2024-04-01T10:05:00Z");
    exec.setStartTime(start);
    exec.setCompletionTime(end);

    StageExecutionEntity stage = new StageExecutionEntity();
    stage.setId(stageExecId);
    stage.setStageId(stageId);
    stage.setPipelineExecutionId(execId);
    stage.setStatus(ExecutionStatus.SUCCESS);
    stage.setStartTime(start);
    stage.setCompletionTime(end);

    when(pipelineExecutionRepository.findByPipelineNameOrderByStartTimeDesc("DemoPipeline"))
        .thenReturn(List.of(exec));
    when(stageExecutionRepository.findByPipelineExecutionIdAndStageNameOrderByStartTimeDesc(execId, "Build"))
        .thenReturn(List.of(stage));
    when(stageExecutionRepository.findById(stageExecId))
        .thenReturn(Optional.of(stage));
    when(pipelineExecutionRepository.findById(execId))
        .thenReturn(Optional.of(exec));
    when(pipelineExecutionRepository.findPipelineNameByPipelineId(pipelineId))
        .thenReturn(Optional.of("DemoPipeline"));
    when(stageExecutionRepository.findStageNameByStageId(stageId))
        .thenReturn(Optional.of("Build"));
    when(jobExecutionRepository.findByStageExecution(stage))
        .thenReturn(List.of());

    List<StageReportDTO> reports = reportService.getStageReports("DemoPipeline", "Build");

    assertEquals(1, reports.size());
    assertEquals("Build", reports.get(0).getName());
    assertEquals(ExecutionStatus.SUCCESS, reports.get(0).getStatus());
  }

  @Test
  void testGetJobReportsForStage() {
    UUID pipelineId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID execId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    UUID stageExecId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    UUID stageId = UUID.fromString("44444444-4444-4444-4444-444444444444");
    UUID jobId = UUID.fromString("55555555-5555-5555-5555-555555555555");

    PipelineExecutionEntity exec = new PipelineExecutionEntity();
    exec.setId(execId);
    exec.setPipelineId(pipelineId);
    exec.setRunNumber(1);
    exec.setCommitHash("abc123");

    StageExecutionEntity stage = new StageExecutionEntity();
    stage.setId(stageExecId);
    stage.setStageId(stageId);
    stage.setPipelineExecutionId(execId);
    stage.setStatus(ExecutionStatus.SUCCESS);

    JobExecutionEntity job = new JobExecutionEntity();
    job.setId(UUID.randomUUID());
    job.setJobId(jobId);
    job.setStatus(ExecutionStatus.SUCCESS);
    job.setStartTime(Instant.parse("2024-04-01T10:00:00Z"));
    job.setCompletionTime(Instant.parse("2024-04-01T10:01:00Z"));
    job.setAllowFailure(false);

    when(pipelineExecutionRepository.findByPipelineNameOrderByStartTimeDesc("DemoPipeline"))
        .thenReturn(List.of(exec));
    when(stageExecutionRepository.findByPipelineExecutionIdAndStageNameOrderByStartTimeDesc(execId, "Build"))
        .thenReturn(List.of(stage));
    when(stageExecutionRepository.findById(stageExecId))
        .thenReturn(Optional.of(stage));
    when(jobExecutionRepository.findByStageExecutionAndJobNameOrderByStartTimeDesc(stageExecId, "Compile"))
        .thenReturn(List.of(job));
    when(jobExecutionRepository.findJobNameByJobId(jobId))
        .thenReturn(Optional.of("Compile"));

    List<JobReportDTO> reports = reportService.getJobReportsForStage("DemoPipeline", "Build", "Compile");

    assertEquals(1, reports.size());
    JobReportDTO report = reports.get(0);
    assertEquals("Compile", report.getName());
    assertEquals("DemoPipeline", report.getPipelineName());
    assertEquals(1, report.getRunNumber());
    assertEquals("abc123", report.getCommitHash());
    assertEquals("Build", report.getStageName());
    assertEquals(1, report.getExecutions().size());
    assertEquals(ExecutionStatus.SUCCESS, report.getExecutions().get(0).getStatus());
  }

  @Test
  void testGetPipelineRunSummary_success() {
    UUID pipelineId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID pipelineExecId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    UUID stageExecId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    UUID stageId = UUID.fromString("44444444-4444-4444-4444-444444444444");

    // Setup execution time
    Instant start = Instant.parse("2024-04-01T10:00:00Z");
    Instant end = Instant.parse("2024-04-01T10:05:00Z");

    // Pipeline execution entity
    PipelineExecutionEntity pipelineExecution = new PipelineExecutionEntity();
    pipelineExecution.setId(pipelineExecId);
    pipelineExecution.setPipelineId(pipelineId);
    pipelineExecution.setRunNumber(1);
    pipelineExecution.setCommitHash("abc123");
    pipelineExecution.setStartTime(start);
    pipelineExecution.setCompletionTime(end);

    // Stage execution entity
    StageExecutionEntity stageExecution = new StageExecutionEntity();
    stageExecution.setId(stageExecId);
    stageExecution.setStageId(stageId);
    stageExecution.setPipelineExecutionId(pipelineExecId);
    stageExecution.setStatus(ExecutionStatus.SUCCESS);
    stageExecution.setStartTime(start);
    stageExecution.setCompletionTime(end);

    // Mocking
    when(pipelineExecutionRepository.findPipelineIdByName("DemoPipeline"))
        .thenReturn(Optional.of(pipelineId));
    when(pipelineExecutionRepository.findByPipelineIdAndRunNumber(pipelineId, 1))
        .thenReturn(Optional.of(pipelineExecution));
    when(stageExecutionRepository.findByPipelineExecutionId(pipelineExecId))
        .thenReturn(List.of(stageExecution));
    when(stageExecutionRepository.findById(stageExecId))
        .thenReturn(Optional.of(stageExecution));
    when(pipelineExecutionRepository.findById(pipelineExecId))
        .thenReturn(Optional.of(pipelineExecution));
    when(pipelineExecutionRepository.findPipelineNameByPipelineId(pipelineId))
        .thenReturn(Optional.of("DemoPipeline"));
    when(stageExecutionRepository.findStageNameByStageId(stageId))
        .thenReturn(Optional.of("Build"));
    when(jobExecutionRepository.findByStageExecution(stageExecution))
        .thenReturn(List.of());

    // Execute
    PipelineReportDTO report = reportService.getPipelineRunSummary("DemoPipeline", 1);

    // Assert
    assertEquals(1, report.getRunNumber());
    assertEquals("abc123", report.getCommitHash());
    assertEquals(start, report.getStartTime());
    assertEquals(end, report.getCompletionTime());
    assertEquals(ExecutionStatus.SUCCESS, report.getStatus());

    List<StageReportDTO> stageReports = report.getStages();
    assertEquals(1, stageReports.size());
    assertEquals("Build", stageReports.get(0).getName());
    assertTrue(stageReports.get(0).getJobs().isEmpty());
  }


  @Test
  void testGetJobReport_success() {
    UUID pipelineId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID execId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    UUID stageId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    UUID stageExecId = UUID.fromString("44444444-4444-4444-4444-444444444444");
    UUID jobId = UUID.fromString("55555555-5555-5555-5555-555555555555");

    Instant start = Instant.parse("2024-04-01T10:00:00Z");
    Instant end = Instant.parse("2024-04-01T10:01:00Z");

    // Pipeline Execution
    PipelineExecutionEntity pipelineExecution = new PipelineExecutionEntity();
    pipelineExecution.setId(execId);
    pipelineExecution.setPipelineId(pipelineId);
    pipelineExecution.setRunNumber(7);
    pipelineExecution.setCommitHash("abcd1234");

    // Stage Execution
    StageExecutionEntity stageExecution = new StageExecutionEntity();
    stageExecution.setId(stageExecId);
    stageExecution.setStageId(stageId);
    stageExecution.setPipelineExecutionId(execId);

    // Job Execution
    JobExecutionEntity jobExecution = new JobExecutionEntity();
    jobExecution.setId(UUID.randomUUID());
    jobExecution.setJobId(jobId);
    jobExecution.setStatus(ExecutionStatus.SUCCESS);
    jobExecution.setStartTime(start);
    jobExecution.setCompletionTime(end);
    jobExecution.setAllowFailure(true);

    // Mock behavior
    when(pipelineExecutionRepository.findPipelineIdByName("DemoPipeline"))
        .thenReturn(Optional.of(pipelineId));
    when(pipelineExecutionRepository.findByPipelineIdAndRunNumber(pipelineId, 7))
        .thenReturn(Optional.of(pipelineExecution));
    when(stageExecutionRepository.findByPipelineExecutionIdAndStageNameOrderByStartTimeDesc(execId, "Build"))
        .thenReturn(List.of(stageExecution));
    when(stageExecutionRepository.findById(stageExecId))
        .thenReturn(Optional.of(stageExecution));
    when(jobExecutionRepository.findByStageExecutionAndJobNameOrderByStartTimeDesc(stageExecId, "Compile"))
        .thenReturn(List.of(jobExecution));
    when(jobExecutionRepository.findJobNameByJobId(jobId))
        .thenReturn(Optional.of("Compile"));

    // Call service
    JobReportDTO report = reportService.getJobReport("DemoPipeline", 7, "Build", "Compile");

    // Assert
    assertEquals("Compile", report.getName());
    assertEquals("DemoPipeline", report.getPipelineName());
    assertEquals("abcd1234", report.getCommitHash());
    assertEquals("Build", report.getStageName());
    assertEquals(7, report.getRunNumber());

    assertEquals(1, report.getExecutions().size());
    JobReportDTO.ExecutionRecord record = report.getExecutions().get(0);
    assertEquals(ExecutionStatus.SUCCESS, record.getStatus());
    assertEquals(start, record.getStartTime());
    assertEquals(end, record.getCompletionTime());
    assertTrue(record.isAllowFailure());
  }

  @Test
  void testGetStageReport_withJobs() {
    UUID pipelineId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID execId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    UUID stageExecId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    UUID stageId = UUID.fromString("44444444-4444-4444-4444-444444444444");
    UUID jobId = UUID.fromString("55555555-5555-5555-5555-555555555555");

    Instant start = Instant.parse("2024-04-01T10:00:00Z");
    Instant end = Instant.parse("2024-04-01T10:01:00Z");

    // Setup pipeline execution
    PipelineExecutionEntity pipelineExecution = new PipelineExecutionEntity();
    pipelineExecution.setId(execId);
    pipelineExecution.setPipelineId(pipelineId);
    pipelineExecution.setRunNumber(3);
    pipelineExecution.setCommitHash("def456");

    // Setup stage execution
    StageExecutionEntity stageExecution = new StageExecutionEntity();
    stageExecution.setId(stageExecId);
    stageExecution.setStageId(stageId);
    stageExecution.setPipelineExecutionId(execId);
    stageExecution.setStatus(ExecutionStatus.SUCCESS);
    stageExecution.setStartTime(start);
    stageExecution.setCompletionTime(end);

    // Setup job execution
    JobExecutionEntity job = new JobExecutionEntity();
    job.setId(UUID.randomUUID());
    job.setJobId(jobId);
    job.setStatus(ExecutionStatus.SUCCESS);
    job.setStartTime(start);
    job.setCompletionTime(end);
    job.setAllowFailure(false);

    // Mock behavior
    when(pipelineExecutionRepository.findPipelineIdByName("PipelineX"))
        .thenReturn(Optional.of(pipelineId));

    when(pipelineExecutionRepository.findByPipelineIdAndRunNumber(pipelineId, 3))
        .thenReturn(Optional.of(pipelineExecution));

    when(stageExecutionRepository.findByPipelineExecutionIdAndStageNameOrderByStartTimeDesc(execId, "TestStage"))
        .thenReturn(List.of(stageExecution));

    when(stageExecutionRepository.findById(stageExecId))
        .thenReturn(Optional.of(stageExecution));

    when(pipelineExecutionRepository.findById(execId))
        .thenReturn(Optional.of(pipelineExecution));

    when(pipelineExecutionRepository.findPipelineNameByPipelineId(pipelineId))
        .thenReturn(Optional.of("PipelineX"));

    when(stageExecutionRepository.findStageNameByStageId(stageId))
        .thenReturn(Optional.of("TestStage"));

    when(jobExecutionRepository.findByStageExecution(stageExecution))
        .thenReturn(List.of(job));

    when(jobExecutionRepository.findJobNameByJobId(jobId))
        .thenReturn(Optional.of("RunJob"));

    // Call the public method
    StageReportDTO report = reportService.getStageReport("PipelineX", 3, "TestStage");

    // Assertions
    assertEquals("TestStage", report.getName());
    assertEquals(ExecutionStatus.SUCCESS, report.getStatus());
    assertEquals(start, report.getStartTime());
    assertEquals(end, report.getCompletionTime());

    List<JobReportDTO> jobReports = report.getJobs();
    assertEquals(1, jobReports.size());

    JobReportDTO jobReport = jobReports.get(0);
    assertEquals("RunJob", jobReport.getName());
    assertEquals("PipelineX", jobReport.getPipelineName());
    assertEquals("TestStage", jobReport.getStageName());
    assertEquals(3, jobReport.getRunNumber());
    assertEquals("def456", jobReport.getCommitHash());

    JobReportDTO.ExecutionRecord record = jobReport.getExecutions().get(0);
    assertEquals(ExecutionStatus.SUCCESS, record.getStatus());
    assertEquals(start, record.getStartTime());
    assertEquals(end, record.getCompletionTime());
    assertFalse(record.isAllowFailure());
  }

  @Test
  void testPipelineStatus_withFailedStage() {
    StageExecutionEntity stage1 = new StageExecutionEntity();
    stage1.setStatus(ExecutionStatus.SUCCESS);
    StageExecutionEntity stage2 = new StageExecutionEntity();
    stage2.setStatus(ExecutionStatus.FAILED);

    // Assume repo returns these stages
    when(stageExecutionRepository.findByPipelineExecutionId(any()))
        .thenReturn(List.of(stage1, stage2));

    // All other repo mocks needed for getPipelineReports()...
    // You can copy base from earlier examples

    ExecutionStatus result = invokeStatusCalculation(List.of(stage1, stage2));
    assertEquals(ExecutionStatus.FAILED, result);
  }

  @Test
  void testPipelineStatus_withCanceledStage() {
    StageExecutionEntity stage = new StageExecutionEntity();
    stage.setStatus(ExecutionStatus.CANCELED);

    ExecutionStatus result = invokeStatusCalculation(List.of(stage));
    assertEquals(ExecutionStatus.CANCELED, result);
  }

  @Test
  void testPipelineStatus_withRunningStage() {
    StageExecutionEntity stage = new StageExecutionEntity();
    stage.setStatus(ExecutionStatus.RUNNING);

    ExecutionStatus result = invokeStatusCalculation(List.of(stage));
    assertEquals(ExecutionStatus.RUNNING, result);
  }


  @Test
  void testPipelineStatus_withPendingStage() {
    StageExecutionEntity stage = new StageExecutionEntity();
    stage.setStatus(ExecutionStatus.PENDING);

    ExecutionStatus result = invokeStatusCalculation(List.of(stage));
    assertEquals(ExecutionStatus.PENDING, result);
  }

  @Test
  void testPipelineStatus_withAllSuccessStages() {
    StageExecutionEntity stage1 = new StageExecutionEntity();
    stage1.setStatus(ExecutionStatus.SUCCESS);
    StageExecutionEntity stage2 = new StageExecutionEntity();
    stage2.setStatus(ExecutionStatus.SUCCESS);

    ExecutionStatus result = invokeStatusCalculation(List.of(stage1, stage2));
    assertEquals(ExecutionStatus.SUCCESS, result);
  }

  @Test
  void testPipelineStatus_withEmptyStages() {
    ExecutionStatus result = invokeStatusCalculation(Collections.emptyList());
    assertEquals(ExecutionStatus.PENDING, result);
  }

  private ExecutionStatus invokeStatusCalculation(List<StageExecutionEntity> stages) {
    try {
      Method method = ReportService.class.getDeclaredMethod("calculatePipelineStatus", List.class);
      method.setAccessible(true);
      return (ExecutionStatus) method.invoke(reportService, stages);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
