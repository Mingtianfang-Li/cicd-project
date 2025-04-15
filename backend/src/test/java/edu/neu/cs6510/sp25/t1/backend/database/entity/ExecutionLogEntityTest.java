package edu.neu.cs6510.sp25.t1.backend.database.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExecutionLogEntityTest {

  @Test
  void testConstructorSetsFieldsCorrectly() {
    // Arrange
    String logText = "Build completed successfully.";
    UUID pipelineId = UUID.randomUUID();
    UUID stageId = UUID.randomUUID();
    UUID jobId = UUID.randomUUID();

    // Act
    ExecutionLogEntity log = new ExecutionLogEntity(logText, pipelineId, stageId, jobId);

    // Assert
    assertEquals(logText, log.getLogText());
    assertEquals(pipelineId, log.getPipelineExecutionId());
    assertEquals(stageId, log.getStageExecutionId());
    assertEquals(jobId, log.getJobExecutionId());

    assertNotNull(log.getTimestamp());
    assertTrue(log.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
  }

  @Test
  void testDefaultConstructorInitializesTimestamp() {
    // Act
    ExecutionLogEntity log = new ExecutionLogEntity();

    // Assert
    assertNotNull(log.getTimestamp());
    assertTrue(log.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
  }

  @Test
  void testSetters() {
    // Arrange
    ExecutionLogEntity log = new ExecutionLogEntity();

    UUID pipelineId = UUID.randomUUID();
    UUID stageId = UUID.randomUUID();
    UUID jobId = UUID.randomUUID();
    String logText = "Stage started";

    // Act
    log.setPipelineExecutionId(pipelineId);
    log.setStageExecutionId(stageId);
    log.setJobExecutionId(jobId);
    log.setLogText(logText);

    // Assert
    assertEquals(pipelineId, log.getPipelineExecutionId());
    assertEquals(stageId, log.getStageExecutionId());
    assertEquals(jobId, log.getJobExecutionId());
    assertEquals(logText, log.getLogText());
  }
}
