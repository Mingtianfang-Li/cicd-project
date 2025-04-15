package edu.neu.cs6510.sp25.t1.backend.database.entity;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;



class JobExecutionEntityTest {

  @Test
  void testBuilderAndFieldAccess() {
    UUID jobId = UUID.randomUUID();
    String commitHash = "abc123def456";
    boolean isLocal = true;
    boolean allowFailure = true;
    ExecutionStatus status = ExecutionStatus.RUNNING;
    Instant startTime = Instant.now();

    StageExecutionEntity stage = new StageExecutionEntity(); // can be mocked if needed

    JobExecutionEntity entity = JobExecutionEntity.builder()
        .jobId(jobId)
        .commitHash(commitHash)
        .isLocal(isLocal)
        .allowFailure(allowFailure)
        .status(status)
        .startTime(startTime)
        .stageExecution(stage)
        .build();

    assertEquals(jobId, entity.getJobId());
    assertEquals(commitHash, entity.getCommitHash());
    assertEquals(isLocal, entity.isLocal());
    assertEquals(allowFailure, entity.isAllowFailure());
    assertEquals(status, entity.getStatus());
    assertEquals(startTime, entity.getStartTime());
    assertEquals(stage, entity.getStageExecution());
    assertNull(entity.getCompletionTime());
  }

  @Test
  void testUpdateState_setsCompletionTimeForTerminalStates() throws InterruptedException {
    JobExecutionEntity entity = new JobExecutionEntity();
    assertNull(entity.getCompletionTime());

    for (ExecutionStatus terminalStatus : List.of(
        ExecutionStatus.SUCCESS,
        ExecutionStatus.FAILED,
        ExecutionStatus.CANCELED
    )) {
      entity.setCompletionTime(null); // reset
      entity.updateState(terminalStatus);

      assertEquals(terminalStatus, entity.getStatus());
      assertNotNull(entity.getCompletionTime());
    }
  }

  @Test
  void testUpdateState_doesNotSetCompletionTimeForNonTerminalStatus() {
    JobExecutionEntity entity = new JobExecutionEntity();

    entity.updateState(ExecutionStatus.RUNNING);
    assertEquals(ExecutionStatus.RUNNING, entity.getStatus());
    assertNull(entity.getCompletionTime());

    entity.updateState(ExecutionStatus.PENDING);
    assertEquals(ExecutionStatus.PENDING, entity.getStatus());
    assertNull(entity.getCompletionTime());
  }
}
