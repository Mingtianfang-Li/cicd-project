package edu.neu.cs6510.sp25.t1.worker.api.controller;

import edu.neu.cs6510.sp25.t1.common.dto.JobDTO;
import edu.neu.cs6510.sp25.t1.common.dto.JobExecutionDTO;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import edu.neu.cs6510.sp25.t1.worker.service.JobDataService;
import edu.neu.cs6510.sp25.t1.worker.service.WorkerJobQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkerStatusControllerTest {

    @Mock
    private WorkerJobQueue jobQueue;

    @Mock
    private JobDataService jobDataService;

    @InjectMocks
    private WorkerStatusController controller;

    private UUID jobId1;
    private UUID jobId2;
    private JobExecutionDTO jobExecution1;
    private JobExecutionDTO jobExecution2;

    @BeforeEach
    void setUp() {
        // Initialize test data
        jobId1 = UUID.randomUUID();
        jobId2 = UUID.randomUUID();

        JobDTO jobDTO1 = new JobDTO();
        jobDTO1.setName("job1");

        jobExecution1 = new JobExecutionDTO();
        jobExecution1.setId(jobId1);
        jobExecution1.setStatus(ExecutionStatus.RUNNING);
        jobExecution1.setAllowFailure(false);
        jobExecution1.setJob(jobDTO1);

        JobDTO jobDTO2 = new JobDTO();
        jobDTO2.setName("job2");

        jobExecution2 = new JobExecutionDTO();
        jobExecution2.setId(jobId2);
        jobExecution2.setStatus(ExecutionStatus.PENDING);
        jobExecution2.setAllowFailure(true);
        jobExecution2.setJob(jobDTO2);
    }

    @Test
    void getWorkerStatus_ShouldReturnCorrectStatus() {
        // Arrange
        when(jobQueue.getActiveJobCount()).thenReturn(2);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.getWorkerStatus();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();
        assertEquals("running", body.get("status"));
        assertEquals(2, body.get("active_jobs"));

        verify(jobQueue, times(1)).getActiveJobCount();
    }

    @Test
    void getActiveJobs_ShouldReturnJobList() {
        // Arrange
        List<UUID> activeJobIds = Arrays.asList(jobId1, jobId2);
        when(jobQueue.getActiveJobIds()).thenReturn(activeJobIds);
        when(jobDataService.getJobExecutionById(jobId1)).thenReturn(Optional.of(jobExecution1));
        when(jobDataService.getJobExecutionById(jobId2)).thenReturn(Optional.of(jobExecution2));

        // Act
        ResponseEntity<Map<String, Object>> response = controller.getActiveJobs();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();
        assertEquals(2, body.get("active_job_count"));

        @SuppressWarnings("unchecked")
        List<JobExecutionDTO> jobs = (List<JobExecutionDTO>) body.get("jobs");
        assertNotNull(jobs);
        assertEquals(2, jobs.size());
        assertEquals(jobId1, jobs.get(0).getId());
        assertEquals(jobId2, jobs.get(1).getId());

        verify(jobQueue, times(1)).getActiveJobIds();
        verify(jobDataService, times(1)).getJobExecutionById(jobId1);
        verify(jobDataService, times(1)).getJobExecutionById(jobId2);
    }

    @Test
    void getActiveJobs_WithNullJobExecution_ShouldFilterOutNull() {
        // Arrange
        List<UUID> activeJobIds = Arrays.asList(jobId1, jobId2);
        when(jobQueue.getActiveJobIds()).thenReturn(activeJobIds);
        when(jobDataService.getJobExecutionById(jobId1)).thenReturn(Optional.of(jobExecution1));
        when(jobDataService.getJobExecutionById(jobId2)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, Object>> response = controller.getActiveJobs();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();
        assertEquals(2, body.get("active_job_count")); // Original count from jobQueue

        @SuppressWarnings("unchecked")
        List<JobExecutionDTO> jobs = (List<JobExecutionDTO>) body.get("jobs");
        assertNotNull(jobs);
        assertEquals(1, jobs.size()); // Only one job should be present
        assertEquals(jobId1, jobs.get(0).getId());

        verify(jobQueue, times(1)).getActiveJobIds();
        verify(jobDataService, times(1)).getJobExecutionById(jobId1);
        verify(jobDataService, times(1)).getJobExecutionById(jobId2);
    }

    @Test
    void cancelJob_WhenSuccessful_ShouldReturnOk() {
        // Arrange
        when(jobQueue.cancelJob(jobId1)).thenReturn(true);
        doNothing().when(jobDataService).updateJobStatus(eq(jobId1), eq(ExecutionStatus.CANCELED), anyString());

        // Act
        ResponseEntity<Map<String, String>> response = controller.cancelJob(jobId1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, String> body = response.getBody();
        assertEquals("cancelled", body.get("status"));
        assertEquals("Job cancelled successfully", body.get("message"));

        verify(jobQueue, times(1)).cancelJob(jobId1);
        verify(jobDataService, times(1)).updateJobStatus(eq(jobId1), eq(ExecutionStatus.CANCELED), anyString());
    }

    @Test
    void cancelJob_WhenFailure_ShouldReturnBadRequest() {
        // Arrange
        when(jobQueue.cancelJob(jobId1)).thenReturn(false);

        // Act
        ResponseEntity<Map<String, String>> response = controller.cancelJob(jobId1);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, String> body = response.getBody();
        assertEquals("failed", body.get("status"));
        assertEquals("Could not cancel job. It might be completed already or not found.", body.get("message"));

        verify(jobQueue, times(1)).cancelJob(jobId1);
        verify(jobDataService, never()).updateJobStatus(any(), any(), anyString());
    }

    @Test
    void getWorkerInfo_ShouldReturnCorrectInfo() {
        // Act
        ResponseEntity<Map<String, Object>> response = controller.getWorkerInfo();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();
        assertEquals(5, body.get("max_concurrent_jobs"));
        assertEquals("docker", body.get("executor_type"));
        assertNotNull(body.get("worker_id"));
        assertTrue(body.get("worker_id") instanceof UUID);
    }
}