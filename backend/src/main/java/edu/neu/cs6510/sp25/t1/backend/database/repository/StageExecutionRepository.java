package edu.neu.cs6510.sp25.t1.backend.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.backend.database.entity.StageExecutionEntity;

/**
 * Repository for managing StageExecution entities.
 */
@Repository
public interface StageExecutionRepository extends JpaRepository<StageExecutionEntity, UUID> {

  /**
   * Finds stage executions by pipeline execution ID.
   *
   * @param pipelineExecutionId the pipeline execution ID
   * @return a list of stage executions associated with the given pipeline execution
   */
  List<StageExecutionEntity> findByPipelineExecutionId(UUID pipelineExecutionId);

  /**
   * Finds a specific stage execution by stage ID and pipeline execution ID.
   *
   * @param stageId             the stage ID
   * @param pipelineExecutionId the pipeline execution ID
   * @return an optional stage execution matching the criteria
   */
  Optional<StageExecutionEntity> findByStageIdAndPipelineExecutionId(UUID stageId, UUID pipelineExecutionId);

  /**
   * Finds a specific stage execution by pipeline execution ID and stage ID.
   *
   * @param pipelineExecutionId the pipeline execution ID
   * @param stageId             the stage ID
   * @return an optional stage execution matching the criteria
   */
  Optional<StageExecutionEntity> findByPipelineExecutionIdAndStageId(UUID pipelineExecutionId, UUID stageId);

  /**
   * Join stageExecution and stage to fetch the stage name by stageId
   *
   * @param stageId the stage ID
   * @return an optional stage name
   */
  @Query("SELECT s.name FROM StageExecutionEntity se JOIN StageEntity s ON se.stageId = s.id WHERE se.stageId = :stageId")
  Optional<String> findStageNameByStageId(@Param("stageId") UUID stageId);

  /**
   * Join stageExecution and stage tables to fetch the stage name by pipelineExecutionId and stageName
   *
   * @param pipelineExecutionId the pipeline execution ID
   * @param stageName           the name of the stage
   * @return a list of stage executions with the stage name
   */
  @Query("SELECT se FROM StageExecutionEntity se JOIN StageEntity s ON se.stageId = s.id WHERE se.pipelineExecutionId = :pipelineExecutionId AND s.name = :stageName ORDER BY se.startTime DESC")
  List<StageExecutionEntity> findByPipelineExecutionIdAndStageNameOrderByStartTimeDesc(@Param("pipelineExecutionId") UUID pipelineExecutionId, @Param("stageName") String stageName);

}
