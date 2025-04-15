package edu.neu.cs6510.sp25.t1.backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import edu.neu.cs6510.sp25.t1.backend.database.entity.JobEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.JobExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.StageExecutionEntity;
import edu.neu.cs6510.sp25.t1.common.dto.JobDTO;
import edu.neu.cs6510.sp25.t1.common.dto.JobExecutionDTO;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;

public class JobExecutionMapperTest {

    private JobExecutionMapper mapper;
    private JobExecutionEntity jobExecutionEntity;
    private JobEntity jobEntity;
    private StageExecutionEntity stageExecutionEntity;

    @BeforeEach
    public void setUp() {
        mapper = new JobExecutionMapper();

        // Create a mock StageExecutionEntity
        UUID stageExecutionId = UUID.randomUUID();
        stageExecutionEntity = Mockito.mock(StageExecutionEntity.class);
        Mockito.when(stageExecutionEntity.getId()).thenReturn(stageExecutionId);

        // Create job entity
        UUID jobId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        jobEntity = new JobEntity();
        jobEntity.setId(jobId);
        jobEntity.setStageId(stageId);
        jobEntity.setName("build-job");
        jobEntity.setDockerImage("docker.io/maven:3.8.4");
        jobEntity.setScript(Arrays.asList("mvn clean", "mvn compile"));
        jobEntity.setWorkingDir("/workspace");
        jobEntity.setDependencies(Arrays.asList(UUID.randomUUID()));
        jobEntity.setAllowFailure(false);
        jobEntity.setArtifacts(Arrays.asList("target/*.jar"));
        jobEntity.setCreatedAt(Instant.now().minusSeconds(86400)); // 1 day ago
        jobEntity.setUpdatedAt(Instant.now());

        // Create job execution entity
        UUID jobExecutionId = UUID.randomUUID();
        jobExecutionEntity = new JobExecutionEntity();
        jobExecutionEntity.setId(jobExecutionId);
        jobExecutionEntity.setStageExecution(stageExecutionEntity);
        jobExecutionEntity.setJobId(jobId);
        jobExecutionEntity.setCommitHash("abc1234");
        jobExecutionEntity.setLocal(false);
        jobExecutionEntity.setStatus(ExecutionStatus.SUCCESS);
        jobExecutionEntity.setStartTime(Instant.now().minusSeconds(120));
        jobExecutionEntity.setCompletionTime(Instant.now());
        jobExecutionEntity.setAllowFailure(false);
    }

    @Test
    public void testToDTO_WithValidEntity() {
        // Act
        JobExecutionDTO dto = mapper.toDTO(jobExecutionEntity);

        // Assert
        assertNotNull(dto);
        assertEquals(jobExecutionEntity.getId(), dto.getId());
        assertEquals(stageExecutionEntity.getId(), dto.getStageExecutionId());
        assertEquals(jobExecutionEntity.getJobId(), dto.getJobId());
        assertEquals(jobExecutionEntity.getCommitHash(), dto.getCommitHash());
        assertEquals(jobExecutionEntity.isLocal(), dto.isLocal());
        assertEquals(jobExecutionEntity.getStatus(), dto.getStatus());
        assertEquals(jobExecutionEntity.getStartTime(), dto.getStartTime());
        assertEquals(jobExecutionEntity.getCompletionTime(), dto.getCompletionTime());
        assertEquals(jobExecutionEntity.isAllowFailure(), dto.isAllowFailure());
    }

