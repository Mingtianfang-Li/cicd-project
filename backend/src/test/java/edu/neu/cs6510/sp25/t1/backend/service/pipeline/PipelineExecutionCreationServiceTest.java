package edu.neu.cs6510.sp25.t1.backend.service.pipeline;

import edu.neu.cs6510.sp25.t1.backend.database.entity.JobEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.JobExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.PipelineExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.StageEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.StageExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.database.repository.JobExecutionRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.JobRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.PipelineExecutionRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.PipelineRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.StageExecutionRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.StageRepository;
import edu.neu.cs6510.sp25.t1.common.api.request.PipelineExecutionRequest;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // To fix the strict stubbing argument mismatch
public class PipelineExecutionCreationServiceTest {

    @Mock
    private PipelineExecutionRepository pipelineExecutionRepository;

    @Mock
    private StageExecutionRepository stageExecutionRepository;

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @Mock
    private PipelineRepository pipelineRepository;

    @Mock
    private StageRepository stageRepository;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private PipelineExecutionCreationService service;

    @Captor
    private ArgumentCaptor<PipelineExecutionEntity> pipelineExecutionCaptor;

    @Captor
    private ArgumentCaptor<StageExecutionEntity> stageExecutionCaptor;

    @Captor
    private ArgumentCaptor<List<JobExecutionEntity>> jobExecutionsCaptor;

    private PipelineExecutionRequest request;
    private UUID pipelineId;
    private UUID stageId;
    private UUID jobId;
    private Map<String, Object> pipelineConfig;
    private Queue<Queue<UUID>> stageQueue;

    @BeforeEach
    public void setUp() {
        pipelineId = UUID.randomUUID();
        stageId = UUID.randomUUID();
        jobId = UUID.randomUUID();

        // Create request
        UUID requestId = UUID.randomUUID();
        String filePath = "/path/to/pipeline.yaml";
        String repo = "https://github.com/test/repo";
        String branch = "main";
        boolean isLocal = false;
        int runNumber = 1;
        String commitHash = "abc123";

        request = new PipelineExecutionRequest(
                requestId,
                filePath,
                repo,
                branch,
                isLocal,
                runNumber,
                commitHash
        );

        // Create pipeline configuration
        pipelineConfig = new HashMap<>();
        pipelineConfig.put("name", "test-pipeline");

        // Initialize stage queue
        stageQueue = new LinkedList<>();
    }

    @Test
    public void testCreatePipelineExecution() {
        // Act
        PipelineExecutionEntity result = service.createPipelineExecution(request, pipelineId);

        // Assert
        assertNotNull(result);
        assertEquals(pipelineId, result.getPipelineId());
        assertEquals(request.getCommitHash(), result.getCommitHash());
        assertEquals(request.isLocal(), result.isLocal());
        assertEquals(ExecutionStatus.PENDING, result.getStatus());
        assertNotNull(result.getStartTime());
    }

    @Test
    public void testSavePipelineExecution() {
        // Arrange
        PipelineExecutionEntity entity = new PipelineExecutionEntity();
        entity.setId(UUID.randomUUID()); // Set ID to avoid null issues
        entity.setPipelineId(pipelineId);
        entity.setCommitHash(request.getCommitHash());
        entity.setLocal(request.isLocal());
        entity.setStatus(ExecutionStatus.PENDING);
        entity.setStartTime(Instant.now());

        // Configure mock with specific behavior for this exact entity and ID
        when(pipelineExecutionRepository.saveAndFlush(eq(entity))).thenReturn(entity);
        when(pipelineExecutionRepository.findById(eq(entity.getId()))).thenReturn(Optional.of(entity));

        // Act
        PipelineExecutionEntity result = service.savePipelineExecution(entity);

        // Assert
        assertNotNull(result);
        assertEquals(entity, result);
        verify(pipelineExecutionRepository).saveAndFlush(eq(entity));
        verify(pipelineExecutionRepository).findById(eq(entity.getId()));
    }

