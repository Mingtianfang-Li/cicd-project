package edu.neu.cs6510.sp25.t1.common.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PipelineReportDTOTest {

    @Test
    public void testConstructorAndGetters() {
        UUID id = UUID.randomUUID();
        String name = "Pipeline Alpha";
        int runNumber = 1;
        String commitHash = "commit123";
        ExecutionStatus status = ExecutionStatus.SUCCESS;
        Instant start = Instant.now();
        Instant end = start.plusSeconds(300);

        PipelineReportDTO dto = new PipelineReportDTO(
                id, name, runNumber, commitHash, status, start, end, Collections.emptyList());

        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(runNumber, dto.getRunNumber());
        assertEquals(commitHash, dto.getCommitHash());
        assertEquals(status, dto.getStatus());
        assertEquals(start, dto.getStartTime());
        assertEquals(end, dto.getCompletionTime());
        assertNotNull(dto.getStages());
        assertTrue(dto.getStages().isEmpty());
    }

    @Test
    public void testSetters() {
        PipelineReportDTO dto = new PipelineReportDTO();
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        dto.setId(id);
        dto.setName("Pipeline Beta");
        dto.setRunNumber(2);
        dto.setCommitHash("abc456");
        dto.setStatus(ExecutionStatus.FAILED);
        dto.setStartTime(now);
        dto.setCompletionTime(now.plusSeconds(200));

        assertEquals(id, dto.getId());
        assertEquals("Pipeline Beta", dto.getName());
        assertEquals(2, dto.getRunNumber());
        assertEquals("abc456", dto.getCommitHash());
        assertEquals(ExecutionStatus.FAILED, dto.getStatus());
        assertEquals(now, dto.getStartTime());
        assertEquals(now.plusSeconds(200), dto.getCompletionTime());
    }

    @Test
    public void testSetPipelineNameAlsoSetsName() {
        PipelineReportDTO dto = new PipelineReportDTO();
        dto.setPipelineName("MainPipeline");

        assertEquals("MainPipeline", dto.getPipelineName());
        assertEquals("MainPipeline", dto.getName(), "setPipelineName should also set name");
    }
}
