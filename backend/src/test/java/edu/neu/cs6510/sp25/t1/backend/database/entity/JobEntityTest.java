package edu.neu.cs6510.sp25.t1.backend.database.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobEntityTest {

  @Test
  void testBuilderAndDefaults() {
    UUID stageId = UUID.randomUUID();
    List<String> scripts = List.of("echo 'Hello'", "echo 'World'");
    List<UUID> dependencies = List.of(UUID.randomUUID());
    List<String> artifacts = List.of("build/output.txt");

    JobEntity job = JobEntity.builder()
        .stageId(stageId)
        .name("compile-job")
        .workingDir("/app")
        .script(scripts)
        .dependencies(dependencies)
        .artifacts(artifacts)
        .build();

    assertEquals(stageId, job.getStageId());
    assertEquals("compile-job", job.getName());
    assertEquals("/app", job.getWorkingDir());
    assertEquals(scripts, job.getScript());
    assertEquals(dependencies, job.getDependencies());
    assertEquals(artifacts, job.getArtifacts());

    // Defaults
    assertEquals("docker.io/library/alpine:latest", job.getDockerImage());
    assertFalse(job.isAllowFailure());
    assertNull(job.getCreatedAt());
    assertNull(job.getUpdatedAt());
  }

//  @Test
//  void testPrePersistSetsTimestamps() {
//    JobEntity job = new JobEntity();
//    assertNull(job.getCreatedAt());
//    assertNull(job.getUpdatedAt());
//
//    job.onCreate();
//
//    assertNotNull(job.getCreatedAt());
//    assertNotNull(job.getUpdatedAt());
//    assertEquals(job.getCreatedAt(), job.getUpdatedAt());
//  }

  @Test
  void testPreUpdateSetsUpdatedAtOnly() throws InterruptedException {
    JobEntity job = new JobEntity();
    job.onCreate();

    Instant createdAtBefore = job.getCreatedAt();
    Instant updatedAtBefore = job.getUpdatedAt();

    Thread.sleep(10); // ensure time difference
    job.onUpdate();

    assertEquals(createdAtBefore, job.getCreatedAt());
    assertTrue(job.getUpdatedAt().isAfter(updatedAtBefore));
  }
}
