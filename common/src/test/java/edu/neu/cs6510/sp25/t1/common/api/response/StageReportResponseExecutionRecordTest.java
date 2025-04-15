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
 * Unit tests for {@link StageReportResponse.ExecutionRecord}.
 */
class StageReportResponseExecutionRecordTest {

  private static final String EXECUTION_ID = "exec-123";
  private static final String STATUS = "SUCCESS";

  @Test
  void constructor_WithValidParameters_ShouldCreateInstanceWithCorrectValues() {
    // Act
    StageReportResponse.ExecutionRecord record = new StageReportResponse.ExecutionRecord(
            EXECUTION_ID, STATUS);

    // Assert
    assertEquals(EXECUTION_ID, record.getExecutionId(), "Execution ID should match");
    assertEquals(STATUS, record.getStatus(), "Status should match");
  }

  @Test
  void constructor_WithNullParameters_ShouldCreateInstanceWithNullValues() {
    // Act
    StageReportResponse.ExecutionRecord record = new StageReportResponse.ExecutionRecord(
            null, null);

    // Assert
    assertNull(record.getExecutionId(), "Execution ID should be null");
    assertNull(record.getStatus(), "Status should be null");
  }

  static Stream<Arguments> provideParameterVariations() {
    return Stream.of(
            // executionId, status
            Arguments.of("", ""),
            Arguments.of("exec-1", null),
            Arguments.of(null, "RUNNING"),
            Arguments.of("exec-2", "FAILED"),
            Arguments.of("exec-3", "CANCELED")
    );
  }

  @ParameterizedTest
  @MethodSource("provideParameterVariations")
  void constructor_WithVariousParameters_ShouldCreateCorrectInstance(
          String executionId, String status) {

    // Act
    StageReportResponse.ExecutionRecord record = new StageReportResponse.ExecutionRecord(
            executionId, status);

    // Assert
    assertEquals(executionId, record.getExecutionId(), "Execution ID should match");
    assertEquals(status, record.getStatus(), "Status should match");
  }

  @Test
  void getters_ShouldReturnCorrectValues() {
    // Arrange
    StageReportResponse.ExecutionRecord record = new StageReportResponse.ExecutionRecord(
            EXECUTION_ID, STATUS);

    // Act & Assert
    assertEquals(EXECUTION_ID, record.getExecutionId());
    assertEquals(STATUS, record.getStatus());
    // Testing each getter individually ensures complete method coverage
  }

  @Test
  void jsonSerialization_ShouldSerializeCorrectly() throws Exception {
    // Arrange
    StageReportResponse.ExecutionRecord record = new StageReportResponse.ExecutionRecord(
            EXECUTION_ID, STATUS);
    ObjectMapper objectMapper = new ObjectMapper();

    // Act
    String json = objectMapper.writeValueAsString(record);

    // Assert
    assertTrue(json.contains("\"executionId\":\"" + EXECUTION_ID + "\""));
    assertTrue(json.contains("\"status\":\"" + STATUS + "\""));
  }

  @Test
  void executionRecord_WithSpecialCharactersInExecutionId_ShouldHandleCorrectly() {
    // Arrange
    String specialId = "exec-123!@#$%^&*()";

    // Act
    StageReportResponse.ExecutionRecord record = new StageReportResponse.ExecutionRecord(
            specialId, STATUS);

    // Assert
    assertEquals(specialId, record.getExecutionId(), "Should handle special characters in execution ID");
  }

  @Test
  void executionRecord_WithLongStatus_ShouldHandleCorrectly() {
    // Arrange
    String longStatus = "VERY_LONG_STATUS_WITH_MANY_WORDS_TO_DESCRIBE_THE_CURRENT_STATE_OF_EXECUTION";

    // Act
    StageReportResponse.ExecutionRecord record = new StageReportResponse.ExecutionRecord(
            EXECUTION_ID, longStatus);

    // Assert
    assertEquals(longStatus, record.getStatus(), "Should handle long status values");
  }
}