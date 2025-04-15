package edu.neu.cs6510.sp25.t1.backend.error;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Represents a standard error response body for APIs.
 * Used to return consistent error structure when exceptions occur.
 */
@Getter
public class ApiError {

    /** The HTTP status code associated with the error. */
    private HttpStatus status;

    /** The timestamp when the error occurred, formatted as yyyy-MM-dd HH:mm:ss. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /** A brief message describing the error. */
    private String message;

    /** Detailed information about the error. */
    private String detail;

    /**
     * Constructs an {@code ApiError} instance with the specified status, message, and detail.
     *
     * @param status  the HTTP status code
     * @param message a brief message describing the error
     * @param detail  detailed information about the error
     */
    public ApiError(HttpStatus status, String message, String detail) {
        this.status = status;
        this.message = message;
        this.detail = detail;
        this.timestamp = LocalDateTime.now();
    }
}