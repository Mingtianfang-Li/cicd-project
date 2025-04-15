package edu.neu.cs6510.sp25.t1.worker.error;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for the DockerExecutionException class.
 */
public class DockerExecutionExceptionTest {

    @Test
    public void constructorWithMessageAndExitCode_shouldSetMessageAndExitCode() {
        // Arrange
        String errorMessage = "Docker container exited with non-zero code";
        int exitCode = 127;

        // Act
        DockerExecutionException exception = new DockerExecutionException(errorMessage, exitCode);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(exitCode, exception.getExitCode());
        assertNull(exception.getCause());
    }

    @Test
    public void constructorWithMessageAndCause_shouldSetMessageAndCauseAndDefaultExitCode() {
        // Arrange
        String errorMessage = "Docker execution failed";
        Throwable cause = new RuntimeException("Container not found");

        // Act
        DockerExecutionException exception = new DockerExecutionException(errorMessage, cause);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(-1, exception.getExitCode(), "Exit code should default to -1 when using cause constructor");
    }

    @Test
    public void getExitCode_shouldReturnCorrectValue() {
        // Arrange
        int exitCode = 1;
        DockerExecutionException exception = new DockerExecutionException("Test", exitCode);

        // Act
        int returnedExitCode = exception.getExitCode();

        // Assert
        assertEquals(exitCode, returnedExitCode);
    }

    @Test
    public void exceptionShouldBeInstanceOfWorkerException() {
        // Act
        DockerExecutionException exception = new DockerExecutionException("Test", 1);

        // Assert
        assertTrue(exception instanceof WorkerException);
    }

    @Test
    public void exitCodeShouldBeNegativeOneWhenUsingCauseConstructor() {
        // Act
        DockerExecutionException exception = new DockerExecutionException(
                "Docker execution failed", new RuntimeException());

        // Assert
        assertEquals(-1, exception.getExitCode());
    }
}