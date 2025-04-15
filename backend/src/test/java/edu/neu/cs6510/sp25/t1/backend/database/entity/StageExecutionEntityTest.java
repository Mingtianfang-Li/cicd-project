package edu.neu.cs6510.sp25.t1.backend.database.entity;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;



class StageExecutionEntityTest {

  @Test
  void testBuilderAndFieldAssignment() {
    UUID stageId = UUID.randomUUID();
    UUID pipelineExecutionId = UUID.randomUUID();
    String commitHash = "abc123";
    boolean isLocal = true;
    int executionOrder = 2;
    ExecutionStatus status = ExecutionStatus.RUNNING;
    Instant startTime = Instant.now();
    Instant completionTime = Instant.now();

    JobExecutionEntity job1 = new JobExecutionEntity();
    JobExecutionEntity job2 = new JobExecutionEntity();

    StageExecutionEntity entity = StageExecutionEntity.builder()
        .stageId(stageId)
        .pipelineExecutionId(pipelineExecutionId)
        .commitHash(commitHash)
        .isLocal(isLocal)
        .executionOrder(executionOrder)
        .status(status)
        .startTime(startTime)
        .completionTime(completionTime)
        .jobs(List.of(job1, job2))
        .build();

    assertEquals(stageId, entity.getStageId());
    assertEquals(pipelineExecutionId, entity.getPipelineExecutionId());
    assertEquals(commitHash, entity.getCommitHash());
    assertEquals(isLocal, entity.isLocal());
    assertEquals(status, entity.getStatus());
    assertEquals(executionOrder, entity.getExecutionOrder());
    assertEquals(startTime, entity.getStartTime());
    assertEquals(completionTime, entity.getCompletionTime());
    assertEquals(2, entity.getJobs().size());
  }

  @Test
  void testOnCreateInitializesTimestampsAndStatus() {
    StageExecutionEntity entity = new StageExecutionEntity();

    assertNull(entity.getStartTime());
    assertNull(entity.getStatus());

    entity.onCreate();

    assertNotNull(entity.getStartTime());
    assertEquals(ExecutionStatus.PENDING, entity.getStatus());
  }

  @Test
  void testUpdateStateSetsCompletionTimeForTerminalStates() {
    StageExecutionEntity entity = new StageExecutionEntity();

    for (ExecutionStatus terminal : List.of(
        ExecutionStatus.SUCCESS,
        ExecutionStatus.FAILED,
        ExecutionStatus.CANCELED
    )) {
      entity.setCompletionTime(null);
      entity.updateState(terminal);

      assertEquals(terminal, entity.getStatus());
      assertNotNull(entity.getCompletionTime());
    }
  }

  @Test
  void testUpdateStateDoesNotSetCompletionTimeForNonTerminalStates() {
    StageExecutionEntity entity = new StageExecutionEntity();

    entity.updateState(ExecutionStatus.RUNNING);
    assertEquals(ExecutionStatus.RUNNING, entity.getStatus());
    assertNull(entity.getCompletionTime());

    entity.updateState(ExecutionStatus.PENDING);
    assertEquals(ExecutionStatus.PENDING, entity.getStatus());
    assertNull(entity.getCompletionTime());
  }

  @Test
  void testSettersAndGetters() {
    StageExecutionEntity entity = new StageExecutionEntity();
    UUID id = UUID.randomUUID();
    UUID stageId = UUID.randomUUID();
    UUID pipelineId = UUID.randomUUID();

    entity.setId(id);
    entity.setStageId(stageId);
    entity.setPipelineExecutionId(pipelineId);
    entity.setExecutionOrder(3);
    entity.setCommitHash("commit123");
    entity.setLocal(true);

    assertEquals(id, entity.getId());
    assertEquals(stageId, entity.getStageId());
    assertEquals(pipelineId, entity.getPipelineExecutionId());
    assertEquals(3, entity.getExecutionOrder());
    assertEquals("commit123", entity.getCommitHash());
    assertTrue(entity.isLocal());
  }
}
