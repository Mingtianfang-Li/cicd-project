package edu.neu.cs6510.sp25.t1.worker.error;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for the JobExecutionConfigException class.
 */
public class JobExecutionConfigExceptionTest {

    @Test
    public void constructorWithMessage_shouldSetMessage() {
        // Arrange
        String errorMessage = "Missing required parameter 'script'";

        // Act
        JobExecutionConfigException exception = new JobExecutionConfigException(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void constructorWithMessageAndCause_shouldSetMessageAndCause() {
        // Arrange
        String errorMessage = "Invalid job configuration";
        Throwable cause = new IllegalArgumentException("Parameter value out of range");

        // Act
        JobExecutionConfigException exception = new JobExecutionConfigException(errorMessage, cause);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void exceptionShouldBeInstanceOfWorkerException() {
        // Act
        JobExecutionConfigException exception = new JobExecutionConfigException("Test");

        // Assert
        assertTrue(exception instanceof WorkerException);
    }

    @Test
    public void messageShouldBePreservedInInheritanceChain() {
        // Arrange
        String errorMessage = "Configuration conflict detected";

        // Act
        JobExecutionConfigException exception = new JobExecutionConfigException(errorMessage);
        WorkerException workerException = exception;

        // Assert
        assertEquals(errorMessage, workerException.getMessage(),
                "Exception message should be preserved when accessed through parent class reference");
    }
}