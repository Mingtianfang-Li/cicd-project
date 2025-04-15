package edu.neu.cs6510.sp25.t1.common.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link ExecutionLog}.
 */
class ExecutionLogTest {

  private static final UUID ID = UUID.randomUUID();
  private static final UUID PIPELINE_EXECUTION_ID = UUID.randomUUID();
  private static final UUID STAGE_EXECUTION_ID = UUID.randomUUID();
  private static final UUID JOB_EXECUTION_ID = UUID.randomUUID();
  private static final String LOG_TEXT = "Build completed successfully";
  private static final Instant TIMESTAMP = Instant.now();

  @Test
  void constructor_WithValidParameters_ShouldCreateInstanceWithCorrectValues() {
    // Act
    ExecutionLog log = new ExecutionLog(
            ID, PIPELINE_EXECUTION_ID, STAGE_EXECUTION_ID, JOB_EXECUTION_ID, LOG_TEXT, TIMESTAMP);

    // Assert
    assertEquals(ID, log.getId(), "ID should match");
    assertEquals(PIPELINE_EXECUTION_ID, log.getPipelineExecutionId(), "Pipeline execution ID should match");
    assertEquals(STAGE_EXECUTION_ID, log.getStageExecutionId(), "Stage execution ID should match");
    assertEquals(JOB_EXECUTION_ID, log.getJobExecutionId(), "Job execution ID should match");
    assertEquals(LOG_TEXT, log.getLogText(), "Log text should match");
    assertEquals(TIMESTAMP, log.getTimestamp(), "Timestamp should match");
  }

  @Test
  void constructor_WithNullParameters_ShouldCreateInstanceWithNullValues() {
    // Act
    ExecutionLog log = new ExecutionLog(
            ID, null, null, null, null, null);

    // Assert
    assertEquals(ID, log.getId(), "ID should match");
    assertNull(log.getPipelineExecutionId(), "Pipeline execution ID should be null");
    assertNull(log.getStageExecutionId(), "Stage execution ID should be null");
    assertNull(log.getJobExecutionId(), "Job execution ID should be null");
    assertNull(log.getLogText(), "Log text should be null");
    assertNull(log.getTimestamp(), "Timestamp should be null");
  }

  static Stream<Arguments> provideParameterVariations() {
    Instant now = Instant.now();
    Instant past = now.minusSeconds(3600);

    return Stream.of(
            // id, pipelineExecutionId, stageExecutionId, jobExecutionId, logText, timestamp
            Arguments.of(UUID.randomUUID(), null, null, null, "Pipeline log only", now),
            Arguments.of(UUID.randomUUID(), UUID.randomUUID(), null, null, "Pipeline and stage log", now),
            Arguments.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, "No job ID", past),
            Arguments.of(UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID(), "No pipeline ID", past),
            Arguments.of(UUID.randomUUID(), null, null, UUID.randomUUID(), "Job log only", now)
    );
  }

  @ParameterizedTest
  @MethodSource("provideParameterVariations")
  void constructor_WithVariousParameters_ShouldCreateCorrectInstance(
          UUID id, UUID pipelineExecutionId, UUID stageExecutionId,
          UUID jobExecutionId, String logText, Instant timestamp) {

    // Act
    ExecutionLog log = new ExecutionLog(
            id, pipelineExecutionId, stageExecutionId, jobExecutionId, logText, timestamp);

    // Assert
    assertEquals(id, log.getId(), "ID should match");
    assertEquals(pipelineExecutionId, log.getPipelineExecutionId(), "Pipeline execution ID should match");
    assertEquals(stageExecutionId, log.getStageExecutionId(), "Stage execution ID should match");
    assertEquals(jobExecutionId, log.getJobExecutionId(), "Job execution ID should match");
    assertEquals(logText, log.getLogText(), "Log text should match");
    assertEquals(timestamp, log.getTimestamp(), "Timestamp should match");
  }

  @Test
  void getters_ShouldReturnCorrectValues() {
    // Arrange
    ExecutionLog log = new ExecutionLog(
            ID, PIPELINE_EXECUTION_ID, STAGE_EXECUTION_ID, JOB_EXECUTION_ID, LOG_TEXT, TIMESTAMP);

    // Act & Assert
    assertEquals(ID, log.getId());
    assertEquals(PIPELINE_EXECUTION_ID, log.getPipelineExecutionId());
    assertEquals(STAGE_EXECUTION_ID, log.getStageExecutionId());
    assertEquals(JOB_EXECUTION_ID, log.getJobExecutionId());
    assertEquals(LOG_TEXT, log.getLogText());
    assertEquals(TIMESTAMP, log.getTimestamp());
  }

  @Test
  void getId_ShouldNeverReturnNull() {
    // Arrange
    ExecutionLog log = new ExecutionLog(
            ID, null, null, null, null, null);

    // Act & Assert
    assertNotNull(log.getId(), "ID should never be null");
  }

  @Test
  void logText_WithMultilineContent_ShouldHandleCorrectly() {
    // Arrange
    String multilineText = "Line 1\nLine 2\nLine 3";

    // Act
    ExecutionLog log = new ExecutionLog(
            ID, PIPELINE_EXECUTION_ID, STAGE_EXECUTION_ID, JOB_EXECUTION_ID, multilineText, TIMESTAMP);

    // Assert
    assertEquals(multilineText, log.getLogText(), "Should handle multiline log text correctly");
  }

  @Test
  void timestamp_WithDifferentTimezones_ShouldHandleCorrectly() {
    // Arrange
    Instant utcTime = Instant.parse("2023-10-15T12:30:00Z");

    // Act
    ExecutionLog log = new ExecutionLog(
            ID, PIPELINE_EXECUTION_ID, STAGE_EXECUTION_ID, JOB_EXECUTION_ID, LOG_TEXT, utcTime);

    // Assert
    assertEquals(utcTime, log.getTimestamp(), "Should handle UTC time correctly");
    assertEquals(utcTime.getEpochSecond(), log.getTimestamp().getEpochSecond(),
            "Epoch seconds should match regardless of how Instant is created");
  }
}