package edu.neu.cs6510.sp25.t1.common.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobExecutionTest {

  private JobExecution jobExecution;
  private UUID stageExecutionId;
  private UUID jobId;
  private String commitHash;
  private boolean isLocal;
  private List<String> script;
  private List<String> dependencies;

  @BeforeEach
  void setUp() {
    stageExecutionId = UUID.randomUUID();
    jobId = UUID.randomUUID();
    commitHash = "abc123";
    isLocal = true;
    script = Arrays.asList("echo 'Hello'", "exit 0");
    dependencies = Arrays.asList("dep1", "dep2");
    boolean allowFailure = false;

    jobExecution = new JobExecution(stageExecutionId, jobId, commitHash, isLocal, "docker-image", script, dependencies, allowFailure);
  }

  @Test
  void testConstructor() {
    assertNotNull(jobExecution.getId());
    assertEquals(stageExecutionId, jobExecution.getStageExecutionId());
    assertEquals(jobId, jobExecution.getJobId());
    assertEquals(commitHash, jobExecution.getCommitHash());
    assertEquals(isLocal, jobExecution.isLocal());
    assertEquals(ExecutionStatus.PENDING, jobExecution.getStatus());
    assertEquals(script, jobExecution.getScript());
    assertEquals(dependencies, jobExecution.getDependencies());
    assertFalse(jobExecution.isAllowFailure());
    assertEquals("", jobExecution.getLogs());
  }

  @Test
  void testUpdateStateToSuccess() {
    jobExecution.updateState(ExecutionStatus.SUCCESS);
    assertEquals(ExecutionStatus.SUCCESS, jobExecution.getStatus());
    assertNotNull(jobExecution.getCompletionTime());
  }

  @Test
  void testUpdateStateToFailed() {
    jobExecution.updateState(ExecutionStatus.FAILED);
    assertEquals(ExecutionStatus.FAILED, jobExecution.getStatus());
    assertNotNull(jobExecution.getCompletionTime());
  }

  @Test
  void testUpdateStateToPendingDoesNotSetCompletionTime() {
    jobExecution.updateState(ExecutionStatus.PENDING);
    assertEquals(ExecutionStatus.PENDING, jobExecution.getStatus());
    assertNull(jobExecution.getCompletionTime());
  }

  @Test
  void testAppendLogs() {
    jobExecution.appendLogs("First log entry");
    jobExecution.appendLogs("Second log entry");

    assertEquals("First log entry\nSecond log entry\n", jobExecution.getLogs());
  }

  @Test
  void testTimestampsAreUpdated() {
    Instant beforeUpdate = jobExecution.getStartTime();
    jobExecution.updateState(ExecutionStatus.SUCCESS);

    assertNotNull(jobExecution.getCompletionTime());
    assertTrue(jobExecution.getCompletionTime().isAfter(beforeUpdate));
  }

  @Test
  void testUpdateStateToCanceled() {
    jobExecution.updateState(ExecutionStatus.CANCELED);
    assertEquals(ExecutionStatus.CANCELED, jobExecution.getStatus());
    assertNotNull(jobExecution.getCompletionTime());
  }
}
