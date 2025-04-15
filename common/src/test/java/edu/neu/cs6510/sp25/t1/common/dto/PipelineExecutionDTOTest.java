package edu.neu.cs6510.sp25.t1.common.dto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;

public class PipelineExecutionDTOTest {

    @Test
    public void testEmptyConstructor() {
        // Using the no-args constructor
        PipelineExecutionDTO pipelineExecutionDTO = new PipelineExecutionDTO();

        // Verify all fields are initialized to null or default values
        assertNull(pipelineExecutionDTO.getId());
        assertNull(pipelineExecutionDTO.getPipelineId());
        assertEquals(0, pipelineExecutionDTO.getRunNumber());
        assertNull(pipelineExecutionDTO.getCommitHash());
        assertFalse(pipelineExecutionDTO.isLocal());
        assertNull(pipelineExecutionDTO.getStatus());
        assertNull(pipelineExecutionDTO.getStartTime());
        assertNull(pipelineExecutionDTO.getCompletionTime());
    }

    @Test
    public void testAllArgsConstructor() {
        // Create test data
        UUID id = UUID.randomUUID();
        UUID pipelineId = UUID.randomUUID();
        int runNumber = 42;
        String commitHash = "abc123def456";
        boolean isLocal = true;
        ExecutionStatus status = ExecutionStatus.RUNNING;
        Instant startTime = Instant.now();
        Instant completionTime = Instant.now().plusSeconds(300);

        // Use all-args constructor
        PipelineExecutionDTO pipelineExecutionDTO = new PipelineExecutionDTO(
                id, pipelineId, runNumber, commitHash, isLocal, status,
                startTime, completionTime
        );

        // Verify all fields are set correctly
        assertEquals(id, pipelineExecutionDTO.getId());
        assertEquals(pipelineId, pipelineExecutionDTO.getPipelineId());
        assertEquals(runNumber, pipelineExecutionDTO.getRunNumber());
        assertEquals(commitHash, pipelineExecutionDTO.getCommitHash());
        assertTrue(pipelineExecutionDTO.isLocal());
        assertEquals(status, pipelineExecutionDTO.getStatus());
        assertEquals(startTime, pipelineExecutionDTO.getStartTime());
        assertEquals(completionTime, pipelineExecutionDTO.getCompletionTime());
    }

    @Test
    public void testBuilder() {
        // Create test data
        UUID id = UUID.randomUUID();
        UUID pipelineId = UUID.randomUUID();
        int runNumber = 7;
        String commitHash = "7890abcdef";
        boolean isLocal = false;
        ExecutionStatus status = ExecutionStatus.SUCCESS;
        Instant startTime = Instant.now().minusSeconds(120);
        Instant completionTime = Instant.now();

        // Use builder pattern
        PipelineExecutionDTO pipelineExecutionDTO = PipelineExecutionDTO.builder()
                .id(id)
                .pipelineId(pipelineId)
                .runNumber(runNumber)
                .commitHash(commitHash)
                .isLocal(isLocal)
                .status(status)
                .startTime(startTime)
                .completionTime(completionTime)
                .build();

        // Verify all fields are set correctly
        assertEquals(id, pipelineExecutionDTO.getId());
        assertEquals(pipelineId, pipelineExecutionDTO.getPipelineId());
        assertEquals(runNumber, pipelineExecutionDTO.getRunNumber());
        assertEquals(commitHash, pipelineExecutionDTO.getCommitHash());
        assertFalse(pipelineExecutionDTO.isLocal());
        assertEquals(status, pipelineExecutionDTO.getStatus());
        assertEquals(startTime, pipelineExecutionDTO.getStartTime());
        assertEquals(completionTime, pipelineExecutionDTO.getCompletionTime());
    }

