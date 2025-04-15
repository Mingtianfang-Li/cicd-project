package edu.neu.cs6510.sp25.t1.backend.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.backend.database.entity.StageExecutionEntity;
import edu.neu.cs6510.sp25.t1.common.dto.StageExecutionDTO;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class StageExecutionMapperTest {

  private StageExecutionMapper stageExecutionMapper;

  private UUID testId;
  private UUID testStageId;
  private UUID testPipelineExecutionId;
  private int testExecutionOrder;
  private String testCommitHash;
  private boolean testIsLocal;
  private ExecutionStatus testStatus;
  private Instant testStartTime;
  private Instant testCompletionTime;

  @BeforeEach
  void setUp() {
    stageExecutionMapper = new StageExecutionMapper();

    // Initialize test data
    testId = UUID.randomUUID();
    testStageId = UUID.randomUUID();
    testPipelineExecutionId = UUID.randomUUID();
    testExecutionOrder = 3;
    testCommitHash = "abc123";
    testIsLocal = true;
    testStatus = ExecutionStatus.SUCCESS;
    testStartTime = Instant.now().minusSeconds(3600);
    testCompletionTime = Instant.now();
  }

  @Test
  void testToDTOWithValidEntity() {
    // Arrange
    StageExecutionEntity entity = StageExecutionEntity.builder()
            .id(testId)
            .stageId(testStageId)
            .pipelineExecutionId(testPipelineExecutionId)
            .executionOrder(testExecutionOrder)
            .commitHash(testCommitHash)
            .isLocal(testIsLocal)
            .status(testStatus)
            .startTime(testStartTime)
            .completionTime(testCompletionTime)
            .build();

    // Act
    StageExecutionDTO dto = stageExecutionMapper.toDTO(entity);

    // Assert
    assertNotNull(dto);
    assertEquals(testId, dto.getId());
    assertEquals(testStageId, dto.getStageId());
    assertEquals(testPipelineExecutionId, dto.getPipelineExecutionId());
    assertEquals(testExecutionOrder, dto.getExecutionOrder());
    assertEquals(testCommitHash, dto.getCommitHash());
    assertEquals(testIsLocal, dto.isLocal());
    assertEquals(testStatus, dto.getStatus());
    assertEquals(testStartTime, dto.getStartTime());
    assertEquals(testCompletionTime, dto.getCompletionTime());
  }

  @Test
  void testToDTOWithNullEntity() {
    // Act
    StageExecutionDTO dto = stageExecutionMapper.toDTO(null);

    // Assert
    assertNull(dto);
  }

  @Test
  void testToEntityWithValidDTO() {
    // Arrange
    StageExecutionDTO dto = StageExecutionDTO.builder()
            .id(testId)
            .stageId(testStageId)
            .pipelineExecutionId(testPipelineExecutionId)
            .executionOrder(testExecutionOrder)
            .commitHash(testCommitHash)
            .isLocal(testIsLocal)
            .status(testStatus)
            .startTime(testStartTime)
            .completionTime(testCompletionTime)
            .build();

    // Act
    StageExecutionEntity entity = stageExecutionMapper.toEntity(dto);

    // Assert
    assertNotNull(entity);
    assertEquals(testId, entity.getId());
    assertEquals(testStageId, entity.getStageId());
    assertEquals(testPipelineExecutionId, entity.getPipelineExecutionId());
    assertEquals(testExecutionOrder, entity.getExecutionOrder());
    assertEquals(testCommitHash, entity.getCommitHash());
    assertEquals(testIsLocal, entity.isLocal());
    assertEquals(testStatus, entity.getStatus());
    assertEquals(testStartTime, entity.getStartTime());
    assertEquals(testCompletionTime, entity.getCompletionTime());
  }

  @Test
  void testToEntityWithNullDTO() {
    // Act
    StageExecutionEntity entity = stageExecutionMapper.toEntity(null);

    // Assert
    assertNull(entity);
  }

  @Test
  void testBidirectionalMapping() {
    // Arrange
    StageExecutionEntity originalEntity = StageExecutionEntity.builder()
            .id(testId)
            .stageId(testStageId)
            .pipelineExecutionId(testPipelineExecutionId)
            .executionOrder(testExecutionOrder)
            .commitHash(testCommitHash)
            .isLocal(testIsLocal)
            .status(testStatus)
            .startTime(testStartTime)
            .completionTime(testCompletionTime)
            .build();

    // Act
    StageExecutionDTO dto = stageExecutionMapper.toDTO(originalEntity);
    StageExecutionEntity convertedEntity = stageExecutionMapper.toEntity(dto);

    // Assert
    assertNotNull(convertedEntity);
    assertEquals(originalEntity.getId(), convertedEntity.getId());
    assertEquals(originalEntity.getStageId(), convertedEntity.getStageId());
    assertEquals(originalEntity.getPipelineExecutionId(), convertedEntity.getPipelineExecutionId());
    assertEquals(originalEntity.getExecutionOrder(), convertedEntity.getExecutionOrder());
    assertEquals(originalEntity.getCommitHash(), convertedEntity.getCommitHash());
    assertEquals(originalEntity.isLocal(), convertedEntity.isLocal());
    assertEquals(originalEntity.getStatus(), convertedEntity.getStatus());
    assertEquals(originalEntity.getStartTime(), convertedEntity.getStartTime());
    assertEquals(originalEntity.getCompletionTime(), convertedEntity.getCompletionTime());
  }

  @Test
  void testToDTOWithPartialEntity() {
    // Arrange - Entity with some null fields
    StageExecutionEntity entity = StageExecutionEntity.builder()
            .id(testId)
            .stageId(testStageId)
            .pipelineExecutionId(testPipelineExecutionId)
            .executionOrder(testExecutionOrder)
            .commitHash(null) // Null field
            .isLocal(testIsLocal)
            .status(null) // Null field
            .startTime(testStartTime)
            .completionTime(null) // Null field
            .build();

    // Act
    StageExecutionDTO dto = stageExecutionMapper.toDTO(entity);

    // Assert
    assertNotNull(dto);
    assertEquals(testId, dto.getId());
    assertEquals(testStageId, dto.getStageId());
    assertEquals(testPipelineExecutionId, dto.getPipelineExecutionId());
    assertEquals(testExecutionOrder, dto.getExecutionOrder());
    assertNull(dto.getCommitHash());
    assertEquals(testIsLocal, dto.isLocal());
    assertNull(dto.getStatus());
    assertEquals(testStartTime, dto.getStartTime());
    assertNull(dto.getCompletionTime());
  }

  @Test
  void testToEntityWithPartialDTO() {
    // Arrange - DTO with some null fields
    StageExecutionDTO dto = StageExecutionDTO.builder()
            .id(testId)
            .stageId(testStageId)
            .pipelineExecutionId(testPipelineExecutionId)
            .executionOrder(testExecutionOrder)
            .commitHash(null) // Null field
            .isLocal(testIsLocal)
            .status(testStatus)
            .startTime(null) // Null field
            .completionTime(testCompletionTime)
            .build();

    // Act
    StageExecutionEntity entity = stageExecutionMapper.toEntity(dto);

    // Assert
    assertNotNull(entity);
    assertEquals(testId, entity.getId());
    assertEquals(testStageId, entity.getStageId());
    assertEquals(testPipelineExecutionId, entity.getPipelineExecutionId());
    assertEquals(testExecutionOrder, entity.getExecutionOrder());
    assertNull(entity.getCommitHash());
    assertEquals(testIsLocal, entity.isLocal());
    assertEquals(testStatus, entity.getStatus());
    assertNull(entity.getStartTime());
    assertEquals(testCompletionTime, entity.getCompletionTime());
  }
}