    @Test
    public void testSavePipelineExecution_SaveFails() {
        // Arrange
        PipelineExecutionEntity entity = new PipelineExecutionEntity();
        entity.setId(UUID.randomUUID()); // Set ID to avoid null issues
        entity.setPipelineId(pipelineId);
        entity.setCommitHash(request.getCommitHash());
        entity.setLocal(request.isLocal());
        entity.setStatus(ExecutionStatus.PENDING);
        entity.setStartTime(Instant.now());

        when(pipelineExecutionRepository.saveAndFlush(any(PipelineExecutionEntity.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.savePipelineExecution(entity);
        });

        assertTrue(exception.getMessage().contains("Failed to save pipeline execution"));
        verify(pipelineExecutionRepository).saveAndFlush(any(PipelineExecutionEntity.class));
    }

    @Test
    public void testCreateAndSaveStageExecutions() {
        // Arrange
        UUID pipelineExecutionId = UUID.randomUUID();

        // Setup pipeline execution entity
        PipelineExecutionEntity pipelineExecution = new PipelineExecutionEntity();
        pipelineExecution.setId(pipelineExecutionId);
        pipelineExecution.setPipelineId(pipelineId);
        pipelineExecution.setCommitHash("abc123");
        pipelineExecution.setLocal(false);

        when(pipelineExecutionRepository.findById(eq(pipelineExecutionId))).thenReturn(Optional.of(pipelineExecution));

        // Setup stage entities
        List<StageEntity> stageEntities = createStageEntities(3);
        when(stageRepository.findByPipelineId(eq(pipelineId))).thenReturn(stageEntities);

        // Setup job entities for each stage
        for (StageEntity stage : stageEntities) {
            List<JobEntity> jobEntities = createJobEntities(2, stage.getId());
            when(jobRepository.findByStageId(eq(stage.getId()))).thenReturn(jobEntities);

            // Make sure existsById returns true for all job IDs
            for (JobEntity job : jobEntities) {
                when(jobRepository.existsById(eq(job.getId()))).thenReturn(true);
            }
        }

        // Setup stage execution responses
        when(stageExecutionRepository.saveAndFlush(any(StageExecutionEntity.class)))
                .thenAnswer(invocation -> {
                    StageExecutionEntity stage = invocation.getArgument(0);
                    if (stage.getId() == null) {
                        stage.setId(UUID.randomUUID());
                    }
                    return stage;
                });

        // Setup job execution responses
        when(jobExecutionRepository.saveAll(anyList()))
                .thenAnswer(invocation -> {
                    List<JobExecutionEntity> jobs = invocation.getArgument(0);
                    return jobs.stream()
                            .peek(job -> {
                                if (job.getId() == null) {
                                    job.setId(UUID.randomUUID());
                                }
                            })
                            .collect(Collectors.toList());
                });

        // Act
        service.createAndSaveStageExecutions(pipelineExecutionId, pipelineConfig, stageQueue);

        // Assert
        verify(pipelineExecutionRepository).findById(eq(pipelineExecutionId));
        verify(stageRepository).findByPipelineId(eq(pipelineId));
        verify(stageExecutionRepository, times(3)).saveAndFlush(any(StageExecutionEntity.class));
        verify(jobRepository, times(3)).flush();
        verify(jobExecutionRepository, times(3)).saveAll(anyList());
        verify(jobExecutionRepository, times(3)).flush();

        // Verify stage queue size
        assertEquals(3, stageQueue.size());
    }

    @Test
    public void testCreateAndSaveStageExecutions_NoStages() {
        // Arrange
        UUID pipelineExecutionId = UUID.randomUUID();

        // Setup pipeline execution entity
        PipelineExecutionEntity pipelineExecution = new PipelineExecutionEntity();
        pipelineExecution.setId(pipelineExecutionId);
        pipelineExecution.setPipelineId(pipelineId);

        when(pipelineExecutionRepository.findById(eq(pipelineExecutionId))).thenReturn(Optional.of(pipelineExecution));
        when(stageRepository.findByPipelineId(eq(pipelineId))).thenReturn(Collections.emptyList());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createAndSaveStageExecutions(pipelineExecutionId, pipelineConfig, stageQueue);
        });

