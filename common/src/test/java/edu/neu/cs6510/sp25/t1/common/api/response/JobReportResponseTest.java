package edu.neu.cs6510.sp25.t1.common.api.response;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link JobReportResponse}.
 */
class JobReportResponseTest {

  private static final String JOB_NAME = "build-job";
  private static final List<JobReportResponse.ExecutionRecord> EXECUTIONS = Arrays.asList(
          new JobReportResponse.ExecutionRecord("exec-1", "SUCCESS", "Build completed successfully"),
          new JobReportResponse.ExecutionRecord("exec-2", "FAILED", "Build failed due to compilation errors")
  );

  @Test
  void constructor_WithValidParameters_ShouldCreateInstanceWithCorrectValues() {
    // Act
    JobReportResponse response = new JobReportResponse(JOB_NAME, EXECUTIONS);

    // Assert
    assertEquals(JOB_NAME, response.getJobName(), "Job name should match");
    assertEquals(EXECUTIONS, response.getExecutions(), "Executions list should match");
    assertEquals(2, response.getExecutions().size(), "Executions list should contain 2 items");
  }

  @Test
  void constructor_WithNullParameters_ShouldCreateInstanceWithNullValues() {
    // Act
    JobReportResponse response = new JobReportResponse(null, null);

    // Assert
    assertNull(response.getJobName(), "Job name should be null");
    assertNull(response.getExecutions(), "Executions list should be null");
  }

  static Stream<Arguments> provideParameterVariations() {
    return Stream.of(
            // jobName, executions
            Arguments.of("", Collections.emptyList()),
            Arguments.of("test-job", null),
            Arguments.of(null, new ArrayList<>()),
            Arguments.of("deploy-job", List.of(
                    new JobReportResponse.ExecutionRecord("exec-3", "SUCCESS", "")
            ))
    );
  }

  @ParameterizedTest
  @MethodSource("provideParameterVariations")
  void constructor_WithVariousParameters_ShouldCreateCorrectInstance(
          String jobName, List<JobReportResponse.ExecutionRecord> executions) {

    // Act
    JobReportResponse response = new JobReportResponse(jobName, executions);

    // Assert
    assertEquals(jobName, response.getJobName(), "Job name should match");
    assertEquals(executions, response.getExecutions(), "Executions list should match");
  }

  @Test
  void getters_ShouldReturnCorrectValues() {
    // Arrange
    JobReportResponse response = new JobReportResponse(JOB_NAME, EXECUTIONS);

    // Act & Assert
    assertEquals(JOB_NAME, response.getJobName());
    assertEquals(EXECUTIONS, response.getExecutions());
    // Testing each getter individually ensures complete method coverage
  }

  @Test
  void jsonSerialization_ShouldSerializeCorrectly() throws Exception {
    // Arrange
    JobReportResponse originalResponse = new JobReportResponse(JOB_NAME, EXECUTIONS);
    ObjectMapper objectMapper = new ObjectMapper();

    // Act
    String json = objectMapper.writeValueAsString(originalResponse);

    // Assert - verify json contains expected values
    assertTrue(json.contains("\"jobName\":\"" + JOB_NAME + "\""));
    assertTrue(json.contains("\"executionId\":\"exec-1\""));
    assertTrue(json.contains("\"status\":\"SUCCESS\""));
    assertTrue(json.contains("\"status\":\"FAILED\""));
  }

  @Test
  void jsonDeserialization_ShouldDeserializeCorrectly() throws Exception {
    // Arrange
    String json = "{\"jobName\":\"build-job\",\"executions\":[{\"executionId\":\"exec-1\",\"status\":\"SUCCESS\",\"logs\":\"Build completed successfully\"}]}";
    ObjectMapper objectMapper = new ObjectMapper();

    // Act
    JobReportResponse response = objectMapper.readValue(json, JobReportResponse.class);

    // Assert
    assertEquals("build-job", response.getJobName());
    assertNotNull(response.getExecutions());
    assertEquals(1, response.getExecutions().size());
    assertEquals("exec-1", response.getExecutions().getFirst().getExecutionId());
    assertEquals("SUCCESS", response.getExecutions().getFirst().getStatus());
  }

  @Test
  void getExecutions_WithEmptyList_ShouldReturnEmptyList() {
    // Arrange
    List<JobReportResponse.ExecutionRecord> emptyList = Collections.emptyList();
    JobReportResponse response = new JobReportResponse(JOB_NAME, emptyList);

    // Act
    List<JobReportResponse.ExecutionRecord> returnedExecutions = response.getExecutions();

    // Assert
    assertNotNull(returnedExecutions, "Returned executions list should not be null");
    assertTrue(returnedExecutions.isEmpty(), "Returned executions list should be empty");
  }
}