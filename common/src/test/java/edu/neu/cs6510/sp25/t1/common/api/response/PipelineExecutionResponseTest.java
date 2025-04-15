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
 * Unit tests for {@link PipelineExecutionResponse}.
 */
class PipelineExecutionResponseTest {

  private static final String EXECUTION_ID = "pipeline-123";
  private static final String STATUS = "RUNNING";

  @Test
  void constructor_WithValidParameters_ShouldCreateInstanceWithCorrectValues() {
    // Act
    PipelineExecutionResponse response = new PipelineExecutionResponse(EXECUTION_ID, STATUS);

    // Assert
    assertEquals(EXECUTION_ID, response.getExecutionId(), "Execution ID should match");
    assertEquals(STATUS, response.getStatus(), "Status should match");
  }

  @Test
  void constructor_WithNullParameters_ShouldCreateInstanceWithNullValues() {
    // Act
    PipelineExecutionResponse response = new PipelineExecutionResponse(null, null);

    // Assert
    assertNull(response.getExecutionId(), "Execution ID should be null");
    assertNull(response.getStatus(), "Status should be null");
  }

  static Stream<Arguments> provideParameterVariations() {
    return Stream.of(
            // executionId, status
            Arguments.of("", ""),
            Arguments.of("pipeline-1", null),
            Arguments.of(null, "COMPLETED"),
            Arguments.of("pipeline-2", "FAILED"),
            Arguments.of("pipeline-3", "PENDING")
    );
  }

  @ParameterizedTest
  @MethodSource("provideParameterVariations")
  void constructor_WithVariousParameters_ShouldCreateCorrectInstance(
          String executionId, String status) {

    // Act
    PipelineExecutionResponse response = new PipelineExecutionResponse(executionId, status);

    // Assert
    assertEquals(executionId, response.getExecutionId(), "Execution ID should match");
    assertEquals(status, response.getStatus(), "Status should match");
  }

  @Test
  void getters_ShouldReturnCorrectValues() {
    // Arrange
    PipelineExecutionResponse response = new PipelineExecutionResponse(EXECUTION_ID, STATUS);

    // Act & Assert
    assertEquals(EXECUTION_ID, response.getExecutionId());
    assertEquals(STATUS, response.getStatus());
    // Testing each getter individually ensures complete method coverage
  }

  @Test
  void jsonSerialization_ShouldSerializeCorrectly() throws Exception {
    // Arrange
    PipelineExecutionResponse originalResponse = new PipelineExecutionResponse(EXECUTION_ID, STATUS);
    ObjectMapper objectMapper = new ObjectMapper();

    // Act
    String json = objectMapper.writeValueAsString(originalResponse);

    // Assert
    assertTrue(json.contains("\"executionId\":\"" + EXECUTION_ID + "\""));
    assertTrue(json.contains("\"status\":\"" + STATUS + "\""));
  }

  @Test
  void executionId_WithSpecialCharacters_ShouldHandleCorrectly() {
    // Arrange
    String specialId = "pipeline-123!@#$%^&*()";

    // Act
    PipelineExecutionResponse response = new PipelineExecutionResponse(specialId, STATUS);

    // Assert
    assertEquals(specialId, response.getExecutionId(), "Should handle special characters in execution ID");
  }

  @Test
  void status_WithUnusualValues_ShouldHandleCorrectly() {
    // Arrange
    String unusualStatus = "IN_PROGRESS_WITH_WARNINGS";

    // Act
    PipelineExecutionResponse response = new PipelineExecutionResponse(EXECUTION_ID, unusualStatus);

    // Assert
    assertEquals(unusualStatus, response.getStatus(), "Should handle unusual status values");
  }
}