    @Test
    public void testSettersAndGetters() {
        // Create an empty DTO
        PipelineExecutionDTO pipelineExecutionDTO = new PipelineExecutionDTO();

        // Create test data
        UUID id = UUID.randomUUID();
        UUID pipelineId = UUID.randomUUID();
        int runNumber = 15;
        String commitHash = "commit123hash456";
        boolean isLocal = true;
        ExecutionStatus status = ExecutionStatus.PENDING;
        Instant startTime = Instant.now();
        Instant completionTime = Instant.now().plusSeconds(180);

        // Use setters
        pipelineExecutionDTO.setId(id);
        pipelineExecutionDTO.setPipelineId(pipelineId);
        pipelineExecutionDTO.setRunNumber(runNumber);
        pipelineExecutionDTO.setCommitHash(commitHash);
        pipelineExecutionDTO.setLocal(isLocal);
        pipelineExecutionDTO.setStatus(status);
        pipelineExecutionDTO.setStartTime(startTime);
        pipelineExecutionDTO.setCompletionTime(completionTime);

        // Verify getters return correct values
        assertEquals(id, pipelineExecutionDTO.getId());
        assertEquals(pipelineId, pipelineExecutionDTO.getPipelineId());
        assertEquals(runNumber, pipelineExecutionDTO.getRunNumber());
        assertEquals(commitHash, pipelineExecutionDTO.getCommitHash());
        assertTrue(pipelineExecutionDTO.isLocal());
        assertEquals(status, pipelineExecutionDTO.getStatus());
        assertEquals(startTime, pipelineExecutionDTO.getStartTime());
        assertEquals(completionTime, pipelineExecutionDTO.getCompletionTime());
    }

    @Test
    public void testDifferentExecutionStatuses() {
        PipelineExecutionDTO pipelineExecutionDTO = new PipelineExecutionDTO();

        // Test all possible execution statuses
        pipelineExecutionDTO.setStatus(ExecutionStatus.PENDING);
        assertEquals(ExecutionStatus.PENDING, pipelineExecutionDTO.getStatus());

        pipelineExecutionDTO.setStatus(ExecutionStatus.RUNNING);
        assertEquals(ExecutionStatus.RUNNING, pipelineExecutionDTO.getStatus());

        pipelineExecutionDTO.setStatus(ExecutionStatus.SUCCESS);
        assertEquals(ExecutionStatus.SUCCESS, pipelineExecutionDTO.getStatus());

        pipelineExecutionDTO.setStatus(ExecutionStatus.FAILED);
        assertEquals(ExecutionStatus.FAILED, pipelineExecutionDTO.getStatus());

        pipelineExecutionDTO.setStatus(ExecutionStatus.CANCELED);
        assertEquals(ExecutionStatus.CANCELED, pipelineExecutionDTO.getStatus());
    }

    @Test
    public void testNegativeRunNumber() {
        // Create a DTO with a negative run number
        PipelineExecutionDTO pipelineExecutionDTO = PipelineExecutionDTO.builder()
                .runNumber(-5)
                .build();

        // Verify the run number is set as provided (no validation in DTO)
        assertEquals(-5, pipelineExecutionDTO.getRunNumber());

        // Update to a positive run number
        pipelineExecutionDTO.setRunNumber(10);
        assertEquals(10, pipelineExecutionDTO.getRunNumber());
    }

    @Test
    public void testZeroRunNumber() {
        // Create a DTO with run number 0
        PipelineExecutionDTO pipelineExecutionDTO = PipelineExecutionDTO.builder()
                .runNumber(0)
                .build();

        // Verify the run number is set as provided
        assertEquals(0, pipelineExecutionDTO.getRunNumber());
    }

    @Test
    public void testTemporalConsistency() {
        // Create test data with completion time before start time
        Instant startTime = Instant.now();
        Instant completionTime = startTime.minusSeconds(60); // Completion is before start

        // Create DTO with inconsistent temporal data
        PipelineExecutionDTO pipelineExecutionDTO = PipelineExecutionDTO.builder()
                .startTime(startTime)
                .completionTime(completionTime)
                .build();

        // Verify values are stored as provided (no validation in DTO)
        assertEquals(startTime, pipelineExecutionDTO.getStartTime());
        assertEquals(completionTime, pipelineExecutionDTO.getCompletionTime());

        // Note: In a real application, validation logic should be added to ensure
        // completionTime is after startTime, but since this is just a DTO test,
        // we're verifying the current behavior even if not ideal
    }
}