package edu.neu.cs6510.sp25.t1.worker.service;

import edu.neu.cs6510.sp25.t1.common.dto.JobDTO;
import edu.neu.cs6510.sp25.t1.common.dto.JobExecutionDTO;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import edu.neu.cs6510.sp25.t1.worker.execution.DockerExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkerExecutionServiceTest {

    @Mock
    private DockerExecutor dockerExecutor;

    @Mock
    private JobDataService jobDataService;

    @Mock
    private ArtifactService artifactService;

    @InjectMocks
    private WorkerExecutionService workerExecutionService;

    private JobExecutionDTO jobExecution;
    private JobDTO job;

    @BeforeEach
    void setUp() {
        // Create a test job execution
        job = new JobDTO();
        job.setId(UUID.randomUUID());
        job.setName("test-job");
        job.setDockerImage("alpine:latest");
        job.setWorkingDir("/app");
        job.setArtifacts(Arrays.asList("reports/", "logs/*.log"));

        jobExecution = new JobExecutionDTO();
        jobExecution.setId(UUID.randomUUID());
        jobExecution.setJobId(job.getId());
        jobExecution.setJob(job);
        jobExecution.setAllowFailure(false);
    }

    @Test
    void testExecuteJobSuccess() {
        // Setup
        when(dockerExecutor.execute(jobExecution)).thenReturn(ExecutionStatus.SUCCESS);

        // Execute
        workerExecutionService.executeJob(jobExecution);

        // Verify
        verify(dockerExecutor).execute(jobExecution);
        verify(jobDataService).updateJobStatus(jobExecution.getId(), ExecutionStatus.SUCCESS,
                "Job execution completed with status: SUCCESS");
        verify(artifactService).processArtifacts(
                jobExecution.getId(),
                job.getWorkingDir(),
                job.getArtifacts()
        );
    }

    @Test
    void testExecuteJobFailure() {
        // Setup
        when(dockerExecutor.execute(jobExecution)).thenReturn(ExecutionStatus.FAILED);

        // Execute
        workerExecutionService.executeJob(jobExecution);

        // Verify
        verify(dockerExecutor).execute(jobExecution);
        verify(jobDataService).updateJobStatus(jobExecution.getId(), ExecutionStatus.FAILED,
                "Job execution completed with status: FAILED");
        verify(artifactService).processArtifacts(
                jobExecution.getId(),
                job.getWorkingDir(),
                job.getArtifacts()
        );
    }

    @Test
    void testExecuteJobFailureWithAllowFailure() {
        // Setup
        jobExecution.setAllowFailure(true);
        when(dockerExecutor.execute(jobExecution)).thenReturn(ExecutionStatus.FAILED);

        // Execute
        workerExecutionService.executeJob(jobExecution);

        // Verify
        verify(dockerExecutor).execute(jobExecution);
        verify(jobDataService).updateJobStatus(jobExecution.getId(), ExecutionStatus.SUCCESS,
                "Job failed but allowFailure=true. Execution completed.");
        verify(artifactService).processArtifacts(
                jobExecution.getId(),
                job.getWorkingDir(),
                job.getArtifacts()
        );
    }

    @Test
    void testExecuteJobWithException() {
        // Setup
        when(dockerExecutor.execute(jobExecution)).thenThrow(new RuntimeException("Docker execution failed"));

        // Execute
        workerExecutionService.executeJob(jobExecution);

        // Verify
        verify(dockerExecutor).execute(jobExecution);
        verify(jobDataService).updateJobStatus(eq(jobExecution.getId()), eq(ExecutionStatus.FAILED),
                contains("Job execution failed with error:"));
        verify(artifactService, never()).processArtifacts(any(), any(), any());
    }

    @Test
    void testExecuteJobWithNullJob() {
        // Execute
        workerExecutionService.executeJob(null);

        // Verify no interactions with dependencies
        verifyNoInteractions(dockerExecutor);
        verifyNoInteractions(jobDataService);
        verifyNoInteractions(artifactService);
    }

    @Test
    void testExecuteJobWithNullJobId() {
        // Setup
        jobExecution.setId(null);

        // Execute
        workerExecutionService.executeJob(jobExecution);

        // Verify no interactions with dependencies
        verifyNoInteractions(dockerExecutor);
        verifyNoInteractions(jobDataService);
        verifyNoInteractions(artifactService);
    }

    @Test
    void testExecuteJobWithNoArtifacts() {
        // Setup
        job.setArtifacts(null);
        when(dockerExecutor.execute(jobExecution)).thenReturn(ExecutionStatus.SUCCESS);

        // Execute
        workerExecutionService.executeJob(jobExecution);

        // Verify
        verify(dockerExecutor).execute(jobExecution);
        verify(jobDataService).updateJobStatus(jobExecution.getId(), ExecutionStatus.SUCCESS,
                "Job execution completed with status: SUCCESS");
        verifyNoInteractions(artifactService);
    }
}