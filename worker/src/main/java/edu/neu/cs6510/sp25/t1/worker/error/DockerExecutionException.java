package edu.neu.cs6510.sp25.t1.worker.error;

/**
 * Exception thrown when Docker execution fails.
 * This exception captures details about Docker container failures,
 * including the exit code returned by the Docker process.
 */
public class DockerExecutionException extends WorkerException {

  /**
   * The exit code returned by the Docker process.
   * A non-zero value indicates an error occurred during execution.
   */
  private final int exitCode;

  /**
   * Constructs a new DockerExecutionException with the specified detail message
   * and exit code.
   *
   * @param message the detail message (which is saved for later retrieval by the getMessage() method)
   * @param exitCode the exit code returned by the Docker process
   */
  public DockerExecutionException(String message, int exitCode) {
    super(message);
    this.exitCode = exitCode;
  }

  /**
   * Constructs a new DockerExecutionException with the specified detail message and cause.
   * The exit code is set to -1 to indicate an unknown or unspecified exit code.
   *
   * @param message the detail message (which is saved for later retrieval by the getMessage() method)
   * @param cause the cause (which is saved for later retrieval by the getCause() method)
   */
  public DockerExecutionException(String message, Throwable cause) {
    super(message, cause);
    this.exitCode = -1;
  }

  /**
   * Returns the exit code associated with this Docker execution failure.
   *
   * @return the exit code, or -1 if the exception was created with a cause rather than an exit code
   */
  public int getExitCode() {
    return exitCode;
  }
}