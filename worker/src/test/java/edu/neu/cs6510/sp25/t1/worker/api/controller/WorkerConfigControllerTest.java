package edu.neu.cs6510.sp25.t1.worker.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the WorkerConfigController.
 */
public class WorkerConfigControllerTest {

    /**
     * Test that the getConfig method returns expected values with default property values
     */
    @Test
    public void getConfig_WithDefaultValues_ShouldReturnOkWithDefaultConfig() {
        // Arrange
        WorkerConfigController controller = new WorkerConfigController();
        ReflectionTestUtils.setField(controller, "maxRetries", 10);
        ReflectionTestUtils.setField(controller, "retryDelay", 2000);

        // Act
        ResponseEntity<String> response = controller.getConfig();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String expectedResponse = "Worker Config - MaxRetries: 10, RetryDelay: 2000ms";
        assertEquals(expectedResponse, response.getBody());
    }

    /**
     * Test that the getConfig method returns expected values with custom property values
     */
    @Test
    public void getConfig_WithCustomValues_ShouldReturnOkWithCustomConfig() {
        // Arrange
        WorkerConfigController controller = new WorkerConfigController();
        ReflectionTestUtils.setField(controller, "maxRetries", 5);
        ReflectionTestUtils.setField(controller, "retryDelay", 1000);

        // Act
        ResponseEntity<String> response = controller.getConfig();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String expectedResponse = "Worker Config - MaxRetries: 5, RetryDelay: 1000ms";
        assertEquals(expectedResponse, response.getBody());
    }
}