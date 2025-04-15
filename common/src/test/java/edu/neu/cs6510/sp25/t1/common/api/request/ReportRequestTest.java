package edu.neu.cs6510.sp25.t1.common.api.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportRequestTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void testConstructorAndGetters() {
    // Arrange
    String pipelineName = "TestPipeline";
    String stageName = "BuildStage";
    String jobName = "CompileJob";
    int runNumber = 5;

    // Act
    ReportRequest reportRequest = new ReportRequest(pipelineName, stageName, jobName, runNumber);

    // Assert
    assertEquals(pipelineName, reportRequest.getPipelineName());
    assertEquals(stageName, reportRequest.getStageName());
    assertEquals(jobName, reportRequest.getJobName());
    assertEquals(runNumber, reportRequest.getRunNumber());
  }

  @Test
  void testJsonSerialization() throws JsonProcessingException {
    // Arrange
    ReportRequest reportRequest = new ReportRequest("Pipeline1", "Stage1", "Job1", 10);

    // Act
    String json = objectMapper.writeValueAsString(reportRequest);

    // Assert
    assertTrue(json.contains("\"pipelineName\":\"Pipeline1\""));
    assertTrue(json.contains("\"stageName\":\"Stage1\""));
    assertTrue(json.contains("\"jobName\":\"Job1\""));
    assertTrue(json.contains("\"runNumber\":10"));
  }

  @Test
  void testJsonDeserialization() throws JsonProcessingException {
    // Arrange
    String json = "{\"pipelineName\":\"TestPipe\",\"stageName\":\"TestStage\",\"jobName\":\"TestJob\",\"runNumber\":3}";

    // Act
    ReportRequest reportRequest = objectMapper.readValue(json, ReportRequest.class);

    // Assert
    assertEquals("TestPipe", reportRequest.getPipelineName());
    assertEquals("TestStage", reportRequest.getStageName());
    assertEquals("TestJob", reportRequest.getJobName());
    assertEquals(3, reportRequest.getRunNumber());
  }

  @Test
  void testJsonDeserializationWithMissingFields() throws JsonProcessingException {
    // Arrange
    String json = "{\"pipelineName\":\"Pipe\",\"stageName\":\"Stage\"}";

    // Act
    ReportRequest reportRequest = objectMapper.readValue(json, ReportRequest.class);

    // Assert
    assertEquals("Pipe", reportRequest.getPipelineName());
    assertEquals("Stage", reportRequest.getStageName());
    assertNull(reportRequest.getJobName());  // Expect null for missing field
    assertEquals(0, reportRequest.getRunNumber()); // Expect default value 0 for int
  }

}
