package edu.neu.cs6510.sp25.t1.worker.utils;

import edu.neu.cs6510.sp25.t1.backend.database.entity.PipelineEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.PipelineExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.StageExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.database.repository.PipelineExecutionRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.PipelineRepository;
import edu.neu.cs6510.sp25.t1.backend.database.repository.StageExecutionRepository;
import edu.neu.cs6510.sp25.t1.common.dto.JobExecutionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FindPipelineBranchTest {

    @Mock
    private StageExecutionRepository stageExecutionRepository;

    @Mock
    private PipelineExecutionRepository pipelineExecutionRepository;

    @Mock
    private PipelineRepository pipelineRepository;

    @InjectMocks
    private FindPipelineBranch findPipelineBranch;

    private UUID stageExecutionId;
    private UUID pipelineExecutionId;
    private UUID pipelineId;
    private JobExecutionDTO jobExecutionDTO;
    private StageExecutionEntity stageExecution;
    private PipelineExecutionEntity pipelineExecution;
    private PipelineEntity pipeline;

    @BeforeEach
    void setUp() {
        // Initialize test data
        stageExecutionId = UUID.randomUUID();
        pipelineExecutionId = UUID.randomUUID();
        pipelineId = UUID.randomUUID();

        // Set up JobExecutionDTO
        jobExecutionDTO = new JobExecutionDTO();
        jobExecutionDTO.setStageExecutionId(stageExecutionId);

        // Set up StageExecutionEntity
        stageExecution = new StageExecutionEntity();
        stageExecution.setPipelineExecutionId(pipelineExecutionId);

        // Set up PipelineExecutionEntity
        pipelineExecution = new PipelineExecutionEntity();
        pipelineExecution.setPipelineId(pipelineId);

        // Set up PipelineEntity
        pipeline = new PipelineEntity();
        pipeline.setBranch("feature/test-branch");
    }

    @Test
    void testGetBranch_Success() {
        // Arrange
        when(stageExecutionRepository.findById(stageExecutionId)).thenReturn(Optional.of(stageExecution));
        when(pipelineExecutionRepository.findById(pipelineExecutionId)).thenReturn(Optional.of(pipelineExecution));
        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));

        // Act
        String result = findPipelineBranch.getBranch(jobExecutionDTO);

        // Assert
        assertEquals("feature/test-branch", result);
        verify(stageExecutionRepository, times(1)).findById(stageExecutionId);
        verify(pipelineExecutionRepository, times(1)).findById(pipelineExecutionId);
        verify(pipelineRepository, times(1)).findById(pipelineId);
    }

    @Test
    void testGetBranch_StageExecutionNotFound() {
        // Arrange
        when(stageExecutionRepository.findById(stageExecutionId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            findPipelineBranch.getBranch(jobExecutionDTO);
        });
        assertEquals("StageExecution not found", exception.getMessage());

        verify(stageExecutionRepository, times(1)).findById(stageExecutionId);
        verify(pipelineExecutionRepository, never()).findById(any());
        verify(pipelineRepository, never()).findById(any());
    }

    @Test
    void testGetBranch_PipelineExecutionNotFound() {
        // Arrange
        when(stageExecutionRepository.findById(stageExecutionId)).thenReturn(Optional.of(stageExecution));
        when(pipelineExecutionRepository.findById(pipelineExecutionId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            findPipelineBranch.getBranch(jobExecutionDTO);
        });
        assertEquals("PipelineExecution not found", exception.getMessage());

        verify(stageExecutionRepository, times(1)).findById(stageExecutionId);
        verify(pipelineExecutionRepository, times(1)).findById(pipelineExecutionId);
        verify(pipelineRepository, never()).findById(any());
    }

    @Test
    void testGetBranch_PipelineNotFound() {
        // Arrange
        when(stageExecutionRepository.findById(stageExecutionId)).thenReturn(Optional.of(stageExecution));
        when(pipelineExecutionRepository.findById(pipelineExecutionId)).thenReturn(Optional.of(pipelineExecution));
        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            findPipelineBranch.getBranch(jobExecutionDTO);
        });
        assertEquals("Pipeline not found", exception.getMessage());

        verify(stageExecutionRepository, times(1)).findById(stageExecutionId);
        verify(pipelineExecutionRepository, times(1)).findById(pipelineExecutionId);
        verify(pipelineRepository, times(1)).findById(pipelineId);
    }

    @Test
    void testGetBranch_NullBranch() {
        // Arrange
        when(stageExecutionRepository.findById(stageExecutionId)).thenReturn(Optional.of(stageExecution));
        when(pipelineExecutionRepository.findById(pipelineExecutionId)).thenReturn(Optional.of(pipelineExecution));
        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));

        // Set branch to null
        pipeline.setBranch(null);

        // Act
        String result = findPipelineBranch.getBranch(jobExecutionDTO);

        // Assert
        assertNull(result);
        verify(stageExecutionRepository, times(1)).findById(stageExecutionId);
        verify(pipelineExecutionRepository, times(1)).findById(pipelineExecutionId);
        verify(pipelineRepository, times(1)).findById(pipelineId);
    }

    @Test
    void testGetBranch_EmptyBranch() {
        // Arrange
        when(stageExecutionRepository.findById(stageExecutionId)).thenReturn(Optional.of(stageExecution));
        when(pipelineExecutionRepository.findById(pipelineExecutionId)).thenReturn(Optional.of(pipelineExecution));
        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));

        // Set branch to empty string
        pipeline.setBranch("");

        // Act
        String result = findPipelineBranch.getBranch(jobExecutionDTO);

        // Assert
        assertEquals("", result);
        verify(stageExecutionRepository, times(1)).findById(stageExecutionId);
        verify(pipelineExecutionRepository, times(1)).findById(pipelineExecutionId);
        verify(pipelineRepository, times(1)).findById(pipelineId);
    }
}