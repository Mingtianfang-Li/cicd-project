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

class PipelineExecutionTest {

  private PipelineExecution pipelineExecution;
  private UUID id;
  private UUID pipelineId;
  private int runNumber;
  private String commitHash;
  private boolean isLocal;
  private List<StageExecution> stages;

  @BeforeEach
  void setUp() {
    id = UUID.fromString("d9766fb2-c3d0-4cc9-9871-a34c2a0e5f79"); // Fixed UUID
    pipelineId = UUID.fromString("2c3e1a94-4d5f-11ec-81d3-0242ac130003"); // Fixed UUID
    runNumber = 1;
    commitHash = "abcd1234";
    isLocal = true;

    // Create a valid Stage object
    UUID stageId = UUID.fromString("3b6b73ec-2d46-11ec-8d3d-0242ac130003");
    String stageName = "Build Stage";
    int executionOrder = 1;
    List<Job> jobs = List.of(); // Assuming Job class exists
    LocalDateTime createdAt = LocalDateTime.now().minusDays(2);
    LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);
    Stage stage = new Stage(stageId, stageName, pipelineId, executionOrder, jobs, createdAt, updatedAt);

    // Create a valid StageExecution object
    List<JobExecution> jobExecutions = List.of(); // Assuming JobExecution class exists
    String executionEnvironment = "Docker";
    boolean allowFailure = false;

    StageExecution stageExecution = new StageExecution(UUID.randomUUID(), stage, jobExecutions, executionEnvironment, allowFailure);
    stages = List.of(stageExecution);

    pipelineExecution = new PipelineExecution(id, pipelineId, runNumber, commitHash, isLocal, stages);
  }

  @Test
  void testConstructor() {
    assertEquals(UUID.fromString("d9766fb2-c3d0-4cc9-9871-a34c2a0e5f79"), pipelineExecution.getId());
    assertEquals(UUID.fromString("2c3e1a94-4d5f-11ec-81d3-0242ac130003"), pipelineExecution.getPipelineId());
    assertEquals(runNumber, pipelineExecution.getRunNumber());
    assertEquals(commitHash, pipelineExecution.getCommitHash());
    assertTrue(pipelineExecution.isLocal());
  }


  @Test
  void testDefaultIdGeneration() {
    PipelineExecution newPipelineExecution = new PipelineExecution(null, pipelineId, runNumber, commitHash, isLocal, stages);
    assertNotNull(newPipelineExecution.getId());
  }

  @Test
  void testStagesDefaultsToEmptyListWhenNull() {
    PipelineExecution noStagesPipelineExecution = new PipelineExecution(id, pipelineId, runNumber, commitHash, isLocal, null);
    assertNotNull(noStagesPipelineExecution.getStages());
    assertTrue(noStagesPipelineExecution.getStages().isEmpty());
  }

  @Test
  void testSetStatusToSuccess() {
    pipelineExecution.setStatus(ExecutionStatus.SUCCESS);
    assertEquals(ExecutionStatus.SUCCESS, pipelineExecution.getStatus());
    assertNotNull(pipelineExecution.getCompletionTime());
  }

  @Test
  void testSetStatusToFailed() {
    pipelineExecution.setStatus(ExecutionStatus.FAILED);
    assertEquals(ExecutionStatus.FAILED, pipelineExecution.getStatus());
    assertNotNull(pipelineExecution.getCompletionTime());
  }

  @Test
  void testSetStatusToPendingDoesNotSetCompletionTime() {
    pipelineExecution.setStatus(ExecutionStatus.PENDING);
    assertEquals(ExecutionStatus.PENDING, pipelineExecution.getStatus());
    assertNull(pipelineExecution.getCompletionTime());
  }

  @Test
  void testSetStatusDoesNotChangeCompletionTimeOnceSet() {
    pipelineExecution.setStatus(ExecutionStatus.SUCCESS);
    Instant firstCompletionTime = pipelineExecution.getCompletionTime();

    pipelineExecution.setStatus(ExecutionStatus.FAILED);
    Instant secondCompletionTime = pipelineExecution.getCompletionTime();

    // Allow minor differences (e.g., up to 1 millisecond)
    assertTrue(Math.abs(firstCompletionTime.toEpochMilli() - secondCompletionTime.toEpochMilli()) < 2,
            "Completion time should not change once set.");
  }

}