    @Test
    public void testToDTO_WithNullEntity() {
        // Act
        JobExecutionDTO dto = mapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    public void testToJobExecutionDto_WithBothEntities() {
        // Act
        JobExecutionDTO dto = mapper.toJobExecutionDto(jobExecutionEntity, jobEntity);

        // Assert
        assertNotNull(dto);
        // Check execution properties
        assertEquals(jobExecutionEntity.getId(), dto.getId());
        assertEquals(stageExecutionEntity.getId(), dto.getStageExecutionId());
        assertEquals(jobExecutionEntity.getStatus(), dto.getStatus());

        // Check job properties
        assertNotNull(dto.getJob());
        assertEquals(jobEntity.getId(), dto.getJob().getId());
        assertEquals(jobEntity.getName(), dto.getJob().getName());
        assertEquals(jobEntity.getDockerImage(), dto.getJob().getDockerImage());
        assertEquals(jobEntity.getWorkingDir(), dto.getJob().getWorkingDir());
        assertEquals(jobEntity.isAllowFailure(), dto.getJob().isAllowFailure());

        // Check lists
        assertEquals(jobEntity.getScript().size(), dto.getJob().getScript().size());
        assertEquals(jobEntity.getDependencies().size(), dto.getJob().getDependencies().size());
        assertEquals(jobEntity.getArtifacts().size(), dto.getJob().getArtifacts().size());
    }

    @Test
    public void testToJobExecutionDto_WithNullJobEntity() {
        // Act
        JobExecutionDTO dto = mapper.toJobExecutionDto(jobExecutionEntity, null);

        // Assert
        assertNotNull(dto);
        assertEquals(jobExecutionEntity.getId(), dto.getId());
        assertNull(dto.getJob());
    }

    @Test
    public void testToJobExecutionDto_WithNullExecutionEntity() {
        // Act
        JobExecutionDTO dto = mapper.toJobExecutionDto(null, jobEntity);

        // Assert
        assertNull(dto);
    }

    @Test
    public void testToJobDto_WithValidEntity() {
        // Act
        JobDTO dto = mapper.toJobDto(jobEntity);

        // Assert
        assertNotNull(dto);
        assertEquals(jobEntity.getId(), dto.getId());
        assertEquals(jobEntity.getStageId(), dto.getStageId());
        assertEquals(jobEntity.getName(), dto.getName());
        assertEquals(jobEntity.getDockerImage(), dto.getDockerImage());
        assertEquals(jobEntity.getWorkingDir(), dto.getWorkingDir());
        assertEquals(jobEntity.isAllowFailure(), dto.isAllowFailure());
        assertEquals(jobEntity.getCreatedAt(), dto.getCreatedAt());
        assertEquals(jobEntity.getUpdatedAt(), dto.getUpdatedAt());

        // Check lists
        assertEquals(jobEntity.getScript().size(), dto.getScript().size());
        assertTrue(dto.getScript().containsAll(jobEntity.getScript()));
        assertEquals(jobEntity.getDependencies().size(), dto.getDependencies().size());
        assertTrue(dto.getDependencies().containsAll(jobEntity.getDependencies()));
        assertEquals(jobEntity.getArtifacts().size(), dto.getArtifacts().size());
        assertTrue(dto.getArtifacts().containsAll(jobEntity.getArtifacts()));
    }

    @Test
    public void testToJobDto_WithNullEntity() {
        // Act
        JobDTO dto = mapper.toJobDto(null);

        // Assert
        assertNull(dto);
    }

    @Test
    public void testToJobDto_WithNullCollections() {
        // Arrange
        JobEntity entityWithNullLists = new JobEntity();
        entityWithNullLists.setId(UUID.randomUUID());
        entityWithNullLists.setName("test-job");
        // Script, dependencies, and artifacts are null

        // Act
        JobDTO dto = mapper.toJobDto(entityWithNullLists);

        // Assert
        assertNotNull(dto);
        assertEquals(entityWithNullLists.getId(), dto.getId());
        assertEquals(entityWithNullLists.getName(), dto.getName());
        // Check that null collections are handled
        assertNull(dto.getScript());
        assertNull(dto.getDependencies());
        assertNull(dto.getArtifacts());
    }

    @Test
    public void testToJobDto_WithEmptyCollections() {
        // Arrange
        JobEntity entityWithEmptyLists = new JobEntity();
        entityWithEmptyLists.setId(UUID.randomUUID());
        entityWithEmptyLists.setName("test-job");
        entityWithEmptyLists.setScript(new ArrayList<>());
        entityWithEmptyLists.setDependencies(new ArrayList<>());
        entityWithEmptyLists.setArtifacts(new ArrayList<>());

        // Act
        JobDTO dto = mapper.toJobDto(entityWithEmptyLists);

        // Assert
        assertNotNull(dto);
        assertEquals(entityWithEmptyLists.getId(), dto.getId());
        assertEquals(entityWithEmptyLists.getName(), dto.getName());
        // Check that empty collections are preserved
        assertNotNull(dto.getScript());
        assertTrue(dto.getScript().isEmpty());
        assertNotNull(dto.getDependencies());
        assertTrue(dto.getDependencies().isEmpty());
        assertNotNull(dto.getArtifacts());
        assertTrue(dto.getArtifacts().isEmpty());
    }

    @Test
    public void testToJobDto_WithNullTimestamps() {
        // Arrange
        JobEntity entityWithNullTimestamps = new JobEntity();
        entityWithNullTimestamps.setId(UUID.randomUUID());
        entityWithNullTimestamps.setName("test-job");
        // Timestamps are null

        // Act
        JobDTO dto = mapper.toJobDto(entityWithNullTimestamps);

        // Assert
        assertNotNull(dto);
        assertEquals(entityWithNullTimestamps.getId(), dto.getId());
        assertEquals(entityWithNullTimestamps.getName(), dto.getName());
        // Check that null timestamps are handled
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }
}