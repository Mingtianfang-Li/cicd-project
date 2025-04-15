package edu.neu.cs6510.sp25.t1.common.api.response;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link JobReportResponse.ExecutionRecord}.
 */
class JobReportResponseExecutionRecordTest {

  private static final String EXECUTION_ID = "exec-123";
  private static final String STATUS = "SUCCESS";
  private static final String LOGS = "Build completed successfully";

  @Test
  void constructor_WithValidParameters_ShouldCreateInstanceWithCorrectValues() {
    // Act
    JobReportResponse.ExecutionRecord record = new JobReportResponse.ExecutionRecord(
            EXECUTION_ID, STATUS, LOGS);

    // Assert
    assertEquals(EXECUTION_ID, record.getExecutionId(), "Execution ID should match");
    assertEquals(STATUS, record.getStatus(), "Status should match");
    assertEquals(LOGS, record.getLogs(), "Logs should match");
  }

  @Test
  void constructor_WithNullParameters_ShouldCreateInstanceWithNullValues() {
    // Act
    JobReportResponse.ExecutionRecord record = new JobReportResponse.ExecutionRecord(
            null, null, null);

    // Assert
    assertNull(record.getExecutionId(), "Execution ID should be null");
    assertNull(record.getStatus(), "Status should be null");
    assertNull(record.getLogs(), "Logs should be null");
  }

  static Stream<Arguments> provideParameterVariations() {
    return Stream.of(
            // executionId, status, logs
            Arguments.of("", "", ""),
            Arguments.of("exec-1", null, "Build logs here"),
            Arguments.of(null, "RUNNING", null),
            Arguments.of("exec-2", "FAILED", ""),
            Arguments.of("exec-3", "", "Test output with\nmultiple lines")
    );
  }

  @ParameterizedTest
  @MethodSource("provideParameterVariations")
  void constructor_WithVariousParameters_ShouldCreateCorrectInstance(
          String executionId, String status, String logs) {

    // Act
    JobReportResponse.ExecutionRecord record = new JobReportResponse.ExecutionRecord(
            executionId, status, logs);

    // Assert
    assertEquals(executionId, record.getExecutionId(), "Execution ID should match");
    assertEquals(status, record.getStatus(), "Status should match");
    assertEquals(logs, record.getLogs(), "Logs should match");
  }

  @Test
  void getters_ShouldReturnCorrectValues() {
    // Arrange
    JobReportResponse.ExecutionRecord record = new JobReportResponse.ExecutionRecord(
            EXECUTION_ID, STATUS, LOGS);

    // Act & Assert
    assertEquals(EXECUTION_ID, record.getExecutionId());
    assertEquals(STATUS, record.getStatus());
    assertEquals(LOGS, record.getLogs());
    // Testing each getter individually ensures complete method coverage
  }

  @Test
  void jsonSerialization_ShouldSerializeCorrectly() throws Exception {
    // Arrange
    JobReportResponse.ExecutionRecord record = new JobReportResponse.ExecutionRecord(
            EXECUTION_ID, STATUS, LOGS);
    ObjectMapper objectMapper = new ObjectMapper();

    // Act
    String json = objectMapper.writeValueAsString(record);

    // Assert
    assertTrue(json.contains("\"executionId\":\"" + EXECUTION_ID + "\""));
    assertTrue(json.contains("\"status\":\"" + STATUS + "\""));
    assertTrue(json.contains("\"logs\":\"" + LOGS + "\""));
  }

  @Test
  void executionRecord_WithComplexLogs_ShouldHandleCorrectly() {
    // Arrange
    String complexLogs = "Line 1\nLine 2\nLine 3\tTabbed content";

    // Act
    JobReportResponse.ExecutionRecord record = new JobReportResponse.ExecutionRecord(
            EXECUTION_ID, STATUS, complexLogs);

    // Assert
    assertEquals(complexLogs, record.getLogs(), "Should handle complex log content with line breaks and tabs");
  }

  @Test
  void executionRecord_WithSpecialCharactersInExecutionId_ShouldHandleCorrectly() {
    // Arrange
    String specialId = "exec-123!@#$%^&*()";

    // Act
    JobReportResponse.ExecutionRecord record = new JobReportResponse.ExecutionRecord(
            specialId, STATUS, LOGS);

    // Assert
    assertEquals(specialId, record.getExecutionId(), "Should handle special characters in execution ID");
  }

  @Test
  void executionRecord_WithLongLogs_ShouldHandleCorrectly() {
    // Arrange
    StringBuilder longLog = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longLog.append("Log line ").append(i).append("\n");
    }

    // Act
    JobReportResponse.ExecutionRecord record = new JobReportResponse.ExecutionRecord(
            EXECUTION_ID, STATUS, longLog.toString());

    // Assert
    assertEquals(longLog.toString(), record.getLogs(), "Should handle long log content");
  }
}