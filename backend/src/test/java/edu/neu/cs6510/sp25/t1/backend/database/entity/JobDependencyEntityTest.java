package edu.neu.cs6510.sp25.t1.backend.database.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class JobDependencyEntityTest {

  @Test
  void testSettersAndGetters() {
    // Arrange
    UUID id = UUID.randomUUID();
    JobExecutionEntity job = new JobExecutionEntity();
    JobExecutionEntity dependency = new JobExecutionEntity();

    JobDependencyEntity entity = new JobDependencyEntity();

    // Act
    entity.setId(id);
    entity.setJob(job);
    entity.setDependency(dependency);

    // Assert
    assertEquals(id, entity.getId());
    assertSame(job, entity.getJob());
    assertSame(dependency, entity.getDependency());
  }

  @Test
  void testDefaultConstructor() {
    JobDependencyEntity entity = new JobDependencyEntity();
    assertNotNull(entity);
    assertNull(entity.getId());
    assertNull(entity.getJob());
    assertNull(entity.getDependency());
  }
}
