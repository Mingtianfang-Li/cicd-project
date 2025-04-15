package edu.neu.cs6510.sp25.t1.worker.api.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for retrieving worker configurations.
 */
@RestController
@RequestMapping("/api/worker/config")
public class WorkerConfigController {

  @Value("${worker.maxRetries:10}")
  private int maxRetries;

  @Value("${worker.retryDelay:2000}")
  private int retryDelay;

  /**
   * Gets worker execution configurations.
   *
   * @return ResponseEntity with configuration details.
   */
  @GetMapping
  public ResponseEntity<String> getConfig() {
    return ResponseEntity.ok("Worker Config - MaxRetries: " + maxRetries + ", RetryDelay: " + retryDelay + "ms");
  }
}
