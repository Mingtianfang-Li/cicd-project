package edu.neu.cs6510.sp25.t1.backend.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for handling health check requests.
 * This controller provides an endpoint to check the health status of the application.
 * It returns a simple JSON response indicating the status of the application.
 */
@RestController
@RequestMapping("/health")
public class HealthController {

  /**
   * Endpoint to check the health status of the application.
   *
   * @return A map containing the health status of the application.
   */
  @GetMapping
  public Map<String, String> healthCheck() {
    return Map.of("status", "UP");
  }
}
