package edu.neu.cs6510.sp25.t1.backend.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.backend.database.entity.PipelineExecutionEntity;

/**
 * Repository for managing PipelineExecution entities.
 */
@Repository
public interface PipelineExecutionRepository extends JpaRepository<PipelineExecutionEntity, UUID> {

  /**
   * Finds pipeline executions by pipeline ID.
   *
   * @param pipelineId the pipeline ID
   * @return a list of pipeline executions associated with the given pipeline
   */
  Optional<PipelineExecutionEntity> findByPipelineId(UUID pipelineId);

  /**
   * Finds a specific pipeline execution by pipeline ID and run number.
   *
   * @param pipelineId the pipeline ID
   * @param runNumber  the run number of the execution
   * @return an optional pipeline execution matching the criteria
   */
  Optional<PipelineExecutionEntity> findByPipelineIdAndRunNumber(UUID pipelineId, int runNumber);

  /**
   * dynamically fetches the pipeline name associated with a pipeline execution
   * and return the entity
   *
   * @param pipelineName the name of the pipeline
   * @return a list of pipeline
   */
  @Query("SELECT pe FROM PipelineExecutionEntity pe JOIN PipelineEntity p ON pe.pipelineId = p.id WHERE p.name = :pipelineName ORDER BY pe.startTime DESC")
  List<PipelineExecutionEntity> findByPipelineNameOrderByStartTimeDesc(@Param("pipelineName") String pipelineName);

  /**
   * Join pipelineExecution and pipeline tables to fetch the pipeline name by pipelineId
   *
   * @param pipelineId the pipeline ID
   * @return an optional pipeline name
   */
  @Query("SELECT p.name FROM PipelineExecutionEntity pe JOIN PipelineEntity p ON pe.pipelineId = p.id WHERE pe.pipelineId = :pipelineId")
  Optional<String> findPipelineNameByPipelineId(@Param("pipelineId") UUID pipelineId);

  /**
   * Find pipeline
   *
   * @param pipelineName the name of the pipeline
   * @return an optional pipeline id
   */
  @Query("SELECT p.id FROM PipelineEntity p WHERE p.name = :pipelineName")
  Optional<UUID> findPipelineIdByName(@Param("pipelineName") String pipelineName);
}
