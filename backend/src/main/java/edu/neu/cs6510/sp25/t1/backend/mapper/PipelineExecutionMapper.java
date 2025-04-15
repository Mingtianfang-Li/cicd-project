package edu.neu.cs6510.sp25.t1.backend.mapper;

import org.springframework.stereotype.Component;

import edu.neu.cs6510.sp25.t1.backend.database.entity.PipelineExecutionEntity;
import edu.neu.cs6510.sp25.t1.common.dto.PipelineExecutionDTO;

/**
 * Mapper for converting between PipelineExecutionEntity and PipelineExecutionDTO.
 */
@Component
public class PipelineExecutionMapper {

  /**
   * Converts a PipelineExecutionEntity to PipelineExecutionDTO.
   *
   * @param entity the entity to convert
   * @return the corresponding DTO
   */
  public PipelineExecutionDTO toDTO(PipelineExecutionEntity entity) {
    return PipelineExecutionDTO.builder()
            .id(entity.getId())
            .pipelineId(entity.getPipelineId())
            .runNumber(entity.getRunNumber())
            .commitHash(entity.getCommitHash())
            .isLocal(entity.isLocal())
            .status(entity.getStatus())
            .startTime(entity.getStartTime())
            .completionTime(entity.getCompletionTime())
            .build();
  }

  /**
   * Converts a PipelineExecutionDTO to PipelineExecutionEntity.
   *
   * @param dto the DTO to convert
   * @return the corresponding entity
   */
  public PipelineExecutionEntity toEntity(PipelineExecutionDTO dto) {
    return PipelineExecutionEntity.builder()
            .id(dto.getId())
            .pipelineId(dto.getPipelineId())
            .runNumber(dto.getRunNumber())
            .commitHash(dto.getCommitHash())
            .isLocal(dto.isLocal())
            .status(dto.getStatus())
            .startTime(dto.getStartTime())
            .completionTime(dto.getCompletionTime())
            .build();
  }
}
