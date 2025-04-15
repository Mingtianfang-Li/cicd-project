package edu.neu.cs6510.sp25.t1.worker.error;

/**
 * Exception thrown when communication with the backend service fails.
 * This exception is typically used when the worker service is unable to
 * reach or successfully interact with the backend.
 */
public class BackendCommunicationException extends WorkerException {

    /**
     * Constructs a new BackendCommunicationException with the specified detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public BackendCommunicationException(String message) {
        super(message);
    }

    /**
     * Constructs a new BackendCommunicationException with the specified detail message and cause.
     *
     * @param message the detail message describing the cause of the exception
     * @param cause   the underlying cause of the exception
     */
    public BackendCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}

