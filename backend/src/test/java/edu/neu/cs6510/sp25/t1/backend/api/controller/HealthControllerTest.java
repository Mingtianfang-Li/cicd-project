package edu.neu.cs6510.sp25.t1.backend.api.controller;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit test for {@link HealthController}.
 */
class HealthControllerTest {

  private final HealthController healthController = new HealthController();

  @Test
  void testHealthCheck_ReturnsUpStatus() {
    // Act
    Map<String, String> response = healthController.healthCheck();

    // Assert
    assertNotNull(response, "Response should not be null");
    assertEquals(1, response.size(), "Response should contain one key-value pair");
    assertEquals("UP", response.get("status"), "Status should be 'UP'");
  }
}