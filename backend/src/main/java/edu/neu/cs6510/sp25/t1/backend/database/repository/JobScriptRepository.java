package edu.neu.cs6510.sp25.t1.backend.database.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Repository for managing job scripts.
 */
@Repository
public class JobScriptRepository {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Saves a script for a job.
   *
   * @param jobId  the job ID
   * @param script the script content
   */
  @Transactional
  public void saveScript(UUID jobId, String script) {
    String query = "INSERT INTO job_scripts (job_id, script) VALUES (:jobId, :script)";
    entityManager.createNativeQuery(query)
        .setParameter("jobId", jobId)
        .setParameter("script", script)
        .executeUpdate();
  }
}