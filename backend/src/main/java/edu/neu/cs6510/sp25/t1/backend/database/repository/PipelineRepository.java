package edu.neu.cs6510.sp25.t1.backend.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.backend.database.entity.PipelineEntity;

/**
 * Repository for managing Pipeline entities.
 */
@Repository
public interface PipelineRepository extends JpaRepository<PipelineEntity, UUID> {

  /**
   * Finds a pipeline by its unique name.
   *
   * @param name the name of the pipeline
   * @return an optional pipeline entity
   */
  Optional<PipelineEntity> findByName(String name);
}
