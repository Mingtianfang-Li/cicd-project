package edu.neu.cs6510.sp25.t1.worker.error;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Standard error response format for Worker API.
 * This class represents the standardized structure used when returning error responses
 * from the Worker API endpoints. It includes information about the error status,
 * occurrence time, error message, detailed explanation, and the API path that generated the error.
 */
@Getter
public class WorkerApiError {
    /**
     * The HTTP status code or custom status indicator for the error.
     */
    private String status;

    /**
     * The timestamp when the error occurred, formatted as a string in the pattern "yyyy-MM-dd HH:mm:ss".
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * A brief, human-readable message describing the error.
     */
    private String message;

    /**
     * Additional details providing more context about the error.
     */
    private String detail;

    /**
     * The API path (URI) that was being accessed when the error occurred.
     */
    private String path;

    /**
     * Constructs a new WorkerApiError with the specified error information.
     * The timestamp is automatically set to the current time when this constructor is called.
     *
     * @param status the HTTP status code or custom status indicator
     * @param message a brief description of the error
     * @param detail additional error details or context
     * @param path the API path that generated the error
     */
    public WorkerApiError(String status, String message, String detail, String path) {
        this.status = status;
        this.message = message;
        this.detail = detail;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}