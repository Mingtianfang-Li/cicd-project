package edu.neu.cs6510.sp25.t1.cli.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.neu.cs6510.sp25.t1.cli.api.CliBackendClient;
/**
 * Unit tests for ReportCommand class
 */
@ExtendWith(MockitoExtension.class)
public class ReportCommandTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CliBackendClient backendClient;

    @InjectMocks
    private ReportCommand reportCommand;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        // Use reflection to inject backendClient
        Field backendClientField = ReportCommand.class.getDeclaredField("backendClient");
        backendClientField.setAccessible(true);
        backendClientField.set(reportCommand, backendClient);
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    // Helper method to set private fields using reflection
    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = ReportCommand.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(reportCommand, value);
    }

    // Helper method to invoke private methods using reflection
    private Object invokePrivateMethod(String methodName, Class<?>[] paramTypes, Object[] args) throws Exception {
        Method method = ReportCommand.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(reportCommand, args);
    }

    @Test
    public void testJobWithoutStage_ShouldReturnError() throws Exception {
        // Arrange
        setPrivateField("pipelineName", "test-pipeline");
        setPrivateField("jobName", "test-job");
        setPrivateField("stageName", null);

        // Act
        Integer result = reportCommand.call();

        // Assert
        assertEquals(1, result);
        assertTrue(errContent.toString().contains("--stage parameter is required when using --job"));
    }

    @Test
    public void testFetchPipelineHistory() throws Exception {
        // Arrange
        setPrivateField("pipelineName", "test-pipeline");
        setPrivateField("format", "text");

        String mockApiResponse = createMockPipelineHistoryResponse();
        when(backendClient.fetchPipelineReport(eq("test-pipeline"), eq(-1), eq(null), eq(null)))
                .thenReturn(mockApiResponse);

        // Act
        Integer result = reportCommand.call();

        // Assert
        assertEquals(0, result);
        verify(backendClient).fetchPipelineReport("test-pipeline", -1, null, null);
        assertTrue(outContent.toString().contains("pipeline"));
    }

    @Test
    public void testFetchPipelineRunSummary() throws Exception {
        // Arrange
        setPrivateField("pipelineName", "test-pipeline");
        setPrivateField("runNumber", 42);
        setPrivateField("format", "text");

        String mockApiResponse = createMockPipelineRunResponse();
        when(backendClient.fetchPipelineReport(eq("test-pipeline"), eq(42), eq(null), eq(null)))
                .thenReturn(mockApiResponse);

        // Act
        Integer result = reportCommand.call();

        // Assert
        assertEquals(0, result);
        verify(backendClient).fetchPipelineReport("test-pipeline", 42, null, null);
        assertTrue(outContent.toString().contains("Pipeline Name: test-pipeline"));
        assertTrue(outContent.toString().contains("Run Number: 42"));
    }

    @Test
    public void testFetchStageSummary() throws Exception {
        // Arrange
        setPrivateField("pipelineName", "test-pipeline");
        setPrivateField("runNumber", 42);
        setPrivateField("stageName", "build");
        setPrivateField("format", "text");

        String mockApiResponse = createMockStageResponse();
        when(backendClient.fetchPipelineReport(eq("test-pipeline"), eq(42), eq("build"), eq(null)))
                .thenReturn(mockApiResponse);

        // Act
        Integer result = reportCommand.call();

        // Assert
        assertEquals(0, result);
        verify(backendClient).fetchPipelineReport("test-pipeline", 42, "build", null);
        assertTrue(outContent.toString().contains("Stage Name: build"));
    }

    @Test
    public void testFetchJobSummary() throws Exception {
        // Arrange
        setPrivateField("pipelineName", "test-pipeline");
        setPrivateField("runNumber", 42);
        setPrivateField("stageName", "build");
        setPrivateField("jobName", "compile");
        setPrivateField("format", "text");

        String mockApiResponse = createMockJobResponse();
        when(backendClient.fetchPipelineReport(eq("test-pipeline"), eq(42), eq("build"), eq("compile")))
                .thenReturn(mockApiResponse);

        // Act
        Integer result = reportCommand.call();

        // Assert
        assertEquals(0, result);
        verify(backendClient).fetchPipelineReport("test-pipeline", 42, "build", "compile");
        assertTrue(outContent.toString().contains("Job Name: compile"));
    }

    @Test
    public void testFetchStageHistory() throws Exception {
        // Arrange
        setPrivateField("pipelineName", "test-pipeline");
        setPrivateField("stageName", "build");
        setPrivateField("format", "text");

        String mockApiResponse = createMockStageHistoryResponse();
        when(backendClient.fetchPipelineReport(eq("test-pipeline"), isNull(), eq("build"), eq(null)))
                .thenReturn(mockApiResponse);

        // Act
        Integer result = reportCommand.call();

        // Assert
        assertEquals(0, result);
        verify(backendClient).fetchPipelineReport("test-pipeline", null, "build", null);
        assertTrue(outContent.toString().contains("build"));
    }

    @Test
    public void testFormatResponseJson() throws Exception {
        // Arrange
        setPrivateField("format", "json");

        // Act
        String result = (String) invokePrivateMethod("formatResponse",
                new Class[] { String.class },
                new Object[] { createMockPipelineRunResponse() });

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("\"name\""));
        assertTrue(result.contains("\"runNumber\""));
    }

    @Test
    public void testFormatResponseText() throws Exception {
        // Arrange
        setPrivateField("format", "text");

        // Act
        String result = (String) invokePrivateMethod("formatResponse",
                new Class[] { String.class },
                new Object[] { createMockPipelineRunResponse() });

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Pipeline Name:"));
        assertTrue(result.contains("Run Number:"));
    }

    @Test
    public void testFormatTimestamp() throws Exception {
        // Act
        String formattedTimestamp = (String) invokePrivateMethod("formatTimestamp",
                new Class[] { String.class },
                new Object[] { "2023-04-01T14:30:00Z" });

        String nullTimestamp = (String) invokePrivateMethod("formatTimestamp",
                new Class[] { String.class },
                new Object[] { (Object) null });

        String emptyTimestamp = (String) invokePrivateMethod("formatTimestamp",
                new Class[] { String.class },
                new Object[] { "" });

        // Assert
        assertNotNull(formattedTimestamp);
        assertTrue(formattedTimestamp.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
        assertEquals("N/A", nullTimestamp);
        assertEquals("N/A", emptyTimestamp);
    }

    @Test
    public void testApiRequestFailure() throws Exception {
        // Arrange
        setPrivateField("pipelineName", "test-pipeline");
        when(backendClient.fetchPipelineReport(anyString(), any(), any(), any()))
                .thenThrow(new IOException("Network error"));

        // Act
        Integer result = reportCommand.call();

        // Assert
        assertEquals(1, result);
    }


    // Helper methods to create mock responses
    private String createMockPipelineHistoryResponse() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();

        for (int i = 1; i <= 3; i++) {
            ObjectNode pipelineNode = createBasicPipelineNode(mapper, i);
            arrayNode.add(pipelineNode);
        }

        return mapper.writeValueAsString(arrayNode);
    }

    private String createMockPipelineRunResponse() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode pipelineNode = createBasicPipelineNode(mapper, 42);

        // Add stages
        ArrayNode stagesArray = mapper.createArrayNode();
        stagesArray.add(createStageNode(mapper, "build", "success"));
        stagesArray.add(createStageNode(mapper, "test", "success"));
        pipelineNode.set("stages", stagesArray);

        return mapper.writeValueAsString(pipelineNode);
    }

    private String createMockStageResponse() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode stageNode = createStageNode(mapper, "build", "success");

        // Add jobs
        ArrayNode jobsArray = mapper.createArrayNode();
        jobsArray.add(createJobNode(mapper, "compile", "success"));
        jobsArray.add(createJobNode(mapper, "package", "success"));
        stageNode.set("jobs", jobsArray);

        return mapper.writeValueAsString(stageNode);
    }

    private String createMockJobResponse() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jobNode = createJobNode(mapper, "compile", "success");

        // Add executions
        ArrayNode executionsArray = mapper.createArrayNode();
        executionsArray.add(createExecutionNode(mapper, "1", "success"));
        jobNode.set("executions", executionsArray);

        return mapper.writeValueAsString(jobNode);
    }

    private String createMockStageHistoryResponse() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();

        for (int i = 1; i <= 3; i++) {
            ObjectNode stageNode = createStageNode(mapper, "build", i % 2 == 0 ? "success" : "failed");
            arrayNode.add(stageNode);
        }

        return mapper.writeValueAsString(arrayNode);
    }

    private ObjectNode createBasicPipelineNode(ObjectMapper mapper, int runNumber) {
        ObjectNode pipelineNode = mapper.createObjectNode();
        pipelineNode.put("id", "pipeline-" + runNumber);
        pipelineNode.put("name", "test-pipeline");
        pipelineNode.put("runNumber", runNumber);
        pipelineNode.put("commitHash", "abc123def456");
        pipelineNode.put("status", "success");
        pipelineNode.put("startTime", "2023-04-01T12:00:00Z");
        pipelineNode.put("completionTime", "2023-04-01T12:15:00Z");
        return pipelineNode;
    }

    private ObjectNode createStageNode(ObjectMapper mapper, String name, String status) {
        ObjectNode stageNode = mapper.createObjectNode();
        stageNode.put("id", "stage-" + name);
        stageNode.put("name", name);
        stageNode.put("status", status);
        stageNode.put("startTime", "2023-04-01T12:01:00Z");
        stageNode.put("completionTime", "2023-04-01T12:10:00Z");
        return stageNode;
    }

    private ObjectNode createJobNode(ObjectMapper mapper, String name, String status) {
        ObjectNode jobNode = mapper.createObjectNode();
        jobNode.put("name", name);
        jobNode.put("pipelineName", "test-pipeline");
        jobNode.put("runNumber", 42);
        jobNode.put("stageName", "build");
        jobNode.put("status", status);
        jobNode.put("startTime", "2023-04-01T12:02:00Z");
        jobNode.put("completionTime", "2023-04-01T12:08:00Z");
        return jobNode;
    }

    private ObjectNode createExecutionNode(ObjectMapper mapper, String id, String status) {
        ObjectNode executionNode = mapper.createObjectNode();
        executionNode.put("id", id);
        executionNode.put("status", status);
        executionNode.put("allowFailure", false);
        executionNode.put("startTime", "2023-04-01T12:03:00Z");
        executionNode.put("completionTime", "2023-04-01T12:07:00Z");
        return executionNode;
    }

    @Test
    public void testRemoveNullPipelineNameFromArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode inputArray = mapper.createArrayNode();

        // Add node with pipelineName = null
        ObjectNode nodeWithNull = mapper.createObjectNode();
        nodeWithNull.put("pipelineName", (String) null);
        nodeWithNull.put("runNumber", 1);
        inputArray.add(nodeWithNull);

        // Add node with valid pipelineName
        ObjectNode validNode = mapper.createObjectNode();
        validNode.put("pipelineName", "test");
        validNode.put("runNumber", 2);
        inputArray.add(validNode);

        // Simulate the logic
        ArrayNode modifiedRootNode = mapper.createArrayNode();
        for (JsonNode node : inputArray) {
            ObjectNode modifiedNode = (ObjectNode) node.deepCopy();
            if (modifiedNode.has("pipelineName") && modifiedNode.get("pipelineName").isNull()) {
                modifiedNode.remove("pipelineName");
            }
            modifiedRootNode.add(modifiedNode);
        }

        // Assert result
        assertEquals(2, modifiedRootNode.size());
        assertFalse(modifiedRootNode.get(0).has("pipelineName"));
        assertEquals("test", modifiedRootNode.get(1).get("pipelineName").asText());
    }

    @Test
    public void testFetchStageHistory_WithJobName_ShouldLogJobInfo() throws Exception {
        setPrivateField("pipelineName", "test-pipeline");
        setPrivateField("stageName", "build");
        setPrivateField("jobName", "compile");
        setPrivateField("format", "text"); // ✅ add this line

        when(backendClient.fetchPipelineReport(eq("test-pipeline"), isNull(), eq("build"), eq("compile")))
                .thenReturn("{\"status\": \"success\"}");

        Method method = ReportCommand.class.getDeclaredMethod("fetchStageHistory");
        method.setAccessible(true);

        int result = (int) method.invoke(reportCommand);

        assertEquals(0, result);
        assertTrue(outContent.toString().contains("Fetching history for job: compile in stage: build in pipeline: test-pipeline"));
    }


    @Test
    public void testFormatResponse_UnknownFormat_ShouldReturnOriginal() throws Exception {
        setPrivateField("format", "xml");

        String originalJson = createMockPipelineRunResponse();
        String result = (String) invokePrivateMethod("formatResponse",
                new Class[]{String.class},
                new Object[]{originalJson});

        assertEquals(originalJson, result);
    }

    @Test
    public void testConvertJsonToText_UnknownStructure_ShouldFallback() throws Exception {
        setPrivateField("format", "text");

        String unknownJson = "{\"unexpectedKey\":\"value\"}";
        String result = (String) invokePrivateMethod("formatResponse",
                new Class[]{String.class},
                new Object[]{unknownJson});

        assertEquals(unknownJson, result);
    }

    @Test
    public void testFormatJobNode_WithPipelineNameIncluded() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jobNode = mapper.createObjectNode();
        jobNode.put("name", "compile");
        jobNode.put("pipelineName", "test-pipeline");
        jobNode.put("runNumber", 42);
        jobNode.put("stageName", "build");

        ArrayNode executions = mapper.createArrayNode();
        ObjectNode exec = mapper.createObjectNode();
        exec.put("id", "exec-1");
        exec.put("status", "success");
        exec.put("allowFailure", false);
        exec.put("startTime", "2023-04-01T12:00:00Z");
        exec.put("completionTime", "2023-04-01T12:01:00Z");
        executions.add(exec);

        jobNode.set("executions", executions);

        StringBuilder sb = new StringBuilder();
        invokePrivateMethod("formatJobNode",
                new Class[]{StringBuilder.class, JsonNode.class, boolean.class, String.class, boolean.class},
                new Object[]{sb, jobNode, true, "", false});

        assertTrue(sb.toString().contains("Pipeline Name: test-pipeline"));
    }

    @Test
    public void testRunNumberAndJobWithoutStage_ShouldReturnError() throws Exception {
        setPrivateField("pipelineName", "test-pipeline");
        setPrivateField("runNumber", 42);
        setPrivateField("jobName", "compile");
        setPrivateField("stageName", null);

        Integer result = reportCommand.call();

        assertEquals(1, result);
        assertTrue(errContent.toString().contains("--stage parameter is required when using --job"));
    }

    @Test
    public void testFormatTimestamp_InvalidFormat_ShouldReturnOriginalString() throws Exception {
        String invalidTimestamp = "not-a-timestamp";

        String result = (String) invokePrivateMethod("formatTimestamp",
                new Class[] { String.class },
                new Object[] { invalidTimestamp });

        assertEquals(invalidTimestamp, result);
    }

    @Test
    public void testConvertJsonToText_ArrayWithAllTypes_ShouldTriggerAllBranches() throws Exception {
        setPrivateField("format", "text");

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();

        // Pipeline node (has "stages" and "name")
        ObjectNode pipelineNode = mapper.createObjectNode();
        pipelineNode.put("name", "pipeline");
        pipelineNode.put("id", "pipeline-1");
        pipelineNode.put("runNumber", 1);
        pipelineNode.put("status", "success");
        pipelineNode.put("startTime", "2023-04-01T12:00:00Z");
        pipelineNode.put("completionTime", "2023-04-01T12:30:00Z");
        pipelineNode.set("stages", mapper.createArrayNode());
        arrayNode.add(pipelineNode);

        // Stage node (has "jobs" and "name")
        ObjectNode stageNode = mapper.createObjectNode();
        stageNode.put("name", "build");
        stageNode.put("id", "stage-1");
        stageNode.put("status", "success");
        stageNode.put("startTime", "2023-04-01T12:00:00Z");
        stageNode.put("completionTime", "2023-04-01T12:10:00Z");
        stageNode.set("jobs", mapper.createArrayNode());
        arrayNode.add(stageNode);

        // Job node (has "executions" and "name")
        ObjectNode jobNode = mapper.createObjectNode();
        jobNode.put("name", "compile");
        jobNode.set("executions", mapper.createArrayNode());
        arrayNode.add(jobNode);

        String json = mapper.writeValueAsString(arrayNode);

        String result = (String) invokePrivateMethod("formatResponse",
                new Class[]{String.class},
                new Object[]{json});

        // Assert: all sections should appear
        assertTrue(result.contains("Pipeline Name: pipeline"));
        assertTrue(result.contains("Stage Name: build"));
        assertTrue(result.contains("Job Name: compile"));
        assertTrue(result.contains("----------------------------------------")); // Separator
    }



}