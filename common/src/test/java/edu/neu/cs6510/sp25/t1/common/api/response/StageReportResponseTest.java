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
 * Unit tests for {@link StageReportResponse}.
 */
class StageReportResponseTest {

  private static final String STAGE_NAME = "build";
  private static final List<StageReportResponse.ExecutionRecord> EXECUTIONS = Arrays.asList(
          new StageReportResponse.ExecutionRecord("exec-1", "SUCCESS"),
          new StageReportResponse.ExecutionRecord("exec-2", "FAILED")
  );

  @Test
  void constructor_WithValidParameters_ShouldCreateInstanceWithCorrectValues() {
    // Act
    StageReportResponse response = new StageReportResponse(STAGE_NAME, EXECUTIONS);

    // Assert
    assertEquals(STAGE_NAME, response.getStageName(), "Stage name should match");
    assertEquals(EXECUTIONS, response.getExecutions(), "Executions list should match");
    assertEquals(2, response.getExecutions().size(), "Executions list should contain 2 items");
  }

  @Test
  void constructor_WithNullParameters_ShouldCreateInstanceWithNullValues() {
    // Act
    StageReportResponse response = new StageReportResponse(null, null);

    // Assert
    assertNull(response.getStageName(), "Stage name should be null");
    assertNull(response.getExecutions(), "Executions list should be null");
  }

  static Stream<Arguments> provideParameterVariations() {
    return Stream.of(
            // stageName, executions
            Arguments.of("", Collections.emptyList()),
            Arguments.of("test-stage", null),
            Arguments.of(null, new ArrayList<>()),
            Arguments.of("deploy-stage", List.of(
                    new StageReportResponse.ExecutionRecord("exec-3", "SUCCESS")
            ))
    );
  }

  @ParameterizedTest
  @MethodSource("provideParameterVariations")
  void constructor_WithVariousParameters_ShouldCreateCorrectInstance(
          String stageName, List<StageReportResponse.ExecutionRecord> executions) {

    // Act
    StageReportResponse response = new StageReportResponse(stageName, executions);

    // Assert
    assertEquals(stageName, response.getStageName(), "Stage name should match");
    assertEquals(executions, response.getExecutions(), "Executions list should match");
  }

  @Test
  void getters_ShouldReturnCorrectValues() {
    // Arrange
    StageReportResponse response = new StageReportResponse(STAGE_NAME, EXECUTIONS);

    // Act & Assert
    assertEquals(STAGE_NAME, response.getStageName());
    assertEquals(EXECUTIONS, response.getExecutions());
    // Testing each getter individually ensures complete method coverage
  }

  @Test
  void jsonSerialization_ShouldSerializeCorrectly() throws Exception {
    // Arrange
    StageReportResponse originalResponse = new StageReportResponse(STAGE_NAME, EXECUTIONS);
    ObjectMapper objectMapper = new ObjectMapper();

    // Act
    String json = objectMapper.writeValueAsString(originalResponse);

    // Assert - verify json contains expected values
    assertTrue(json.contains("\"stageName\":\"" + STAGE_NAME + "\""));
    assertTrue(json.contains("\"executionId\":\"exec-1\""));
    assertTrue(json.contains("\"status\":\"SUCCESS\""));
    assertTrue(json.contains("\"status\":\"FAILED\""));
  }

  @Test
  void stageName_WithSpecialCharacters_ShouldHandleCorrectly() {
    // Arrange
    String specialStageName = "stage-name_with.special@characters";

    // Act
    StageReportResponse response = new StageReportResponse(specialStageName, EXECUTIONS);

    // Assert
    assertEquals(specialStageName, response.getStageName(),
            "Should handle special characters in stage name");
  }

  @Test
  void getExecutions_WithEmptyList_ShouldReturnEmptyList() {
    // Arrange
    List<StageReportResponse.ExecutionRecord> emptyList = Collections.emptyList();
    StageReportResponse response = new StageReportResponse(STAGE_NAME, emptyList);

    // Act
    List<StageReportResponse.ExecutionRecord> returnedExecutions = response.getExecutions();

    // Assert
    assertNotNull(returnedExecutions, "Returned executions list should not be null");
    assertTrue(returnedExecutions.isEmpty(), "Returned executions list should be empty");
  }
}