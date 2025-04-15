package edu.neu.cs6510.sp25.t1.backend.database.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineEntityTest {

  @Test
  void testBuilderAndFieldAccess() {
    String name = "my-pipeline";
    String repoUrl = "https://github.com/user/repo";
    String commitHash = "abc123def456";

    PipelineEntity pipeline = PipelineEntity.builder()
        .name(name)
        .repositoryUrl(repoUrl)
        .commitHash(commitHash)
        .build();

    assertEquals(name, pipeline.getName());
    assertEquals(repoUrl, pipeline.getRepositoryUrl());
    assertEquals(commitHash, pipeline.getCommitHash());

    // Default branch value
    assertEquals("main", pipeline.getBranch());

    // Timestamp fields should be null before persist
    assertNull(pipeline.getCreatedAt());
    assertNull(pipeline.getUpdatedAt());
  }

//  @Test
//  void testOnCreateSetsTimestamps() {
//    PipelineEntity pipeline = new PipelineEntity();
//    assertNull(pipeline.getCreatedAt());
//    assertNull(pipeline.getUpdatedAt());
//
//    pipeline.onCreate();
//
//    assertNotNull(pipeline.getCreatedAt());
//    assertNotNull(pipeline.getUpdatedAt());
//    assertEquals(pipeline.getCreatedAt(), pipeline.getUpdatedAt());
//  }

  @Test
  void testOnUpdateOnlyUpdatesUpdatedAt() throws InterruptedException {
    PipelineEntity pipeline = new PipelineEntity();
    pipeline.onCreate();

    Instant created = pipeline.getCreatedAt();
    Instant updatedBefore = pipeline.getUpdatedAt();

    Thread.sleep(10); // force time gap
    pipeline.onUpdate();

    assertEquals(created, pipeline.getCreatedAt());
    assertTrue(pipeline.getUpdatedAt().isAfter(updatedBefore));
  }

  @Test
  void testSettersAndGetters() {
    PipelineEntity pipeline = new PipelineEntity();

    UUID id = UUID.randomUUID();
    pipeline.setId(id);
    pipeline.setName("test-pipe");
    pipeline.setRepositoryUrl("git@example.com:test/repo.git");
    pipeline.setBranch("dev");
    pipeline.setCommitHash("f00ba7");

    assertEquals(id, pipeline.getId());
    assertEquals("test-pipe", pipeline.getName());
    assertEquals("git@example.com:test/repo.git", pipeline.getRepositoryUrl());
    assertEquals("dev", pipeline.getBranch());
    assertEquals("f00ba7", pipeline.getCommitHash());
  }
}
