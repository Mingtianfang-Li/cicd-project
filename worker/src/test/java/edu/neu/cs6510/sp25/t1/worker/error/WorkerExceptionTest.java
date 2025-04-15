package edu.neu.cs6510.sp25.t1.worker.error;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Tests for the WorkerException class.
 */
public class WorkerExceptionTest {

    @Test
    public void constructorWithMessage_shouldSetMessage() {
        // Arrange
        String errorMessage = "Worker error occurred";

        // Act
        WorkerException exception = new WorkerException(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void constructorWithMessageAndCause_shouldSetMessageAndCause() {
        // Arrange
        String errorMessage = "Worker error occurred";
        Throwable cause = new IllegalStateException("System in invalid state");

        // Act
        WorkerException exception = new WorkerException(errorMessage, cause);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void exceptionShouldBeInstanceOfRuntimeException() {
        // Act
        WorkerException exception = new WorkerException("Test exception");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    public void exceptionShouldPreserveStackTrace() {
        // Arrange
        Throwable originalCause = new NullPointerException("Null reference");
        StackTraceElement[] originalStackTrace = originalCause.getStackTrace();

        // Act
        WorkerException exception = new WorkerException("Worker error", originalCause);

        // Assert
        assertArrayEquals(originalStackTrace, exception.getCause().getStackTrace(),
                "Stack trace should be preserved in the wrapped exception");
    }

    @Test
    public void nestedExceptionsShouldMaintainCauseChain() {
        // Arrange
        Throwable rootCause = new IllegalArgumentException("Invalid argument");
        Throwable intermediateCause = new IllegalStateException("Invalid state", rootCause);

        // Act
        WorkerException exception = new WorkerException("Worker error", intermediateCause);

        // Assert
        assertEquals(intermediateCause, exception.getCause(),
                "Direct cause should be the intermediate exception");
        assertEquals(rootCause, exception.getCause().getCause(),
                "Root cause should be preserved in the cause chain");
    }
}