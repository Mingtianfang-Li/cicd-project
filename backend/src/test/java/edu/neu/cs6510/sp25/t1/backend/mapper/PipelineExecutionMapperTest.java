package edu.neu.cs6510.sp25.t1.backend.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.backend.database.entity.PipelineExecutionEntity;
import edu.neu.cs6510.sp25.t1.common.dto.PipelineExecutionDTO;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PipelineExecutionMapperTest {

  private PipelineExecutionMapper pipelineExecutionMapper;

  private UUID testId;
  private UUID testPipelineId;
  private int testRunNumber;
  private String testCommitHash;
  private boolean testIsLocal;
  private ExecutionStatus testStatus;
  private Instant testStartTime;
  private Instant testCompletionTime;

  @BeforeEach
  void setUp() {
    pipelineExecutionMapper = new PipelineExecutionMapper();

    // Initialize test data
    testId = UUID.randomUUID();
    testPipelineId = UUID.randomUUID();
    testRunNumber = 42;
    testCommitHash = "abc123";
    testIsLocal = true;
    testStatus = ExecutionStatus.SUCCESS;
    testStartTime = Instant.now().minusSeconds(3600);
    testCompletionTime = Instant.now();
  }

  @Test
  void testToDTOWithValidEntity() {
    // Arrange
    PipelineExecutionEntity entity = PipelineExecutionEntity.builder()
            .id(testId)
            .pipelineId(testPipelineId)
            .runNumber(testRunNumber)
            .commitHash(testCommitHash)
            .isLocal(testIsLocal)
            .status(testStatus)
            .startTime(testStartTime)
            .completionTime(testCompletionTime)
            .build();

    // Act
    PipelineExecutionDTO dto = pipelineExecutionMapper.toDTO(entity);

    // Assert
    assertNotNull(dto);
    assertEquals(testId, dto.getId());
    assertEquals(testPipelineId, dto.getPipelineId());
    assertEquals(testRunNumber, dto.getRunNumber());
    assertEquals(testCommitHash, dto.getCommitHash());
    assertEquals(testIsLocal, dto.isLocal());
    assertEquals(testStatus, dto.getStatus());
    assertEquals(testStartTime, dto.getStartTime());
    assertEquals(testCompletionTime, dto.getCompletionTime());
  }

  @Test
  void testToEntityWithValidDTO() {
    // Arrange
    PipelineExecutionDTO dto = PipelineExecutionDTO.builder()
            .id(testId)
            .pipelineId(testPipelineId)
            .runNumber(testRunNumber)
            .commitHash(testCommitHash)
            .isLocal(testIsLocal)
            .status(testStatus)
            .startTime(testStartTime)
            .completionTime(testCompletionTime)
            .build();

    // Act
    PipelineExecutionEntity entity = pipelineExecutionMapper.toEntity(dto);

    // Assert
    assertNotNull(entity);
    assertEquals(testId, entity.getId());
    assertEquals(testPipelineId, entity.getPipelineId());
    assertEquals(testRunNumber, entity.getRunNumber());
    assertEquals(testCommitHash, entity.getCommitHash());
    assertEquals(testIsLocal, entity.isLocal());
    assertEquals(testStatus, entity.getStatus());
    assertEquals(testStartTime, entity.getStartTime());
    assertEquals(testCompletionTime, entity.getCompletionTime());
  }

  @Test
  void testBidirectionalMapping() {
    // Arrange
    PipelineExecutionEntity originalEntity = PipelineExecutionEntity.builder()
            .id(testId)
            .pipelineId(testPipelineId)
            .runNumber(testRunNumber)
            .commitHash(testCommitHash)
            .isLocal(testIsLocal)
            .status(testStatus)
            .startTime(testStartTime)
            .completionTime(testCompletionTime)
            .build();

    // Act
    PipelineExecutionDTO dto = pipelineExecutionMapper.toDTO(originalEntity);
    PipelineExecutionEntity convertedEntity = pipelineExecutionMapper.toEntity(dto);

    // Assert
    assertNotNull(convertedEntity);
    assertEquals(originalEntity.getId(), convertedEntity.getId());
    assertEquals(originalEntity.getPipelineId(), convertedEntity.getPipelineId());
    assertEquals(originalEntity.getRunNumber(), convertedEntity.getRunNumber());
    assertEquals(originalEntity.getCommitHash(), convertedEntity.getCommitHash());
    assertEquals(originalEntity.isLocal(), convertedEntity.isLocal());
    assertEquals(originalEntity.getStatus(), convertedEntity.getStatus());
    assertEquals(originalEntity.getStartTime(), convertedEntity.getStartTime());
    assertEquals(originalEntity.getCompletionTime(), convertedEntity.getCompletionTime());
  }

  @Test
  void testToDTOWithNullFields() {
    // Arrange - Entity with null fields
    PipelineExecutionEntity entity = PipelineExecutionEntity.builder()
            .id(testId)
            .pipelineId(testPipelineId)
            .runNumber(testRunNumber)
            .commitHash(null) // Null commit hash
            .isLocal(testIsLocal)
            .status(null) // Null status
            .startTime(testStartTime)
            .completionTime(null) // Null completion time
            .build();

    // Act
    PipelineExecutionDTO dto = pipelineExecutionMapper.toDTO(entity);

    // Assert
    assertNotNull(dto);
    assertEquals(testId, dto.getId());
    assertEquals(testPipelineId, dto.getPipelineId());
    assertEquals(testRunNumber, dto.getRunNumber());
    assertNull(dto.getCommitHash());
    assertEquals(testIsLocal, dto.isLocal());
    assertNull(dto.getStatus());
    assertEquals(testStartTime, dto.getStartTime());
    assertNull(dto.getCompletionTime());
  }

  @Test
  void testToEntityWithNullFields() {
    // Arrange - DTO with null fields
    PipelineExecutionDTO dto = PipelineExecutionDTO.builder()
            .id(testId)
            .pipelineId(testPipelineId)
            .runNumber(testRunNumber)
            .commitHash(null) // Null commit hash
            .isLocal(testIsLocal)
            .status(null) // Null status
            .startTime(null) // Null start time
            .completionTime(testCompletionTime)
            .build();

    // Act
    PipelineExecutionEntity entity = pipelineExecutionMapper.toEntity(dto);

    // Assert
    assertNotNull(entity);
    assertEquals(testId, entity.getId());
    assertEquals(testPipelineId, entity.getPipelineId());
    assertEquals(testRunNumber, entity.getRunNumber());
    assertNull(entity.getCommitHash());
    assertEquals(testIsLocal, entity.isLocal());
    assertNull(entity.getStatus());
    assertNull(entity.getStartTime());
    assertEquals(testCompletionTime, entity.getCompletionTime());
  }

  @Test
  void testToDTOWithZeroRunNumber() {
    // Arrange
    PipelineExecutionEntity entity = PipelineExecutionEntity.builder()
            .id(testId)
            .pipelineId(testPipelineId)
            .runNumber(0) // Zero run number
            .commitHash(testCommitHash)
            .isLocal(testIsLocal)
            .status(testStatus)
            .startTime(testStartTime)
            .completionTime(testCompletionTime)
            .build();

    // Act
    PipelineExecutionDTO dto = pipelineExecutionMapper.toDTO(entity);

    // Assert
    assertNotNull(dto);
    assertEquals(0, dto.getRunNumber());
  }
}