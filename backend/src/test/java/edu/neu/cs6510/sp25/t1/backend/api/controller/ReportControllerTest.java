package edu.neu.cs6510.sp25.t1.backend.api.controller;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import edu.neu.cs6510.sp25.t1.backend.service.report.ReportService;
import edu.neu.cs6510.sp25.t1.common.dto.JobReportDTO;
import edu.neu.cs6510.sp25.t1.common.dto.PipelineReportDTO;
import edu.neu.cs6510.sp25.t1.common.dto.StageReportDTO;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;

@ExtendWith(MockitoExtension.class)
public class ReportControllerTest {

  @Mock
  private ReportService reportService;

  @InjectMocks
  private ReportController reportController;

  private List<String> pipelineNames;
  private List<PipelineReportDTO> pipelineReports;
  private PipelineReportDTO pipelineReport;
  private StageReportDTO stageReport;
  private JobReportDTO jobReport;
  private List<StageReportDTO> stageReports;
  private List<JobReportDTO> jobReports;

  @BeforeEach
  public void setUp() {
    // Setup test data
    pipelineNames = Arrays.asList("pipeline1", "pipeline2");

    // Create job report
    jobReport = new JobReportDTO("compile", Collections.emptyList());
    jobReport.setPipelineName("pipeline1");
    jobReport.setRunNumber(1);
    jobReport.setCommitHash("abc123");
    jobReport.setStageName("build");

    // Create stage report
    stageReport = new StageReportDTO(
            UUID.randomUUID(),
            "build",
            ExecutionStatus.SUCCESS,
            Instant.now(),
            Instant.now().plusSeconds(60),
            Arrays.asList(jobReport)
    );

    // Create pipeline report
    pipelineReport = new PipelineReportDTO(
            UUID.randomUUID(),
            "pipeline1",
            1,
            "abc123",
            ExecutionStatus.SUCCESS,
            Instant.now(),
            Instant.now().plusSeconds(120),
            Arrays.asList(stageReport)
    );

    pipelineReports = Arrays.asList(pipelineReport);
    stageReports = Arrays.asList(stageReport);
    jobReports = Arrays.asList(jobReport);
  }

  @Test
  public void testGetAvailablePipelines() {
    // Arrange
    when(reportService.getAvailablePipelines()).thenReturn(pipelineNames);

    // Act
    List<String> result = reportController.getAvailablePipelines();

    // Assert
    assertEquals(pipelineNames, result);
    verify(reportService).getAvailablePipelines();
  }

  @Test
  public void testGetPipelineExecutionHistory_NullStageAndJob() {
    // Arrange
    String pipelineName = "pipeline1";
    when(reportService.getPipelineReports(pipelineName)).thenReturn(pipelineReports);

    // Act
    ResponseEntity<?> response = reportController.getPipelineExecutionHistory(pipelineName, null, null);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(pipelineReports, response.getBody());
    verify(reportService).getPipelineReports(pipelineName);
  }

  @Test
  public void testGetPipelineExecutionHistory_WithStageNoJob() {
    // Arrange
    String pipelineName = "pipeline1";
    String stageName = "build";
    when(reportService.getStageReports(pipelineName, stageName)).thenReturn(stageReports);

    // Act
    ResponseEntity<?> response = reportController.getPipelineExecutionHistory(pipelineName, stageName, null);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(stageReports, response.getBody());
    verify(reportService).getStageReports(pipelineName, stageName);
  }

  @Test
  public void testGetPipelineExecutionHistory_WithStageAndJob() {
    // Arrange
    String pipelineName = "pipeline1";
    String stageName = "build";
    String jobName = "compile";
    when(reportService.getJobReportsForStage(pipelineName, stageName, jobName)).thenReturn(jobReports);

    // Act
    ResponseEntity<?> response = reportController.getPipelineExecutionHistory(pipelineName, stageName, jobName);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(jobReports, response.getBody());
    verify(reportService).getJobReportsForStage(pipelineName, stageName, jobName);
  }

  @Test
  public void testGetPipelineExecutionHistory_ThrowsException() {
    // Arrange
    String pipelineName = "pipeline1";
    when(reportService.getPipelineReports(pipelineName)).thenThrow(new RuntimeException("Test error"));

    // Act
    ResponseEntity<?> response = reportController.getPipelineExecutionHistory(pipelineName, null, null);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertTrue(response.getBody() instanceof Map);
    Map<String, String> errorMap = (Map<String, String>) response.getBody();
    assertTrue(errorMap.containsKey("error"));
    assertTrue(errorMap.get("error").contains("Test error"));
  }

  @Test
  public void testGetPipelineExecutionSummary() {
    // Arrange
    String pipelineName = "pipeline1";
    int runNumber = 1;
    when(reportService.getPipelineRunSummary(pipelineName, runNumber)).thenReturn(pipelineReport);

    // Act
    PipelineReportDTO result = reportController.getPipelineExecutionSummary(pipelineName, runNumber);

    // Assert
    assertEquals(pipelineReport, result);
    verify(reportService).getPipelineRunSummary(pipelineName, runNumber);
  }

  @Test
  public void testGetStageReport() {
    // Arrange
    String pipelineName = "pipeline1";
    int runNumber = 1;
    String stageName = "build";
    when(reportService.getStageReport(pipelineName, runNumber, stageName)).thenReturn(stageReport);

    // Act
    StageReportDTO result = reportController.getStageReport(pipelineName, runNumber, stageName);

    // Assert
    assertEquals(stageReport, result);
    verify(reportService).getStageReport(pipelineName, runNumber, stageName);
  }

  @Test
  public void testGetJobReport() {
    // Arrange
    String pipelineName = "pipeline1";
    int runNumber = 1;
    String stageName = "build";
    String jobName = "compile";
    when(reportService.getJobReport(pipelineName, runNumber, stageName, jobName)).thenReturn(jobReport);

    // Act
    JobReportDTO result = reportController.getJobReport(pipelineName, runNumber, stageName, jobName);

    // Assert
    assertEquals(jobReport, result);
    verify(reportService).getJobReport(pipelineName, runNumber, stageName, jobName);
  }
}