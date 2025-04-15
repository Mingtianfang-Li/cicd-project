package edu.neu.cs6510.sp25.t1.common.api.response;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link JobExecutionResponse}.
 */
class JobExecutionResponseTest {

  private static final String JOB_EXECUTION_ID = "job-123";
  private static final String STATUS = "RUNNING";

  @Test
  void constructor_WithValidParameters_ShouldCreateInstanceWithCorrectValues() {
    // Act
    JobExecutionResponse response = new JobExecutionResponse(JOB_EXECUTION_ID, STATUS);

    // Assert
    assertEquals(JOB_EXECUTION_ID, response.getJobExecutionId(), "Job execution ID should match");
    assertEquals(STATUS, response.getStatus(), "Status should match");
  }

  @Test
  void constructor_WithNullParameters_ShouldCreateInstanceWithNullValues() {
    // Act
    JobExecutionResponse response = new JobExecutionResponse(null, null);

    // Assert
    assertNull(response.getJobExecutionId(), "Job execution ID should be null");
    assertNull(response.getStatus(), "Status should be null");
  }

  static Stream<Arguments> provideParameterVariations() {
    return Stream.of(
            // jobExecutionId, status
            Arguments.of("", ""),
            Arguments.of("job-1", null),
            Arguments.of(null, "COMPLETED"),
            Arguments.of("job-2", "FAILED"),
            Arguments.of("job-3", "PENDING")
    );
  }

  @ParameterizedTest
  @MethodSource("provideParameterVariations")
  void constructor_WithVariousParameters_ShouldCreateCorrectInstance(
          String jobExecutionId, String status) {

    // Act
    JobExecutionResponse response = new JobExecutionResponse(jobExecutionId, status);

    // Assert
    assertEquals(jobExecutionId, response.getJobExecutionId(), "Job execution ID should match");
    assertEquals(status, response.getStatus(), "Status should match");
  }

  @Test
  void getters_ShouldReturnCorrectValues() {
    // Arrange
    JobExecutionResponse response = new JobExecutionResponse(JOB_EXECUTION_ID, STATUS);

    // Act & Assert
    assertEquals(JOB_EXECUTION_ID, response.getJobExecutionId());
    assertEquals(STATUS, response.getStatus());
    // Testing each getter individually ensures complete method coverage
  }

  @Test
  void jsonSerialization_ShouldSerializeAndDeserializeCorrectly() throws Exception {
    // Arrange
    JobExecutionResponse originalResponse = new JobExecutionResponse(JOB_EXECUTION_ID, STATUS);
    ObjectMapper objectMapper = new ObjectMapper();

    // Act
    String json = objectMapper.writeValueAsString(originalResponse);
    JobExecutionResponse deserializedResponse = objectMapper.readValue(json, JobExecutionResponse.class);

    // Assert
    assertEquals(JOB_EXECUTION_ID, deserializedResponse.getJobExecutionId());
    assertEquals(STATUS, deserializedResponse.getStatus());
  }

  @Test
  void jobExecutionId_WithSpecialCharacters_ShouldHandleCorrectly() {
    // Arrange
    String specialId = "job-123!@#$%^&*()";

    // Act
    JobExecutionResponse response = new JobExecutionResponse(specialId, STATUS);

    // Assert
    assertEquals(specialId, response.getJobExecutionId(), "Should handle special characters in job execution ID");
  }

  @Test
  void status_WithUnusualValues_ShouldHandleCorrectly() {
    // Arrange
    String unusualStatus = "IN_PROGRESS_WITH_WARNINGS";

    // Act
    JobExecutionResponse response = new JobExecutionResponse(JOB_EXECUTION_ID, unusualStatus);

    // Assert
    assertEquals(unusualStatus, response.getStatus(), "Should handle unusual status values");
  }
}