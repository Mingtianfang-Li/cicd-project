package edu.neu.cs6510.sp25.t1.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;

public class StageReportDTOTest {

    @Test
    public void testEmptyConstructor() {
        // Using the no-args constructor
        StageReportDTO stageReportDTO = new StageReportDTO();

        // Verify all fields are initialized to null
        assertNull(stageReportDTO.getId());
        assertNull(stageReportDTO.getName());
        assertNull(stageReportDTO.getStatus());
        assertNull(stageReportDTO.getStartTime());
        assertNull(stageReportDTO.getCompletionTime());
        assertNull(stageReportDTO.getJobs());
    }

    @Test
    public void testParameterizedConstructor() {
        // Create test data
        UUID id = UUID.randomUUID();
        String name = "build";
        ExecutionStatus status = ExecutionStatus.SUCCESS;
        Instant startTime = Instant.now().minusSeconds(30);
        Instant completionTime = Instant.now();
        List<JobReportDTO> jobs = createSampleJobReports();

        // Use all-args constructor
        StageReportDTO stageReportDTO = new StageReportDTO(
                id, name, status, startTime, completionTime, jobs);

        // Verify all fields are set correctly
        assertEquals(id, stageReportDTO.getId());
        assertEquals(name, stageReportDTO.getName());
        assertEquals(status, stageReportDTO.getStatus());
        assertEquals(startTime, stageReportDTO.getStartTime());
        assertEquals(completionTime, stageReportDTO.getCompletionTime());
        assertEquals(jobs, stageReportDTO.getJobs());
        assertEquals(2, stageReportDTO.getJobs().size());
    }

    @Test
    public void testSettersAndGetters() {
        // Create an empty DTO
        StageReportDTO stageReportDTO = new StageReportDTO();

        // Create test data
        UUID id = UUID.randomUUID();
        String name = "test-stage";
        ExecutionStatus status = ExecutionStatus.RUNNING;
        Instant startTime = Instant.now();
        Instant completionTime = null; // Still running, no completion time
        List<JobReportDTO> jobs = createSampleJobReports();

        // Use setters
        stageReportDTO.setId(id);
        stageReportDTO.setName(name);
        stageReportDTO.setStatus(status);
        stageReportDTO.setStartTime(startTime);
        stageReportDTO.setCompletionTime(completionTime);
        stageReportDTO.setJobs(jobs);

        // Verify getters return correct values
        assertEquals(id, stageReportDTO.getId());
        assertEquals(name, stageReportDTO.getName());
        assertEquals(status, stageReportDTO.getStatus());
        assertEquals(startTime, stageReportDTO.getStartTime());
        assertEquals(completionTime, stageReportDTO.getCompletionTime());
        assertEquals(jobs, stageReportDTO.getJobs());
    }

    @Test
    public void testWithNullJobs() {
        // Create a DTO with null jobs list
        StageReportDTO stageReportDTO = new StageReportDTO(
                UUID.randomUUID(),
                "deploy",
                ExecutionStatus.PENDING,
                Instant.now(),
                null,
                null);

        // Verify jobs list is null
        assertNull(stageReportDTO.getJobs());

        // Set an empty list
        stageReportDTO.setJobs(new ArrayList<>());

        // Verify jobs list is empty but not null
        assertNotNull(stageReportDTO.getJobs());
        assertTrue(stageReportDTO.getJobs().isEmpty());
    }

    @Test
    public void testDifferentExecutionStatuses() {
        StageReportDTO stageReportDTO = new StageReportDTO();

        // Test all possible execution statuses
        stageReportDTO.setStatus(ExecutionStatus.PENDING);
        assertEquals(ExecutionStatus.PENDING, stageReportDTO.getStatus());

        stageReportDTO.setStatus(ExecutionStatus.RUNNING);
        assertEquals(ExecutionStatus.RUNNING, stageReportDTO.getStatus());

        stageReportDTO.setStatus(ExecutionStatus.SUCCESS);
        assertEquals(ExecutionStatus.SUCCESS, stageReportDTO.getStatus());

        stageReportDTO.setStatus(ExecutionStatus.FAILED);
        assertEquals(ExecutionStatus.FAILED, stageReportDTO.getStatus());

        stageReportDTO.setStatus(ExecutionStatus.CANCELED);
        assertEquals(ExecutionStatus.CANCELED, stageReportDTO.getStatus());
    }

    @Test
    public void testJobsListModification() {
        // Create a stage report with some jobs
        StageReportDTO stageReportDTO = new StageReportDTO();

        // Use ArrayList instead of the immutable list created by List.of()
        List<JobReportDTO> initialJobs = new ArrayList<>(createSampleJobReports());
        stageReportDTO.setJobs(initialJobs);

        // Verify initial size
        assertEquals(2, stageReportDTO.getJobs().size());

        // Modify the jobs list directly through the getter
        stageReportDTO.getJobs().add(createSimpleJobReport("third-job", ExecutionStatus.PENDING));

        // Verify the list was modified (this demonstrates that the getter returns a
        // reference)
        assertEquals(3, stageReportDTO.getJobs().size());
        assertEquals("third-job", stageReportDTO.getJobs().get(2).getName());
    }

    // Helper method to create sample JobReportDTOs
    private List<JobReportDTO> createSampleJobReports() {
        JobReportDTO job1 = createSimpleJobReport("compile", ExecutionStatus.SUCCESS);
        JobReportDTO job2 = createSimpleJobReport("test", ExecutionStatus.RUNNING);

        // Use ArrayList to ensure a mutable list is returned
        return new ArrayList<>(Arrays.asList(job1, job2));
    }

    private JobReportDTO createSimpleJobReport(String name, ExecutionStatus status) {
        // Create a simple execution record for this job
        JobReportDTO.ExecutionRecord record = new JobReportDTO.ExecutionRecord(
                UUID.randomUUID(),
                status,
                Instant.now().minusSeconds(10),
                status == ExecutionStatus.RUNNING ? null : Instant.now(),
                false);

        // Use the required constructor with parameters
        JobReportDTO job = new JobReportDTO(name, List.of(record));

        return job;
    }
}