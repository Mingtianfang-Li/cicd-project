package edu.neu.cs6510.sp25.t1.worker.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import edu.neu.cs6510.sp25.t1.common.dto.JobDTO;
import edu.neu.cs6510.sp25.t1.common.dto.JobExecutionDTO;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
public class WorkerJobQueueTest {

    @Mock
    private WorkerExecutionService executionService;

    @Mock
    private JobDataService jobDataService;

    private WorkerJobQueue workerJobQueue;

    @BeforeEach
    public void setUp() {
        workerJobQueue = new WorkerJobQueue(executionService, jobDataService);
    }

    @Test
    public void testConsumeJob_validJobId_executesJob() throws InterruptedException {
        // Given
        UUID jobId = UUID.randomUUID();
        String jobIdStr = jobId.toString();
        JobExecutionDTO jobExecution = new JobExecutionDTO();
        jobExecution.setId(jobId);
        JobDTO jobDTO = new JobDTO();
        jobDTO.setName("Test Job");
        jobExecution.setJob(jobDTO);

        when(jobDataService.getJobExecutionById(jobId)).thenReturn(Optional.of(jobExecution));

        // Create a countdown latch to wait for async execution
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(executionService).executeJob(any(JobExecutionDTO.class));

        // When
        workerJobQueue.consumeJob(jobIdStr);

        // Then
        // Wait for async task to complete
        assertTrue(latch.await(2, TimeUnit.SECONDS), "Execution did not complete in time");

        verify(jobDataService).getJobExecutionById(jobId);
        verify(jobDataService).updateJobStatus(eq(jobId), eq(ExecutionStatus.RUNNING), anyString());
        verify(executionService).executeJob(jobExecution);
    }

    @Test
    public void testConsumeJob_invalidJobId_logsError() {
        // Given
        String invalidJobId = "not-a-uuid";

        // When
        workerJobQueue.consumeJob(invalidJobId);

        // Then
        verifyNoInteractions(executionService);
        verify(jobDataService, never()).updateJobStatus(any(), any(), anyString());
    }

    @Test
    public void testConsumeJob_nullJobId_logsError() {
        // Given
        String nullJobId = null;

        // When
        workerJobQueue.consumeJob(nullJobId);

        // Then
        verifyNoInteractions(executionService);
        verifyNoInteractions(jobDataService);
    }

    @Test
    public void testConsumeJob_emptyJobId_logsError() {
        // Given
        String emptyJobId = "";

        // When
        workerJobQueue.consumeJob(emptyJobId);

        // Then
        verifyNoInteractions(executionService);
        verifyNoInteractions(jobDataService);
    }


    @Test
    public void testConsumeJob_jobNotFound_logsError() {
        // Given
        UUID jobId = UUID.randomUUID();
        String jobIdStr = jobId.toString();

        when(jobDataService.getJobExecutionById(jobId)).thenReturn(Optional.empty());

        // When
        workerJobQueue.consumeJob(jobIdStr);

        // Then
        verifyNoInteractions(executionService);
        verify(jobDataService, never()).updateJobStatus(any(), any(), anyString());
    }

    @Test
    public void testGetActiveJobCount_returnsCorrectCount() {
        // Given
        UUID jobId1 = UUID.randomUUID();
        UUID jobId2 = UUID.randomUUID();
        JobExecutionDTO job1 = new JobExecutionDTO();
        JobExecutionDTO job2 = new JobExecutionDTO();
        job1.setId(jobId1);
        job2.setId(jobId2);

        when(jobDataService.getJobExecutionById(jobId1)).thenReturn(Optional.of(job1));
        when(jobDataService.getJobExecutionById(jobId2)).thenReturn(Optional.of(job2));

        // When
        workerJobQueue.consumeJob(jobId1.toString());
        workerJobQueue.consumeJob(jobId2.toString());

        // Then
        assertEquals(2, workerJobQueue.getActiveJobCount());
    }

    @Test
    public void testCancelJob_jobRunning_cancelsSuccessfully() throws InterruptedException {
        // Given - setup a job that will block for a while
        UUID jobId = UUID.randomUUID();
        JobExecutionDTO jobExecution = new JobExecutionDTO();
        jobExecution.setId(jobId);

        when(jobDataService.getJobExecutionById(jobId)).thenReturn(Optional.of(jobExecution));

        CountDownLatch blockLatch = new CountDownLatch(1);

        doAnswer(invocation -> {
            // This will block until the test releases it or timeout
            blockLatch.await(5, TimeUnit.SECONDS);
            return null;
        }).when(executionService).executeJob(any());

        // Start the job
        workerJobQueue.consumeJob(jobId.toString());

        // Give the job a moment to start
        Thread.sleep(100);

        // When
        boolean result = workerJobQueue.cancelJob(jobId);

        // Release the blocked thread
        blockLatch.countDown();

        // Then
        assertTrue(result);
        assertEquals(0, workerJobQueue.getActiveJobCount());
    }

    @Test
    public void testCancelJob_jobNotRunning_returnsFalse() {
        // Given
        UUID jobId = UUID.randomUUID();

        // When
        boolean result = workerJobQueue.cancelJob(jobId);

        // Then
        assertFalse(result);
    }
}