        assertTrue(exception.getMessage().contains("Pipeline stage definitions not found"));
        verify(pipelineExecutionRepository).findById(eq(pipelineExecutionId));
        verify(stageRepository).findByPipelineId(eq(pipelineId));
    }

    @Test
    public void testCreateAndSaveStageExecutions_StageExecutionFails() {
        // Arrange
        UUID pipelineExecutionId = UUID.randomUUID();

        // Setup pipeline execution entity
        PipelineExecutionEntity pipelineExecution = new PipelineExecutionEntity();
        pipelineExecution.setId(pipelineExecutionId);
        pipelineExecution.setPipelineId(pipelineId);
        pipelineExecution.setCommitHash("abc123");
        pipelineExecution.setLocal(false);

        when(pipelineExecutionRepository.findById(eq(pipelineExecutionId))).thenReturn(Optional.of(pipelineExecution));

        // Setup stage entities
        List<StageEntity> stageEntities = createStageEntities(1);
        when(stageRepository.findByPipelineId(eq(pipelineId))).thenReturn(stageEntities);

        // Setup stage execution to fail
        when(stageExecutionRepository.saveAndFlush(any(StageExecutionEntity.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.createAndSaveStageExecutions(pipelineExecutionId, pipelineConfig, stageQueue);
        });

        assertTrue(exception.getMessage().contains("Database error"));
        verify(pipelineExecutionRepository).findById(eq(pipelineExecutionId));
        verify(stageRepository).findByPipelineId(eq(pipelineId));
        verify(stageExecutionRepository).saveAndFlush(any(StageExecutionEntity.class));
    }

    @Test
    public void testVerifyEntitiesSaved_AllExist() {
        // Arrange
        UUID pipelineExecutionId = UUID.randomUUID();

        when(pipelineRepository.existsById(eq(pipelineId))).thenReturn(true);
        when(pipelineExecutionRepository.existsById(eq(pipelineExecutionId))).thenReturn(true);

        List<StageEntity> stages = createStageEntities(2);
        when(stageRepository.findByPipelineId(eq(pipelineId))).thenReturn(stages);

        List<JobEntity> jobs1 = createJobEntities(2, stages.get(0).getId());
        List<JobEntity> jobs2 = createJobEntities(1, stages.get(1).getId());
        when(jobRepository.findByStageId(eq(stages.get(0).getId()))).thenReturn(jobs1);
        when(jobRepository.findByStageId(eq(stages.get(1).getId()))).thenReturn(jobs2);

        List<StageExecutionEntity> stageExecutions = createStageExecutionEntities(2, pipelineExecutionId);
        when(stageExecutionRepository.findByPipelineExecutionId(eq(pipelineExecutionId))).thenReturn(stageExecutions);

        List<JobExecutionEntity> jobExecutions1 = createJobExecutionEntities(2, stageExecutions.get(0));
        List<JobExecutionEntity> jobExecutions2 = createJobExecutionEntities(1, stageExecutions.get(1));
        when(jobExecutionRepository.findByStageExecution(eq(stageExecutions.get(0)))).thenReturn(jobExecutions1);
        when(jobExecutionRepository.findByStageExecution(eq(stageExecutions.get(1)))).thenReturn(jobExecutions2);

        // Act - Should not throw any exceptions
        service.verifyEntitiesSaved(pipelineId, pipelineExecutionId);

        // Assert
        verify(pipelineRepository).existsById(eq(pipelineId));
        verify(pipelineExecutionRepository).existsById(eq(pipelineExecutionId));
        verify(stageRepository).findByPipelineId(eq(pipelineId));
        verify(jobRepository, times(2)).findByStageId(any(UUID.class));
        verify(stageExecutionRepository).findByPipelineExecutionId(eq(pipelineExecutionId));
        verify(jobExecutionRepository, times(2)).findByStageExecution(any(StageExecutionEntity.class));
    }

    @Test
    public void testVerifyEntitiesSaved_PipelineDoesNotExist() {
        // Arrange
        UUID pipelineExecutionId = UUID.randomUUID();

        when(pipelineRepository.existsById(eq(pipelineId))).thenReturn(false);
        when(pipelineExecutionRepository.existsById(eq(pipelineExecutionId))).thenReturn(true);
        when(stageRepository.findByPipelineId(eq(pipelineId))).thenReturn(Collections.emptyList());
        when(stageExecutionRepository.findByPipelineExecutionId(eq(pipelineExecutionId))).thenReturn(Collections.emptyList());

        // Act - Should log an error but not throw an exception
        service.verifyEntitiesSaved(pipelineId, pipelineExecutionId);

        // Assert
        verify(pipelineRepository).existsById(eq(pipelineId));
        verify(pipelineExecutionRepository).existsById(eq(pipelineExecutionId));
        verify(stageRepository).findByPipelineId(eq(pipelineId));
        verify(stageExecutionRepository).findByPipelineExecutionId(eq(pipelineExecutionId));
    }

    // Helper methods to create test entities

    private List<StageEntity> createStageEntities(int count) {
        List<StageEntity> stages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            StageEntity stage = new StageEntity();
            stage.setId(UUID.randomUUID());
            stage.setPipelineId(pipelineId);
            stage.setName("Stage " + i);
            stage.setExecutionOrder(i);
            stages.add(stage);
        }
        return stages;
    }

    private List<JobEntity> createJobEntities(int count, UUID stageId) {
        List<JobEntity> jobs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            JobEntity job = new JobEntity();
            job.setId(UUID.randomUUID());
            job.setStageId(stageId);
            job.setName("Job " + i);
            job.setDockerImage("image:latest");
            job.setAllowFailure(i % 2 == 0); // alternate true/false
            jobs.add(job);
        }
        return jobs;
    }

    private List<StageExecutionEntity> createStageExecutionEntities(int count, UUID pipelineExecutionId) {
        List<StageExecutionEntity> stageExecutions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            StageExecutionEntity stageExecution = new StageExecutionEntity();
            stageExecution.setId(UUID.randomUUID());
            stageExecution.setPipelineExecutionId(pipelineExecutionId);
            stageExecution.setStageId(UUID.randomUUID());
            stageExecution.setExecutionOrder(i);
            stageExecution.setStatus(ExecutionStatus.PENDING);
            stageExecution.setStartTime(Instant.now());
            stageExecutions.add(stageExecution);
        }
        return stageExecutions;
    }

    private List<JobExecutionEntity> createJobExecutionEntities(int count, StageExecutionEntity stageExecution) {
        List<JobExecutionEntity> jobExecutions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            JobExecutionEntity jobExecution = new JobExecutionEntity();
            jobExecution.setId(UUID.randomUUID());
            jobExecution.setStageExecution(stageExecution);
            jobExecution.setJobId(UUID.randomUUID());
            jobExecution.setStatus(ExecutionStatus.PENDING);
            jobExecution.setStartTime(Instant.now());
            jobExecutions.add(jobExecution);
        }
        return jobExecutions;
    }
}