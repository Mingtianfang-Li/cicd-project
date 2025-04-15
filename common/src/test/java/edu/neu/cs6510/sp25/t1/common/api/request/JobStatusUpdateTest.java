package edu.neu.cs6510.sp25.t1.common.api.request;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link JobStatusUpdate}.
 */
class JobStatusUpdateTest {

  private static final UUID JOB_EXECUTION_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final ExecutionStatus STATUS = ExecutionStatus.RUNNING;
  private static final String LOGS = "Build in progress...\nCompiling source files...\nTests passed.";

  @Test
  void constructor_WithValidParameters_ShouldCreateInstanceWithCorrectValues() {
    // Act
    JobStatusUpdate update = new JobStatusUpdate(JOB_EXECUTION_ID, STATUS, LOGS);

    // Assert
    assertEquals(JOB_EXECUTION_ID, update.getJobExecutionId(), "Job execution ID should match");
    assertEquals(STATUS, update.getStatus(), "Status should match");
    assertEquals(LOGS, update.getLogs(), "Logs should match");
  }

  @Test
  void defaultConstructor_ShouldCreateEmptyInstance() {
    // Act
    JobStatusUpdate update = new JobStatusUpdate();

    // Assert
    assertNull(update.getJobExecutionId(), "Job execution ID should be null");
    assertNull(update.getStatus(), "Status should be null");
    assertNull(update.getLogs(), "Logs should be null");
  }

  @Test
  void setters_ShouldUpdateValues() {
    // Arrange
    JobStatusUpdate update = new JobStatusUpdate();

    // Act
    update.setJobExecutionId(JOB_EXECUTION_ID);
    update.setStatus(STATUS);
    update.setLogs(LOGS);

    // Assert
    assertEquals(JOB_EXECUTION_ID, update.getJobExecutionId(), "Job execution ID should be updated");
    assertEquals(STATUS, update.getStatus(), "Status should be updated");
    assertEquals(LOGS, update.getLogs(), "Logs should be updated");
  }

  @ParameterizedTest
  @EnumSource(ExecutionStatus.class)
  void constructor_WithDifferentStatusValues_ShouldCreateCorrectInstance(ExecutionStatus status) {
    // Act
    JobStatusUpdate update = new JobStatusUpdate(JOB_EXECUTION_ID, status, LOGS);

    // Assert
    assertEquals(status, update.getStatus(), "Status should match the provided value");
    assertEquals(JOB_EXECUTION_ID, update.getJobExecutionId(), "Job execution ID should match");
    assertEquals(LOGS, update.getLogs(), "Logs should match");
  }

  static Stream<Arguments> provideParameterVariations() {
    return Stream.of(
            // jobExecutionId, status, logs
            Arguments.of(null, STATUS, LOGS),
            Arguments.of(JOB_EXECUTION_ID, null, LOGS),
            Arguments.of(JOB_EXECUTION_ID, STATUS, null),
            Arguments.of(JOB_EXECUTION_ID, STATUS, ""),
            Arguments.of(JOB_EXECUTION_ID, ExecutionStatus.PENDING, "Job is waiting to start"),
            Arguments.of(JOB_EXECUTION_ID, ExecutionStatus.SUCCESS, "Build completed successfully"),
            Arguments.of(JOB_EXECUTION_ID, ExecutionStatus.FAILED, "Build failed with error code 1"),
            Arguments.of(JOB_EXECUTION_ID, ExecutionStatus.CANCELED, "Build was canceled due to timeout")
    );
  }

  @ParameterizedTest
  @MethodSource("provideParameterVariations")
  void constructor_WithVariousParameters_ShouldCreateCorrectInstance(
          UUID jobExecutionId, ExecutionStatus status, String logs) {

    // Act
    JobStatusUpdate update = new JobStatusUpdate(jobExecutionId, status, logs);

    // Assert
    assertEquals(jobExecutionId, update.getJobExecutionId(), "Job execution ID should match");
    assertEquals(status, update.getStatus(), "Status should match");
    assertEquals(logs, update.getLogs(), "Logs should match");
  }

  @Test
  void jsonSerialization_ShouldSerializeAndDeserializeCorrectly() throws Exception {
    // Arrange
    JobStatusUpdate originalUpdate = new JobStatusUpdate(JOB_EXECUTION_ID, STATUS, LOGS);
    ObjectMapper objectMapper = new ObjectMapper();

    // Act
    String json = objectMapper.writeValueAsString(originalUpdate);
    JobStatusUpdate deserializedUpdate = objectMapper.readValue(json, JobStatusUpdate.class);

    // Assert
    assertEquals(JOB_EXECUTION_ID, deserializedUpdate.getJobExecutionId(), "Job execution ID should match after deserialization");
    assertEquals(STATUS, deserializedUpdate.getStatus(), "Status should match after deserialization");
    assertEquals(LOGS, deserializedUpdate.getLogs(), "Logs should match after deserialization");
  }

  @Test
  void jsonPropertyAnnotatedSetters_ShouldWorkWithDeserialization() throws Exception {
    // Arrange
    String json = "{\"jobExecutionId\":\"123e4567-e89b-12d3-a456-426614174000\",\"status\":\"RUNNING\",\"logs\":\"Test logs\"}";
    ObjectMapper objectMapper = new ObjectMapper();

    // Act
    JobStatusUpdate deserializedUpdate = objectMapper.readValue(json, JobStatusUpdate.class);

    // Assert
    assertEquals(JOB_EXECUTION_ID, deserializedUpdate.getJobExecutionId(), "JobExecutionId should be set via annotated setter");
    assertEquals(STATUS, deserializedUpdate.getStatus(), "Status should be set via annotated setter");
    assertEquals("Test logs", deserializedUpdate.getLogs(), "Logs should be set via annotated setter");
  }

  @Test
  void deserializeAllStatusValues_ShouldWorkCorrectly() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();

    for (ExecutionStatus status : ExecutionStatus.values()) {
      // Arrange
      String json = String.format(
              "{\"jobExecutionId\":\"%s\",\"status\":\"%s\",\"logs\":\"Test logs\"}",
              JOB_EXECUTION_ID, status.name()
      );

      // Act
      JobStatusUpdate deserializedUpdate = objectMapper.readValue(json, JobStatusUpdate.class);

      // Assert
      assertEquals(status, deserializedUpdate.getStatus(),
              "Status " + status.name() + " should deserialize correctly");
    }
  }

  @Test
  void statusDescription_ShouldMatchEnumDescription() {
    // For each execution status
    for (ExecutionStatus status : ExecutionStatus.values()) {
      // Act
      JobStatusUpdate update = new JobStatusUpdate(JOB_EXECUTION_ID, status, LOGS);

      // Assert
      String expectedDescription = status.getDescription();
      ExecutionStatus actualStatus = update.getStatus();

      assertEquals(expectedDescription, actualStatus.getDescription(),
              "Description for status " + status + " should match the enum's description");
    }
  }

  @Test
  void longLogs_ShouldHandleCorrectly() {
    // Arrange
    StringBuilder longLogBuilder = new StringBuilder();
    for (int i = 0; i < 100; i++) {
      longLogBuilder.append("Line ").append(i).append(": Build log output for testing long content.\n");
    }
    String longLogs = longLogBuilder.toString();

    // Act
    JobStatusUpdate update = new JobStatusUpdate(JOB_EXECUTION_ID, STATUS, longLogs);

    // Assert
    assertEquals(longLogs, update.getLogs(), "Should handle long log content correctly");
  }
}