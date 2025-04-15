package edu.neu.cs6510.sp25.t1.common.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;

public class JobExecutionDTOTest {

    @Test
    public void testEmptyConstructor() {
        // Using the no-args constructor
        JobExecutionDTO jobExecutionDTO = new JobExecutionDTO();

        // Verify all fields are initialized to null or default values
        assertNull(jobExecutionDTO.getId());
        assertNull(jobExecutionDTO.getStageExecutionId());
        assertNull(jobExecutionDTO.getJobId());
        assertNull(jobExecutionDTO.getCommitHash());
        assertFalse(jobExecutionDTO.isLocal());
        assertNull(jobExecutionDTO.getStatus());
        assertNull(jobExecutionDTO.getStartTime());
        assertNull(jobExecutionDTO.getCompletionTime());
        assertFalse(jobExecutionDTO.isAllowFailure());
        assertNull(jobExecutionDTO.getJob());
    }

    @Test
    public void testAllArgsConstructor() {
        // Create test data
        UUID id = UUID.randomUUID();
        UUID stageExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        String commitHash = "abc123def456";
        boolean isLocal = true;
        ExecutionStatus status = ExecutionStatus.RUNNING;
        Instant startTime = Instant.now();
        Instant completionTime = Instant.now().plusSeconds(30);
        boolean allowFailure = true;
        JobDTO job = createSampleJobDTO();

        // Use all-args constructor
        JobExecutionDTO jobExecutionDTO = new JobExecutionDTO(
                id, stageExecutionId, jobId, commitHash, isLocal, status,
                startTime, completionTime, allowFailure, job
        );

        // Verify all fields are set correctly
        assertEquals(id, jobExecutionDTO.getId());
        assertEquals(stageExecutionId, jobExecutionDTO.getStageExecutionId());
        assertEquals(jobId, jobExecutionDTO.getJobId());
        assertEquals(commitHash, jobExecutionDTO.getCommitHash());
        assertTrue(jobExecutionDTO.isLocal());
        assertEquals(status, jobExecutionDTO.getStatus());
        assertEquals(startTime, jobExecutionDTO.getStartTime());
        assertEquals(completionTime, jobExecutionDTO.getCompletionTime());
        assertTrue(jobExecutionDTO.isAllowFailure());
        assertEquals(job, jobExecutionDTO.getJob());
    }

    @Test
    public void testBuilder() {
        // Create test data
        UUID id = UUID.randomUUID();
        UUID stageExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        String commitHash = "7890abcdef";
        boolean isLocal = false;
        ExecutionStatus status = ExecutionStatus.SUCCESS;
        Instant startTime = Instant.now().minusSeconds(60);
        Instant completionTime = Instant.now();
        boolean allowFailure = false;
        JobDTO job = createSampleJobDTO();

        // Use builder pattern
        JobExecutionDTO jobExecutionDTO = JobExecutionDTO.builder()
                .id(id)
                .stageExecutionId(stageExecutionId)
                .jobId(jobId)
                .commitHash(commitHash)
                .isLocal(isLocal)
                .status(status)
                .startTime(startTime)
                .completionTime(completionTime)
                .allowFailure(allowFailure)
                .job(job)
                .build();

        // Verify all fields are set correctly
        assertEquals(id, jobExecutionDTO.getId());
        assertEquals(stageExecutionId, jobExecutionDTO.getStageExecutionId());
        assertEquals(jobId, jobExecutionDTO.getJobId());
        assertEquals(commitHash, jobExecutionDTO.getCommitHash());
        assertFalse(jobExecutionDTO.isLocal());
        assertEquals(status, jobExecutionDTO.getStatus());
        assertEquals(startTime, jobExecutionDTO.getStartTime());
        assertEquals(completionTime, jobExecutionDTO.getCompletionTime());
        assertFalse(jobExecutionDTO.isAllowFailure());
        assertEquals(job, jobExecutionDTO.getJob());
    }

