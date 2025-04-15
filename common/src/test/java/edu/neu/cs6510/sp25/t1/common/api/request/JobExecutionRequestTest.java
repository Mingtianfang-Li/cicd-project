package edu.neu.cs6510.sp25.t1.common.api.request;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobExecutionRequestTest {

  @Test
  void testConstructorAndGetters() {
    // Arrange
    UUID jobId = UUID.randomUUID();
    UUID stageExecutionId = UUID.randomUUID();
    String commitHash = "abc123";
    boolean isLocal = true;
    String dockerImage = "ubuntu:latest";
    List<String> commands = Arrays.asList("echo 'Hello'", "ls -la");
    String jobName = "TestJob";

    // Act
    JobExecutionRequest request = new JobExecutionRequest(
            jobId,
            stageExecutionId,
            commitHash,
            isLocal,
            dockerImage,
            commands,
            jobName
    );

    // Assert
    assertEquals(jobId, request.getJobId());
    assertEquals(stageExecutionId, request.getStageExecutionId());
    assertEquals(commitHash, request.getCommitHash());
    assertTrue(request.isLocal());
    assertEquals(dockerImage, request.getDockerImage());
    assertEquals(commands, request.getCommands());
    assertEquals(jobName, request.getJobName());
  }

  @Test
  void testIsLocalMethod() {
    // Arrange
    JobExecutionRequest localRequest = new JobExecutionRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "abc123",
            true,
            "ubuntu:latest",
            Arrays.asList("echo 'Hello'"),
            "LocalJob"
    );

    JobExecutionRequest remoteRequest = new JobExecutionRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "def456",
            false,
            "ubuntu:latest",
            Arrays.asList("echo 'Hello'"),
            "RemoteJob"
    );

    // Act & Assert
    assertTrue(localRequest.isLocal());
    assertFalse(remoteRequest.isLocal());
  }

  @Test
  void testNullParameters() {
    // Arrange
    UUID jobId = UUID.randomUUID();
    UUID stageExecutionId = UUID.randomUUID();

    // Act
    JobExecutionRequest request = new JobExecutionRequest(
            jobId,
            stageExecutionId,
            null, // null commitHash
            false,
            null, // null dockerImage
            null, // null commands
            null  // null jobName
    );

    // Assert
    assertEquals(jobId, request.getJobId());
    assertEquals(stageExecutionId, request.getStageExecutionId());
    assertNull(request.getCommitHash());
    assertFalse(request.isLocal());
    assertNull(request.getDockerImage());
    assertNull(request.getCommands());
    assertNull(request.getJobName());
  }

  @Test
  void testEmptyCommands() {
    // Arrange
    UUID jobId = UUID.randomUUID();
    UUID stageExecutionId = UUID.randomUUID();
    List<String> emptyCommands = Arrays.asList();

    // Act
    JobExecutionRequest request = new JobExecutionRequest(
            jobId,
            stageExecutionId,
            "abc123",
            true,
            "ubuntu:latest",
            emptyCommands,
            "EmptyCommandsJob"
    );

    // Assert
    assertEquals(jobId, request.getJobId());
    assertEquals(stageExecutionId, request.getStageExecutionId());
    assertEquals("abc123", request.getCommitHash());
    assertTrue(request.isLocal());
    assertEquals("ubuntu:latest", request.getDockerImage());
    assertEquals(emptyCommands, request.getCommands());
    assertEquals("EmptyCommandsJob", request.getJobName());
    assertEquals(0, request.getCommands().size());
  }
}