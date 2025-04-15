package edu.neu.cs6510.sp25.t1.worker.service;

import edu.neu.cs6510.sp25.t1.backend.database.entity.JobEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.JobExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.database.repository.JobExecutionRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.JobRepository;
import edu.neu.cs6510.sp25.t1.backend.mapper.JobExecutionMapper;
import edu.neu.cs6510.sp25.t1.common.dto.JobDTO;
import edu.neu.cs6510.sp25.t1.common.dto.JobExecutionDTO;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobDataServiceTest {

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobExecutionMapper mapper;

    @InjectMocks
    private JobDataService jobDataService;

    @Captor
    private ArgumentCaptor<JobExecutionEntity> jobExecutionCaptor;

    private UUID jobExecutionId;
    private JobExecutionEntity jobExecutionEntity;
    private JobEntity jobEntity;
    private JobExecutionDTO jobExecutionDTO;

    @BeforeEach
    void setUp() {
        jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        // Set up job entity
        jobEntity = new JobEntity();
        jobEntity.setId(jobId);
        jobEntity.setName("test-job");
        jobEntity.setDockerImage("alpine:latest");
        jobEntity.setScript(new ArrayList<>(Arrays.asList("echo Hello", "ls -la")));
        jobEntity.setArtifacts(new ArrayList<>(Arrays.asList("reports/", "logs/*.log")));

        // Set up job execution entity
        jobExecutionEntity = new JobExecutionEntity();
        jobExecutionEntity.setId(jobExecutionId);
        jobExecutionEntity.setJobId(jobId);
        jobExecutionEntity.setStatus(ExecutionStatus.RUNNING);
        jobExecutionEntity.setStartTime(Instant.now());

        // Set up job execution DTO
        JobDTO jobDTO = new JobDTO();
        jobDTO.setId(jobId);
        jobDTO.setName("test-job");

        jobExecutionDTO = new JobExecutionDTO();
        jobExecutionDTO.setId(jobExecutionId);
        jobExecutionDTO.setJobId(jobId);
        jobExecutionDTO.setStatus(ExecutionStatus.RUNNING);
        jobExecutionDTO.setJob(jobDTO);
    }

    @Test
    void testUpdateJobStatusWhenJobExists() {
        // Setup
        when(jobExecutionRepository.findById(jobExecutionId)).thenReturn(Optional.of(jobExecutionEntity));

        // Execute
        jobDataService.updateJobStatus(jobExecutionId, ExecutionStatus.SUCCESS, "Job completed successfully");

        // Verify
        verify(jobExecutionRepository).findById(jobExecutionId);
        verify(jobExecutionRepository).save(jobExecutionCaptor.capture());

        JobExecutionEntity savedEntity = jobExecutionCaptor.getValue();
        assertEquals(ExecutionStatus.SUCCESS, savedEntity.getStatus());
        assertNotNull(savedEntity.getCompletionTime());
    }

    @Test
    void testUpdateJobStatusWhenJobDoesNotExist() {
        // Setup
        when(jobExecutionRepository.findById(jobExecutionId)).thenReturn(Optional.empty());

        // Execute
        jobDataService.updateJobStatus(jobExecutionId, ExecutionStatus.SUCCESS, "Job completed successfully");

        // Verify
        verify(jobExecutionRepository).findById(jobExecutionId);
        verify(jobExecutionRepository, never()).save(any());
    }

    @Test
    void testUpdateJobStatusWithNonTerminalStatus() {
        // Setup
        when(jobExecutionRepository.findById(jobExecutionId)).thenReturn(Optional.of(jobExecutionEntity));

        // Execute
        jobDataService.updateJobStatus(jobExecutionId, ExecutionStatus.RUNNING, "Job is running");

        // Verify
        verify(jobExecutionRepository).save(jobExecutionCaptor.capture());

        JobExecutionEntity savedEntity = jobExecutionCaptor.getValue();
        assertEquals(ExecutionStatus.RUNNING, savedEntity.getStatus());
        assertNull(savedEntity.getCompletionTime());
    }

    @Test
    void testGetJobExecutionByIdWhenJobExists() {
        // Setup
        when(jobExecutionRepository.findById(jobExecutionId)).thenReturn(Optional.of(jobExecutionEntity));
        when(jobRepository.findById(jobEntity.getId())).thenReturn(Optional.of(jobEntity));
        when(mapper.toJobExecutionDto(jobExecutionEntity, jobEntity)).thenReturn(jobExecutionDTO);

        // Execute
        Optional<JobExecutionDTO> result = jobDataService.getJobExecutionById(jobExecutionId);

        // Verify
        assertTrue(result.isPresent());
        assertEquals(jobExecutionDTO, result.get());
        verify(jobExecutionRepository).findById(jobExecutionId);
        verify(jobRepository).findById(jobEntity.getId());
        verify(mapper).toJobExecutionDto(jobExecutionEntity, jobEntity);
    }

    @Test
    void testGetJobExecutionByIdWhenJobDoesNotExist() {
        // Setup
        when(jobExecutionRepository.findById(jobExecutionId)).thenReturn(Optional.empty());

        // Execute
        Optional<JobExecutionDTO> result = jobDataService.getJobExecutionById(jobExecutionId);

        // Verify
        assertFalse(result.isPresent());
        verify(jobExecutionRepository).findById(jobExecutionId);
        verify(jobRepository, never()).findById(any());
        verify(mapper, never()).toJobExecutionDto(any(), any());
    }

    @Test
    void testGetJobExecutionByIdWhenJobEntityDoesNotExist() {
        // Setup
        when(jobExecutionRepository.findById(jobExecutionId)).thenReturn(Optional.of(jobExecutionEntity));
        when(jobRepository.findById(jobEntity.getId())).thenReturn(Optional.empty());
        when(mapper.toJobExecutionDto(eq(jobExecutionEntity), isNull())).thenReturn(jobExecutionDTO);

        // Execute
        Optional<JobExecutionDTO> result = jobDataService.getJobExecutionById(jobExecutionId);

        // Verify
        assertTrue(result.isPresent());
        assertEquals(jobExecutionDTO, result.get());
        verify(jobExecutionRepository).findById(jobExecutionId);
        verify(jobRepository).findById(jobEntity.getId());
        verify(mapper).toJobExecutionDto(eq(jobExecutionEntity), isNull());
    }
}