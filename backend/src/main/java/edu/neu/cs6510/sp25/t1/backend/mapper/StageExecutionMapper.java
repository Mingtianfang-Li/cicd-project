package edu.neu.cs6510.sp25.t1.backend.mapper;

import org.springframework.stereotype.Component;

import edu.neu.cs6510.sp25.t1.backend.database.entity.StageExecutionEntity;
import edu.neu.cs6510.sp25.t1.common.dto.StageExecutionDTO;

/**
 * Mapper for converting between StageExecutionEntity and StageExecutionDTO.
 */
@Component
public class StageExecutionMapper {

  /**
   * Converts a StageExecutionEntity to StageExecutionDTO.
   *
   * @param entity the entity to convert
   * @return the corresponding DTO, or null if entity is null
   */
  public StageExecutionDTO toDTO(StageExecutionEntity entity) {
    if (entity == null) {
      return null;
    }

    return StageExecutionDTO.builder()
            .id(entity.getId())
            .stageId(entity.getStageId())
            .pipelineExecutionId(entity.getPipelineExecutionId())
            .executionOrder(entity.getExecutionOrder())
            .commitHash(entity.getCommitHash())
            .isLocal(entity.isLocal())
            .status(entity.getStatus())
            .startTime(entity.getStartTime())
            .completionTime(entity.getCompletionTime())
            .build();
  }

  /**
   * Converts a StageExecutionDTO to StageExecutionEntity.
   *
   * @param dto the DTO to convert
   * @return the corresponding entity, or null if dto is null
   */
  public StageExecutionEntity toEntity(StageExecutionDTO dto) {
    if (dto == null) {
      return null;
    }

    return StageExecutionEntity.builder()
            .id(dto.getId())
            .stageId(dto.getStageId())
            .pipelineExecutionId(dto.getPipelineExecutionId())
            .executionOrder(dto.getExecutionOrder())
            .commitHash(dto.getCommitHash())
            .isLocal(dto.isLocal())
            .status(dto.getStatus())
            .startTime(dto.getStartTime())
            .completionTime(dto.getCompletionTime())
            .build();
  }
}