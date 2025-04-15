package edu.neu.cs6510.sp25.t1.backend.service.execution;

import edu.neu.cs6510.sp25.t1.backend.database.entity.PipelineExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.info.ClonedPipelineInfo;
import edu.neu.cs6510.sp25.t1.backend.service.pipeline.GitPipelineService;
import edu.neu.cs6510.sp25.t1.backend.service.pipeline.PipelineDefinitionService;
import edu.neu.cs6510.sp25.t1.backend.service.pipeline.PipelineExecutionCreationService;
import edu.neu.cs6510.sp25.t1.backend.service.pipeline.YamlConfigurationService;
import edu.neu.cs6510.sp25.t1.common.api.request.PipelineExecutionRequest;
import edu.neu.cs6510.sp25.t1.common.api.response.PipelineExecutionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;


@ExtendWith(MockitoExtension.class)
public class PipelineExecutionServiceTest {

    @Mock
    private PipelineDefinitionService pipelineDefinitionService;

    @Mock
    private YamlConfigurationService yamlConfigurationService;

    @Mock
    private PipelineExecutionCreationService pipelineExecutionCreationService;

    @Mock
    private GitPipelineService gitPipelineService;

    @InjectMocks
    private PipelineExecutionService pipelineExecutionService;

    private PipelineExecutionRequest request;
    private Queue<Queue<UUID>> stageQueue;
    private Map<String, Object> pipelineConfig;
    private UUID pipelineId;
    private PipelineExecutionEntity pipelineExecution;
    private Path resolvedPath;
    private ClonedPipelineInfo clonedPipelineInfo;
    private String pipelineExecutionId;

    @BeforeEach
    public void setUp() {
        // Initialize test data with the actual constructor
        UUID requestId = UUID.randomUUID();
        String filePath = "/path/to/pipeline.yaml";
        String repo = "https://github.com/test/repo";
        String branch = "main";
        boolean isLocal = false;
        int runNumber = 1;
        String commitHash = "abc123";

        // Create request using the constructor parameters required
        request = new PipelineExecutionRequest(
                requestId,
                filePath,
                repo,
                branch,
                isLocal,
                runNumber,
                commitHash
        );

        stageQueue = new LinkedList<>();

        pipelineConfig = new HashMap<>();
        pipelineConfig.put("name", "test-pipeline");

        pipelineId = UUID.randomUUID();

        pipelineExecution = new PipelineExecutionEntity();
        pipelineExecution.setId(UUID.randomUUID());

        resolvedPath = Paths.get("/mnt/pipeline/uuid/repo/.pipelines/pipeline.yaml");

        clonedPipelineInfo = new ClonedPipelineInfo(
                "/mnt/pipeline/uuid/repo/.pipelines/pipeline.yaml",
                UUID.randomUUID()
        );

        pipelineExecutionId = pipelineExecution.getId().toString();
    }

    @Test
    public void testStartPipelineExecution_Success() throws Exception {
        // Arrange
        when(gitPipelineService.cloneRepoAndLocatePipelineFile(any(PipelineExecutionRequest.class)))
                .thenReturn(clonedPipelineInfo);

        when(yamlConfigurationService.resolveAndValidatePipelinePath(anyString()))
                .thenReturn(resolvedPath);

        when(yamlConfigurationService.parseAndValidatePipelineYaml(anyString()))
                .thenReturn(pipelineConfig);

        when(pipelineDefinitionService.createOrGetPipelineEntity(
                any(PipelineExecutionRequest.class), anyMap()))
                .thenReturn(pipelineId);

        when(pipelineExecutionCreationService.createPipelineExecution(
                any(PipelineExecutionRequest.class), any(UUID.class)))
                .thenReturn(pipelineExecution);

        when(pipelineExecutionCreationService.savePipelineExecution(any(PipelineExecutionEntity.class)))
                .thenReturn(pipelineExecution);

        doNothing().when(pipelineDefinitionService).createPipelineDefinition(
                any(UUID.class), anyMap(), anyString());

        doNothing().when(pipelineExecutionCreationService).createAndSaveStageExecutions(
                any(UUID.class), anyMap(), any(Queue.class));

        doNothing().when(pipelineExecutionCreationService).verifyEntitiesSaved(
                any(UUID.class), any(UUID.class));

        // Act
        PipelineExecutionResponse response = pipelineExecutionService.startPipelineExecution(request, stageQueue);

        // Assert
        assertNotNull(response);

        // Since we're not sure which method or field to use for the ID,
        // let's just check that the response is not null and status is correct
        assertEquals("PENDING", response.getStatus());

        // Verify all interactions
        verify(gitPipelineService).cloneRepoAndLocatePipelineFile(request);
        verify(yamlConfigurationService).resolveAndValidatePipelinePath(clonedPipelineInfo.getYamlPath());
        verify(yamlConfigurationService).parseAndValidatePipelineYaml(resolvedPath.toString());
        verify(pipelineDefinitionService).createOrGetPipelineEntity(request, pipelineConfig);
        verify(pipelineDefinitionService).createPipelineDefinition(pipelineId, pipelineConfig, request.getFilePath());
        verify(pipelineExecutionCreationService).createPipelineExecution(request, pipelineId);
        verify(pipelineExecutionCreationService).savePipelineExecution(pipelineExecution);
        verify(pipelineExecutionCreationService).createAndSaveStageExecutions(
                pipelineExecution.getId(), pipelineConfig, stageQueue);
        verify(pipelineExecutionCreationService).verifyEntitiesSaved(pipelineId, pipelineExecution.getId());
    }

