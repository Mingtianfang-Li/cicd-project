package edu.neu.cs6510.sp25.t1.worker.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Tests for the WorkerApiError class.
 */
public class WorkerApiErrorTest {

    @Test
    public void constructor_shouldSetAllFields() {
        // Arrange
        String status = "400";
        String message = "Bad Request";
        String detail = "Required parameter 'id' is missing";
        String path = "/api/worker/jobs";

        // Act
        WorkerApiError error = new WorkerApiError(status, message, detail, path);

        // Assert
        assertEquals(status, error.getStatus());
        assertEquals(message, error.getMessage());
        assertEquals(detail, error.getDetail());
        assertEquals(path, error.getPath());
        assertNotNull(error.getTimestamp());
    }

    @Test
    public void constructor_shouldSetTimestampToCurrentTime() {
        // Arrange
        LocalDateTime before = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        // Act
        WorkerApiError error = new WorkerApiError("500", "Server Error", "Internal error", "/api/worker/status");
        LocalDateTime after = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        // Assert
        assertNotNull(error.getTimestamp());
        LocalDateTime timestamp = error.getTimestamp().truncatedTo(ChronoUnit.SECONDS);

        // The timestamp should be between or equal to the before and after times
        assertTrue(
                timestamp.isEqual(before) || timestamp.isAfter(before),
                "Timestamp should be equal to or after the time before object creation"
        );
        assertTrue(
                timestamp.isEqual(after) || timestamp.isBefore(after),
                "Timestamp should be equal to or before the time after object creation"
        );
    }

    @Test
    public void getters_shouldReturnCorrectValues() {
        // Arrange
        String status = "404";
        String message = "Not Found";
        String detail = "Resource with ID '123' does not exist";
        String path = "/api/worker/jobs/123";
        WorkerApiError error = new WorkerApiError(status, message, detail, path);

        // Act & Assert
        assertEquals(status, error.getStatus());
        assertEquals(message, error.getMessage());
        assertEquals(detail, error.getDetail());
        assertEquals(path, error.getPath());
    }

    @Test
    public void jsonFormatAnnotation_shouldBeAppliedToTimestampField() throws NoSuchFieldException {
        // Act
        java.lang.reflect.Field timestampField = WorkerApiError.class.getDeclaredField("timestamp");
        JsonFormat annotation = timestampField.getAnnotation(JsonFormat.class);

        // Assert
        assertNotNull(annotation, "JsonFormat annotation should be present on timestamp field");
        assertEquals(JsonFormat.Shape.STRING, annotation.shape());
        assertEquals("yyyy-MM-dd HH:mm:ss", annotation.pattern());
    }

    @Test
    public void constructWithNullValues_shouldAcceptNullValues() {
        // Act
        WorkerApiError error = new WorkerApiError(null, null, null, null);

        // Assert
        assertNull(error.getStatus());
        assertNull(error.getMessage());
        assertNull(error.getDetail());
        assertNull(error.getPath());
        assertNotNull(error.getTimestamp());
    }
}