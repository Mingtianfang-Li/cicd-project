package edu.neu.cs6510.sp25.t1.common.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StageExecutionTest {

  private StageExecution stageExecution;
  private UUID id;
  private Stage stage;
  private List<JobExecution> jobs;
  private String commitHash;

  @BeforeEach
  void setUp() {
    id = UUID.fromString("c3d1d7e0-4a3b-11ec-81d3-0242ac130003"); // Fixed UUID
    commitHash = "abcd1234";
    boolean isLocal = true;

    // Create a mock Stage object
    UUID stageId = UUID.fromString("3b6b73ec-2d46-11ec-8d3d-0242ac130003");
    String stageName = "Build Stage";
    UUID pipelineId = UUID.randomUUID();
    int executionOrder = 1;
    List<Job> jobList = List.of(); // Empty job list
    LocalDateTime createdAt = LocalDateTime.now().minusDays(2);
    LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);
    stage = new Stage(stageId, stageName, pipelineId, executionOrder, jobList, createdAt, updatedAt);

    // Create mock JobExecution objects
    JobExecution job1 = new JobExecution(UUID.randomUUID(), UUID.randomUUID(), "commit1", true, "docker-image", List.of(), List.of(), false);
    JobExecution job2 = new JobExecution(UUID.randomUUID(), UUID.randomUUID(), "commit2", true, "docker-image", List.of(), List.of(), false);
    jobs = List.of(job1, job2);

    stageExecution = new StageExecution(id, stage, jobs, commitHash, isLocal);
  }

  @Test
  void testConstructor() {
    assertNotNull(stageExecution.getId());
    assertEquals(id, stageExecution.getId());
    assertEquals(stage, stageExecution.getStage());
    assertEquals(jobs, stageExecution.getJobs());
    assertEquals(commitHash, stageExecution.getCommitHash());
    assertTrue(stageExecution.isLocal());
    assertEquals(ExecutionStatus.PENDING, stageExecution.getStatus());
    assertNotNull(stageExecution.getStartTime());
    assertNull(stageExecution.getCompletionTime());
  }

  @Test
  void testUpdateStatusToSuccess() {
    jobs.forEach(job -> job.updateState(ExecutionStatus.SUCCESS));
    stageExecution.updateStatus();

    assertEquals(ExecutionStatus.SUCCESS, stageExecution.getStatus());
    assertNotNull(stageExecution.getCompletionTime());
  }

  @Test
  void testUpdateStatusToFailed() {
    jobs.getFirst().updateState(ExecutionStatus.FAILED);
    stageExecution.updateStatus();

    assertEquals(ExecutionStatus.FAILED, stageExecution.getStatus());
    assertNotNull(stageExecution.getCompletionTime());
  }

  @Test
  void testUpdateStatusToCanceled() {
    jobs.getFirst().updateState(ExecutionStatus.CANCELED);
    stageExecution.updateStatus();

    assertEquals(ExecutionStatus.CANCELED, stageExecution.getStatus());
    assertNotNull(stageExecution.getCompletionTime());
  }

  @Test
  void testUpdateStatusHandlesMixedStatuses() {
    jobs.get(0).updateState(ExecutionStatus.SUCCESS);
    jobs.get(1).updateState(ExecutionStatus.CANCELED);
    stageExecution.updateStatus();

    assertEquals(ExecutionStatus.CANCELED, stageExecution.getStatus());
    assertNotNull(stageExecution.getCompletionTime());
  }

  @Test
  void testSetStatus() {
    stageExecution.setStatus(ExecutionStatus.FAILED);
    assertEquals(ExecutionStatus.FAILED, stageExecution.getStatus());
  }

  @Test
  void testCompletionTimeUpdatesOnlyOnce() {
    stageExecution.setStatus(ExecutionStatus.SUCCESS);
    Instant firstCompletionTime = stageExecution.getCompletionTime();

    stageExecution.setStatus(ExecutionStatus.FAILED);
    assertEquals(firstCompletionTime, stageExecution.getCompletionTime());
  }
}
