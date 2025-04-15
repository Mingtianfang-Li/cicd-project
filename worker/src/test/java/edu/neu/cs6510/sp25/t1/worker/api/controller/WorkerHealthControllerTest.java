package edu.neu.cs6510.sp25.t1.worker.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the WorkerHealthController.
 */
public class WorkerHealthControllerTest {

    private final WorkerHealthController controller = new WorkerHealthController();

    /**
     * Test that the health check endpoint returns the expected response.
     */
    @Test
    public void healthCheck_ShouldReturnOkWithMessage() {
        // Act
        ResponseEntity<String> response = controller.healthCheck();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Worker is running.", response.getBody());
    }
}