    @Test
    public void testSettersAndGetters() {
        // Create an empty DTO
        JobExecutionDTO jobExecutionDTO = new JobExecutionDTO();

        // Create test data
        UUID id = UUID.randomUUID();
        UUID stageExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        String commitHash = "abc123def456";
        boolean isLocal = true;
        ExecutionStatus status = ExecutionStatus.PENDING;
        Instant startTime = Instant.now();
        Instant completionTime = Instant.now().plusSeconds(45);
        boolean allowFailure = true;
        JobDTO job = createSampleJobDTO();

        // Use setters
        jobExecutionDTO.setId(id);
        jobExecutionDTO.setStageExecutionId(stageExecutionId);
        jobExecutionDTO.setJobId(jobId);
        jobExecutionDTO.setCommitHash(commitHash);
        jobExecutionDTO.setLocal(isLocal);
        jobExecutionDTO.setStatus(status);
        jobExecutionDTO.setStartTime(startTime);
        jobExecutionDTO.setCompletionTime(completionTime);
        jobExecutionDTO.setAllowFailure(allowFailure);
        jobExecutionDTO.setJob(job);

        // Verify getters return correct values
        assertEquals(id, jobExecutionDTO.getId());
        assertEquals(stageExecutionId, jobExecutionDTO.getStageExecutionId());
        assertEquals(jobId, jobExecutionDTO.getJobId());
        assertEquals(commitHash, jobExecutionDTO.getCommitHash());
        assertTrue(jobExecutionDTO.isLocal());
        assertEquals(status, jobExecutionDTO.getStatus());
        assertEquals(startTime, jobExecutionDTO.getStartTime());
        assertEquals(completionTime, jobExecutionDTO.getCompletionTime());
        assertTrue(jobExecutionDTO.isAllowFailure());
        assertEquals(job, jobExecutionDTO.getJob());
    }

    @Test
    public void testDifferentExecutionStatuses() {
        JobExecutionDTO jobExecutionDTO = new JobExecutionDTO();

        // Test all possible execution statuses
        jobExecutionDTO.setStatus(ExecutionStatus.PENDING);
        assertEquals(ExecutionStatus.PENDING, jobExecutionDTO.getStatus());

        jobExecutionDTO.setStatus(ExecutionStatus.RUNNING);
        assertEquals(ExecutionStatus.RUNNING, jobExecutionDTO.getStatus());

        jobExecutionDTO.setStatus(ExecutionStatus.SUCCESS);
        assertEquals(ExecutionStatus.SUCCESS, jobExecutionDTO.getStatus());

        jobExecutionDTO.setStatus(ExecutionStatus.FAILED);
        assertEquals(ExecutionStatus.FAILED, jobExecutionDTO.getStatus());

        jobExecutionDTO.setStatus(ExecutionStatus.CANCELED);
        assertEquals(ExecutionStatus.CANCELED, jobExecutionDTO.getStatus());
    }

    @Test
    public void testJobDTOIntegration() {
        // Create a JobExecutionDTO with a non-null JobDTO
        JobDTO jobDTO = createSampleJobDTO();
        JobExecutionDTO jobExecutionDTO = new JobExecutionDTO();
        jobExecutionDTO.setJob(jobDTO);

        // Verify the JobDTO is correctly associated
        assertNotNull(jobExecutionDTO.getJob());
        assertEquals(jobDTO.getId(), jobExecutionDTO.getJob().getId());
        assertEquals(jobDTO.getName(), jobExecutionDTO.getJob().getName());
        assertEquals(jobDTO.getDockerImage(), jobExecutionDTO.getJob().getDockerImage());
        assertEquals(jobDTO.getScript(), jobExecutionDTO.getJob().getScript());
    }

    // Helper method to create a sample JobDTO
    private JobDTO createSampleJobDTO() {
        return JobDTO.builder()
                .id(UUID.randomUUID())
                .stageId(UUID.randomUUID())
                .name("test-job")
                .dockerImage("gradle:8.12-jdk21")
                .script(Arrays.asList("./gradlew test", "./gradlew check"))
                .workingDir("/app/project")
                .allowFailure(false)
                .build();
    }
}