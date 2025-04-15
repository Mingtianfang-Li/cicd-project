package edu.neu.cs6510.sp25.t1.backend.api.controller;

import edu.neu.cs6510.sp25.t1.backend.error.ApiError;
import edu.neu.cs6510.sp25.t1.backend.messaging.StageQueuePublisher;
import edu.neu.cs6510.sp25.t1.backend.service.execution.PipelineExecutionService;
import edu.neu.cs6510.sp25.t1.backend.service.status.StatusService;
import edu.neu.cs6510.sp25.t1.common.api.request.PipelineExecutionRequest;
import edu.neu.cs6510.sp25.t1.common.api.response.PipelineExecutionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PipelineControllerTest {

    @Mock
    private PipelineExecutionService pipelineExecutionService;

    @Mock
    private StageQueuePublisher stageQueuePublisher;

    @Mock
    private StatusService statusService;

    @InjectMocks
    private PipelineController pipelineController;

    private PipelineExecutionRequest validRequest;
    private PipelineExecutionResponse successResponse;
    private Map<String, Object> statusResponseMap;

    @BeforeEach
    public void setUp() {
        // Set up a valid pipeline execution request
        UUID pipelineId = UUID.randomUUID();
        String repo = "https://github.com/example/repo";
        String branch = "main";
        String commitHash = "abc1234";
        boolean isLocal = false;
        int runNumber = 1;
        String filePath = ".pipelines/pipeline.yaml";

        validRequest = new PipelineExecutionRequest(
                pipelineId,
                filePath,
                repo,
                branch,
                isLocal,
                runNumber,
                commitHash
        );

        // Set up a successful execution response
        successResponse = new PipelineExecutionResponse(UUID.randomUUID().toString(), "PENDING");

        // Set up a status response map
        statusResponseMap = new HashMap<>();
        statusResponseMap.put("pipeline", "example-pipeline");
        statusResponseMap.put("pipelineStatus", "SUCCESS");
    }

    @Test
    public void testRunPipeline_Success() {
        // Arrange
        when(pipelineExecutionService.startPipelineExecution(any(PipelineExecutionRequest.class), any()))
                .thenReturn(successResponse);
        doNothing().when(stageQueuePublisher).dispatchStageQueue(any());

        // Act
        ResponseEntity<?> response = pipelineController.runPipeline(validRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(successResponse, response.getBody());

        // Verify the service and publisher were called correctly
        verify(pipelineExecutionService, times(1)).startPipelineExecution(eq(validRequest), any());
        verify(stageQueuePublisher, times(1)).dispatchStageQueue(any());
    }

    @Test
    public void testRunPipeline_ServiceThrowsException() {
        // Arrange
        when(pipelineExecutionService.startPipelineExecution(any(PipelineExecutionRequest.class), any()))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<?> response = pipelineController.runPipeline(validRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ApiError);
        ApiError error = (ApiError) response.getBody();
        assertEquals("Pipeline Execution Failed", error.getMessage());
        assertEquals("Service error", error.getDetail());
    }

    @Test
    public void testGetPipelineStatusByFile_Success() {
        // Arrange
        String pipelineFile = "example-pipeline";
        when(statusService.getStatusForPipeline(pipelineFile)).thenReturn(statusResponseMap);

        // Act
        ResponseEntity<?> response = pipelineController.getPipelineStatusByFile(pipelineFile);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(statusResponseMap, response.getBody());

        // Verify the status service was called with the correct parameter
        verify(statusService, times(1)).getStatusForPipeline(pipelineFile);
    }

    @Test
    public void testGetPipelineStatusByFile_WithYamlExtension() {
        // Arrange
        String pipelineFile = "example-pipeline.yaml";
        String pipelineName = "example-pipeline";

        when(statusService.getStatusForPipeline(anyString())).thenReturn(statusResponseMap);

        // Act
        ResponseEntity<?> response = pipelineController.getPipelineStatusByFile(pipelineFile);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(statusResponseMap, response.getBody());

        // Verify the status service was called with the correct parameter (without .yaml extension)
        ArgumentCaptor<String> pipelineNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(statusService, times(1)).getStatusForPipeline(pipelineNameCaptor.capture());
        assertEquals(pipelineFile, pipelineNameCaptor.getValue());
    }

    @Test
    public void testGetPipelineStatusByFile_ServiceThrowsException() {
        // Arrange
        String pipelineFile = "example-pipeline";
        when(statusService.getStatusForPipeline(pipelineFile))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<?> response = pipelineController.getPipelineStatusByFile(pipelineFile);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ApiError);
        ApiError error = (ApiError) response.getBody();
        assertEquals("Pipeline status fetch failed", error.getMessage());
        assertEquals("Service error", error.getDetail());
    }

    @Test
    public void testGetPipelineStatusByFile_NullPipelineFile() {
        // Arrange
        String pipelineFile = null;

        // Act
        ResponseEntity<?> response = pipelineController.getPipelineStatusByFile(pipelineFile);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid pipeline ", response.getBody());

        // Verify the status service was never called
        verify(statusService, never()).getStatusForPipeline(any());
    }
}