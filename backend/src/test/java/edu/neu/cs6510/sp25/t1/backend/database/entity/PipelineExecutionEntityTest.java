package edu.neu.cs6510.sp25.t1.backend.database.entity;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PipelineExecutionEntityTest {

  @Test
  void testBuilderAndGetters() {
    UUID pipelineId = UUID.randomUUID();
    String commitHash = "abc123def456";
    boolean isLocal = true;
    int runNumber = 7;
    ExecutionStatus status = ExecutionStatus.RUNNING;
    Instant start = Instant.now();
    Instant complete = Instant.now();

    PipelineExecutionEntity entity = PipelineExecutionEntity.builder()
        .pipelineId(pipelineId)
        .runNumber(runNumber)
        .commitHash(commitHash)
        .isLocal(isLocal)
        .status(status)
        .startTime(start)
        .completionTime(complete)
        .build();

    assertEquals(pipelineId, entity.getPipelineId());
    assertEquals(runNumber, entity.getRunNumber());
    assertEquals(commitHash, entity.getCommitHash());
    assertEquals(isLocal, entity.isLocal());
    assertEquals(status, entity.getStatus());
    assertEquals(start, entity.getStartTime());
    assertEquals(complete, entity.getCompletionTime());
  }

  @Test
  void testOnCreateInitializesStartTimeAndStatus() {
    PipelineExecutionEntity entity = new PipelineExecutionEntity();
    assertNull(entity.getStartTime());
    assertNull(entity.getStatus());

    entity.onCreate();

    assertNotNull(entity.getStartTime());
    assertEquals(ExecutionStatus.PENDING, entity.getStatus());
  }

  @Test
  void testUpdateStateSetsCompletionTimeForTerminalStates() throws InterruptedException {
    PipelineExecutionEntity entity = new PipelineExecutionEntity();

    for (ExecutionStatus terminal : List.of(
        ExecutionStatus.SUCCESS,
        ExecutionStatus.FAILED,
        ExecutionStatus.CANCELED
    )) {
      entity.setCompletionTime(null); // reset
      entity.updateState(terminal);
      assertEquals(terminal, entity.getStatus());
      assertNotNull(entity.getCompletionTime());
    }
  }

  @Test
  void testUpdateStateDoesNotSetCompletionTimeForNonTerminal() {
    PipelineExecutionEntity entity = new PipelineExecutionEntity();

    entity.updateState(ExecutionStatus.RUNNING);
    assertEquals(ExecutionStatus.RUNNING, entity.getStatus());
    assertNull(entity.getCompletionTime());

    entity.updateState(ExecutionStatus.PENDING);
    assertEquals(ExecutionStatus.PENDING, entity.getStatus());
    assertNull(entity.getCompletionTime());
  }

  @Test
  void testSettersAndId() {
    UUID id = UUID.randomUUID();
    PipelineExecutionEntity entity = new PipelineExecutionEntity();
    entity.setId(id);
    assertEquals(id, entity.getId());
  }
}
