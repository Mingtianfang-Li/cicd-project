package edu.neu.cs6510.sp25.t1.worker.error;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for the BackendCommunicationException class.
 */
public class BackendCommunicationExceptionTest {

    @Test
    public void constructorWithMessage_shouldSetMessage() {
        // Arrange
        String errorMessage = "Failed to communicate with backend";

        // Act
        BackendCommunicationException exception = new BackendCommunicationException(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void constructorWithMessageAndCause_shouldSetMessageAndCause() {
        // Arrange
        String errorMessage = "Failed to communicate with backend";
        Throwable cause = new RuntimeException("Connection refused");

        // Act
        BackendCommunicationException exception = new BackendCommunicationException(errorMessage, cause);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void exceptionShouldBeInstanceOfWorkerException() {
        // Act
        BackendCommunicationException exception = new BackendCommunicationException("Test");

        // Assert
        assertTrue(exception instanceof WorkerException);
    }
}