package edu.neu.cs6510.sp25.t1.backend.database.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.backend.database.entity.StageEntity;

/**
 * Repository for managing Stage entities.
 */
@Repository
public interface StageRepository extends JpaRepository<StageEntity, UUID> {

  /**
   * Finds all stages associated with a specific pipeline.
   *
   * @param pipelineId the pipeline ID
   * @return a list of stages belonging to the specified pipeline
   */
  List<StageEntity> findByPipelineId(UUID pipelineId);
}
