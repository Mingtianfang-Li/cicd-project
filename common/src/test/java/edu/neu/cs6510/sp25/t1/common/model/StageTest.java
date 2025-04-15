package edu.neu.cs6510.sp25.t1.common.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StageTest {

  private Stage stage;
  private UUID id;
  private String name;
  private UUID pipelineId;
  private int executionOrder;
  private List<Job> jobs;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @BeforeEach
  void setUp() {
    id = UUID.fromString("c3d1d7e0-4a3b-11ec-81d3-0242ac130003"); // Fixed UUID
    name = "Build Stage";
    pipelineId = UUID.fromString("2b6d73ec-2d46-11ec-8d3d-0242ac130003");
    executionOrder = 1;
    jobs = List.of(); // Empty job list
    createdAt = LocalDateTime.now().minusDays(2);
    updatedAt = LocalDateTime.now().minusDays(1);

    stage = new Stage(id, name, pipelineId, executionOrder, jobs, createdAt, updatedAt);
  }

  @Test
  void testConstructor() {
    assertNotNull(stage.getId());
    assertEquals(id, stage.getId());
    assertEquals(name, stage.getName());
    assertEquals(pipelineId, stage.getPipelineId());
    assertEquals(executionOrder, stage.getExecutionOrder());
    assertEquals(jobs, stage.getJobs());
    assertEquals(createdAt, stage.getCreatedAt());
    assertEquals(updatedAt, stage.getUpdatedAt());
  }

  @Test
  void testJobsDefaultsToEmptyList() {
    Stage stageWithNullJobs = new Stage(id, name, pipelineId, executionOrder, null, createdAt, updatedAt);
    assertNotNull(stageWithNullJobs.getJobs());
    assertTrue(stageWithNullJobs.getJobs().isEmpty());
  }

  @Test
  void testGetters() {
    assertEquals(id, stage.getId());
    assertEquals(name, stage.getName());
    assertEquals(pipelineId, stage.getPipelineId());
    assertEquals(executionOrder, stage.getExecutionOrder());
    assertEquals(jobs, stage.getJobs());
    assertEquals(createdAt, stage.getCreatedAt());
    assertEquals(updatedAt, stage.getUpdatedAt());
  }
}
