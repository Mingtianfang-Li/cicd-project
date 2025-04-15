package edu.neu.cs6510.sp25.t1.common.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.common.dto.JobReportDTO.ExecutionRecord;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JobReportDTOTest {

    @Test
    public void testConstructorAndGetters() {
        UUID id = UUID.randomUUID();
        Instant start = Instant.now();
        Instant end = start.plusSeconds(120);
        ExecutionRecord record = new ExecutionRecord(id, ExecutionStatus.SUCCESS, start, end, false);
        JobReportDTO jobReport = new JobReportDTO("BuildJob", Collections.singletonList(record));

        assertEquals("BuildJob", jobReport.getName());
        assertEquals(1, jobReport.getExecutions().size());
        assertEquals(record, jobReport.getExecutions().get(0));
    }

    @Test
    public void testSettersAndAdditionalFields() {
        JobReportDTO jobReport = new JobReportDTO("TestJob", Collections.emptyList());
        jobReport.setPipelineName("MainPipeline");
        jobReport.setRunNumber(42);
        jobReport.setCommitHash("abc123");
        jobReport.setStageName("TestStage");

        assertEquals("MainPipeline", jobReport.getPipelineName());
        assertEquals(42, jobReport.getRunNumber());
        assertEquals("abc123", jobReport.getCommitHash());
        assertEquals("TestStage", jobReport.getStageName());
    }

    @Test
    public void testExecutionRecordSetters() {
        ExecutionRecord record = new ExecutionRecord();
        UUID id = UUID.randomUUID();
        Instant start = Instant.now();
        Instant end = start.plusSeconds(60);

        record.setId(id);
        record.setStatus(ExecutionStatus.FAILED);
        record.setStartTime(start);
        record.setCompletionTime(end);
        record.setAllowFailure(true);

        assertEquals(id, record.getId());
        assertEquals(ExecutionStatus.FAILED, record.getStatus());
        assertEquals(start, record.getStartTime());
        assertEquals(end, record.getCompletionTime());
        assertTrue(record.isAllowFailure());
    }
}