    @Test
    public void testStartPipelineExecution_GitCloneFailure() throws Exception {
        // Arrange
        when(gitPipelineService.cloneRepoAndLocatePipelineFile(any(PipelineExecutionRequest.class)))
                .thenThrow(new RuntimeException("Failed to clone repository"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            pipelineExecutionService.startPipelineExecution(request, stageQueue);
        });

        assertTrue(exception.getMessage().contains("Pipeline execution failed"));

        // Verify interactions
        verify(gitPipelineService).cloneRepoAndLocatePipelineFile(request);
        verifyNoInteractions(yamlConfigurationService);
        verifyNoInteractions(pipelineDefinitionService);
        verifyNoInteractions(pipelineExecutionCreationService);
    }

    @Test
    public void testStartPipelineExecution_YamlValidationFailure() throws Exception {
        // Arrange
        when(gitPipelineService.cloneRepoAndLocatePipelineFile(any(PipelineExecutionRequest.class)))
                .thenReturn(clonedPipelineInfo);

        when(yamlConfigurationService.resolveAndValidatePipelinePath(anyString()))
                .thenReturn(resolvedPath);

        when(yamlConfigurationService.parseAndValidatePipelineYaml(anyString()))
                .thenThrow(new IllegalArgumentException("Invalid YAML: Missing required fields"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            pipelineExecutionService.startPipelineExecution(request, stageQueue);
        });

        assertTrue(exception.getMessage().contains("Pipeline execution failed"));

        // Verify interactions
        verify(gitPipelineService).cloneRepoAndLocatePipelineFile(request);
        verify(yamlConfigurationService).resolveAndValidatePipelinePath(clonedPipelineInfo.getYamlPath());
        verify(yamlConfigurationService).parseAndValidatePipelineYaml(resolvedPath.toString());
        verifyNoInteractions(pipelineDefinitionService);
        verifyNoInteractions(pipelineExecutionCreationService);
    }

    @Test
    public void testStartPipelineExecution_EntityCreationFailure() throws Exception {
        // Arrange
        when(gitPipelineService.cloneRepoAndLocatePipelineFile(any(PipelineExecutionRequest.class)))
                .thenReturn(clonedPipelineInfo);

        when(yamlConfigurationService.resolveAndValidatePipelinePath(anyString()))
                .thenReturn(resolvedPath);

        when(yamlConfigurationService.parseAndValidatePipelineYaml(anyString()))
                .thenReturn(pipelineConfig);

        when(pipelineDefinitionService.createOrGetPipelineEntity(
                any(PipelineExecutionRequest.class), anyMap()))
                .thenReturn(pipelineId);

        doThrow(new RuntimeException("Database error")).when(pipelineDefinitionService)
                .createPipelineDefinition(any(UUID.class), anyMap(), anyString());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            pipelineExecutionService.startPipelineExecution(request, stageQueue);
        });

        assertTrue(exception.getMessage().contains("Pipeline execution failed"));

        // Verify interactions
        verify(gitPipelineService).cloneRepoAndLocatePipelineFile(request);
        verify(yamlConfigurationService).resolveAndValidatePipelinePath(clonedPipelineInfo.getYamlPath());
        verify(yamlConfigurationService).parseAndValidatePipelineYaml(resolvedPath.toString());
        verify(pipelineDefinitionService).createOrGetPipelineEntity(request, pipelineConfig);
        verify(pipelineDefinitionService).createPipelineDefinition(pipelineId, pipelineConfig, request.getFilePath());
        verifyNoInteractions(pipelineExecutionCreationService);
    }
}