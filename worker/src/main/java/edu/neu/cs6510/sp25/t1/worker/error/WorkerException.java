package edu.neu.cs6510.sp25.t1.worker.error;

/**
 * Base exception class for worker-related errors.
 * This class serves as the parent exception for all worker-specific exceptions,
 * providing a common type that can be caught to handle any worker error.
 * It extends RuntimeException to be unchecked, allowing for more flexible
 * exception handling in the worker service.
 */
public class WorkerException extends RuntimeException {

    /**
     * Constructs a new worker exception with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public WorkerException(String message) {
        super(message);
    }

    /**
     * Constructs a new worker exception with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public WorkerException(String message, Throwable cause) {
        super(message, cause);
    }
}