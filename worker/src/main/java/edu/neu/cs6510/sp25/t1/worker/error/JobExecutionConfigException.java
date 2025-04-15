package edu.neu.cs6510.sp25.t1.worker.error;

/**
 * Exception thrown when job configuration is invalid.
 * This exception is used to signal errors in job configurations that prevent
 * proper execution of worker tasks, such as missing required parameters,
 * invalid parameter values, or configuration conflicts.
 */
public class JobExecutionConfigException extends WorkerException {

    /**
     * Constructs a new JobExecutionConfigException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public JobExecutionConfigException(String message) {
        super(message);
    }

    /**
     * Constructs a new JobExecutionConfigException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public JobExecutionConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}