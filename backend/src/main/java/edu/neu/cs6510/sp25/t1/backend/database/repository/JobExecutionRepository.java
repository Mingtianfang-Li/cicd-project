package edu.neu.cs6510.sp25.t1.backend.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.backend.database.entity.JobExecutionEntity;
import edu.neu.cs6510.sp25.t1.backend.database.entity.StageExecutionEntity;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;

/**
 * Repository for managing JobExecution entities.
 */
@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecutionEntity, UUID> {

  List<JobExecutionEntity> findByStageExecution(StageExecutionEntity stageExecution);

  Optional<JobExecutionEntity> findByJobId(UUID jobId);


  List<JobExecutionEntity> findByStatus(ExecutionStatus status);

  @Query("SELECT j.name FROM JobExecutionEntity je JOIN JobEntity j ON je.jobId = j.id WHERE je.jobId = :jobId")
  Optional<String> findJobNameByJobId(@Param("jobId") UUID jobId);

  @Query("SELECT je FROM JobExecutionEntity je JOIN JobEntity j ON je.jobId = j.id WHERE je.stageExecution.id = :stageExecutionId AND j.name = :jobName ORDER BY je.startTime DESC")
  List<JobExecutionEntity> findByStageExecutionAndJobNameOrderByStartTimeDesc(@Param("stageExecutionId") UUID stageExecutionId, @Param("jobName") String jobName);

  @Query("SELECT jd.dependency.id FROM JobDependencyEntity jd WHERE jd.job.id = :jobId")
  List<UUID> findDependenciesByJobId(@Param("jobId") UUID jobId);
}
