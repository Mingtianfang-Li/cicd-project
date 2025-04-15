package edu.neu.cs6510.sp25.t1.common.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StageExecutionDTOTest {

    @Test
    public void testBuilder() {
        UUID id = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID pipelineExecutionId = UUID.randomUUID();
        Instant start = Instant.now();
        Instant end = start.plusSeconds(120);

        StageExecutionDTO dto = StageExecutionDTO.builder()
                .id(id)
                .stageId(stageId)
                .pipelineExecutionId(pipelineExecutionId)
                .executionOrder(1)
                .commitHash("hash123")
                .isLocal(true)
                .status(ExecutionStatus.SUCCESS)
                .startTime(start)
                .completionTime(end)
                .build();

        assertEquals(id, dto.getId());
        assertEquals(stageId, dto.getStageId());
        assertEquals(pipelineExecutionId, dto.getPipelineExecutionId());
        assertEquals(1, dto.getExecutionOrder());
        assertEquals("hash123", dto.getCommitHash());
        assertTrue(dto.isLocal());
        assertEquals(ExecutionStatus.SUCCESS, dto.getStatus());
        assertEquals(start, dto.getStartTime());
        assertEquals(end, dto.getCompletionTime());
    }

    @Test
    public void testSettersAndGetters() {
        StageExecutionDTO dto = new StageExecutionDTO();
        UUID id = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID pipelineId = UUID.randomUUID();
        Instant start = Instant.now();
        Instant end = start.plusSeconds(60);

        dto.setId(id);
        dto.setStageId(stageId);
        dto.setPipelineExecutionId(pipelineId);
        dto.setExecutionOrder(5);
        dto.setCommitHash("abc456");
        dto.setLocal(false);
        dto.setStatus(ExecutionStatus.FAILED);
        dto.setStartTime(start);
        dto.setCompletionTime(end);

        assertEquals(id, dto.getId());
        assertEquals(stageId, dto.getStageId());
        assertEquals(pipelineId, dto.getPipelineExecutionId());
        assertEquals(5, dto.getExecutionOrder());
        assertEquals("abc456", dto.getCommitHash());
        assertFalse(dto.isLocal());
        assertEquals(ExecutionStatus.FAILED, dto.getStatus());
        assertEquals(start, dto.getStartTime());
        assertEquals(end, dto.getCompletionTime());
    }
}
