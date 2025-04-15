package edu.neu.cs6510.sp25.t1.backend.info;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;



class ClonedPipelineInfoTest {

  @Test
  void testClonedPipelineInfoInitialization() {
    String expectedPath = "/mnt/pipeline/uuid/.pipelines/test.yaml";
    UUID expectedUuid = UUID.randomUUID();

    ClonedPipelineInfo info = new ClonedPipelineInfo(expectedPath, expectedUuid);

    assertEquals(expectedPath, info.getYamlPath());
    assertEquals(expectedUuid, info.getUuid());
  }
}
