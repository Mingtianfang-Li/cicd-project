package edu.neu.cs6510.sp25.t1.worker.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for worker health check.
 */
@RestController
@RequestMapping("/api/worker/health")
public class WorkerHealthController {

  /**
   * Health check endpoint for the worker service.
   *
   * @return ResponseEntity indicating worker is alive.
   */
  @GetMapping
  public ResponseEntity<String> healthCheck() {
    return ResponseEntity.ok("Worker is running.");
  }
}
