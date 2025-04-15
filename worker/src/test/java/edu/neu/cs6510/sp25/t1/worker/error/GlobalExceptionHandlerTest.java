package edu.neu.cs6510.sp25.t1.worker.error;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;

/**
 * Tests for the GlobalExceptionHandler class.
 */
@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    private static final String REQUEST_URI = "/api/worker/jobs/123";

    @BeforeEach
    public void setUp() {
        when(webRequest.getDescription(false)).thenReturn(REQUEST_URI);
    }

    @Test
    public void handleDockerExecutionException_shouldReturnInternalServerError() {
        // Arrange
        String errorMessage = "Container failed to start";
        int exitCode = 127;
        DockerExecutionException exception = new DockerExecutionException(errorMessage, exitCode);

        // Act
        ResponseEntity<WorkerApiError> response = exceptionHandler.handleDockerExecutionException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        WorkerApiError error = response.getBody();
        assertNotNull(error);
        assertEquals("DOCKER_ERROR", error.getStatus());
        assertEquals("Docker Execution Failed", error.getMessage());
        assertTrue(error.getDetail().contains(errorMessage));
        assertTrue(error.getDetail().contains("Exit code: " + exitCode));
        assertEquals(REQUEST_URI, error.getPath());
    }

    @Test
    public void handleDockerExecutionException_withCauseConstructor_shouldReturnInternalServerError() {
        // Arrange
        String errorMessage = "Docker execution failed";
        DockerExecutionException exception = new DockerExecutionException(errorMessage, new RuntimeException());

        // Act
        ResponseEntity<WorkerApiError> response = exceptionHandler.handleDockerExecutionException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        WorkerApiError error = response.getBody();
        assertNotNull(error);
        assertEquals("DOCKER_ERROR", error.getStatus());
        assertEquals("Docker Execution Failed", error.getMessage());
        assertTrue(error.getDetail().contains(errorMessage));
        assertFalse(error.getDetail().contains("Exit code:"), "Should not include exit code for exceptions with a cause");
        assertEquals(REQUEST_URI, error.getPath());
    }

    @Test
    public void handleBackendCommunicationException_shouldReturnServiceUnavailable() {
        // Arrange
        String errorMessage = "Failed to connect to backend service";
        BackendCommunicationException exception = new BackendCommunicationException(errorMessage);

        // Act
        ResponseEntity<WorkerApiError> response = exceptionHandler.handleBackendCommunicationException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        WorkerApiError error = response.getBody();
        assertNotNull(error);
        assertEquals("BACKEND_COMMUNICATION_ERROR", error.getStatus());
        assertEquals("Backend Communication Failed", error.getMessage());
        assertEquals(errorMessage, error.getDetail());
        assertEquals(REQUEST_URI, error.getPath());
    }

    @Test
    public void handleJobExecutionConfigException_shouldReturnBadRequest() {
        // Arrange
        String errorMessage = "Missing required script parameter";
        JobExecutionConfigException exception = new JobExecutionConfigException(errorMessage);

        // Act
        ResponseEntity<WorkerApiError> response = exceptionHandler.handleJobExecutionConfigException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        WorkerApiError error = response.getBody();
        assertNotNull(error);
        assertEquals("CONFIG_ERROR", error.getStatus());
        assertEquals("Job Configuration Error", error.getMessage());
        assertEquals(errorMessage, error.getDetail());
        assertEquals(REQUEST_URI, error.getPath());
    }

    @Test
    public void handleResourceAccessException_shouldReturnServiceUnavailable() {
        // Arrange
        String errorMessage = "Connection refused";
        ResourceAccessException exception = new ResourceAccessException(errorMessage);

        // Act
        ResponseEntity<WorkerApiError> response = exceptionHandler.handleResourceAccessException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        WorkerApiError error = response.getBody();
        assertNotNull(error);
        assertEquals("RESOURCE_ACCESS_ERROR", error.getStatus());
        assertEquals("Resource Access Failed", error.getMessage());
        assertTrue(error.getDetail().contains(errorMessage));
        assertEquals(REQUEST_URI, error.getPath());
    }

    @Test
    public void handleGenericException_shouldReturnInternalServerError() {
        // Arrange
        String errorMessage = "Unexpected runtime error";
        Exception exception = new RuntimeException(errorMessage);

        // Act
        ResponseEntity<WorkerApiError> response = exceptionHandler.handleGenericException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        WorkerApiError error = response.getBody();
        assertNotNull(error);
        assertEquals("INTERNAL_ERROR", error.getStatus());
        assertEquals("Internal Server Error", error.getMessage());
        assertTrue(error.getDetail().contains(errorMessage));
        assertEquals(REQUEST_URI, error.getPath());
    }
}