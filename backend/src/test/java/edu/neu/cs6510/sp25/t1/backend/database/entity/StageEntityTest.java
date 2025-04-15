package edu.neu.cs6510.sp25.t1.backend.database.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StageEntityTest {

  @Test
  void testBuilderAndFieldAccess() {
    UUID pipelineId = UUID.randomUUID();
    String name = "build-stage";
    int order = 2;

    StageEntity stage = StageEntity.builder()
        .pipelineId(pipelineId)
        .name(name)
        .executionOrder(order)
        .build();

    assertEquals(pipelineId, stage.getPipelineId());
    assertEquals(name, stage.getName());
    assertEquals(order, stage.getExecutionOrder());

    // Timestamps should be null before persistence
    assertNull(stage.getCreatedAt());
    assertNull(stage.getUpdatedAt());
  }


  @Test
  void testOnUpdateChangesOnlyUpdatedAt() throws InterruptedException {
    StageEntity stage = new StageEntity();
    stage.onCreate(); // set initial timestamps

    Instant created = stage.getCreatedAt();
    Instant updatedBefore = stage.getUpdatedAt();

    Thread.sleep(10); // simulate time passing
    stage.onUpdate();

    assertEquals(created, stage.getCreatedAt()); // createdAt must not change
    assertTrue(stage.getUpdatedAt().isAfter(updatedBefore));
  }

  @Test
  void testSettersAndId() {
    UUID id = UUID.randomUUID();
    StageEntity stage = new StageEntity();
    stage.setId(id);
    stage.setName("test-stage");
    stage.setExecutionOrder(1);

    assertEquals(id, stage.getId());
    assertEquals("test-stage", stage.getName());
    assertEquals(1, stage.getExecutionOrder());
  }
}
