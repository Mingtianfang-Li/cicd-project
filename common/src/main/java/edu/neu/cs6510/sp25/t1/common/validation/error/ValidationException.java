package edu.neu.cs6510.sp25.t1.common.validation.error;

import java.util.List;

/**
 * Exception thrown when a validation error occurs during pipeline validation.
 * <p>
 * Supports:
 * - **Single Validation Errors**: Captures a single message.
 * - **Multiple Errors**: Captures multiple validation errors as a list.
 * - **Error Location Tracking**: Uses `ErrorHandler.Location` for precise error reporting.
 * <p>
 * Usage:
 * - `throw new ValidationException("Pipeline name is missing.");`
 * - `throw new ValidationException(errorsList);` (for multiple errors)
 * - `throw new ValidationException("pipeline.yaml", 12, "Invalid stage reference.");`
 */
public class ValidationException extends Exception {

  /**
   * Constructs a ValidationException with a single error message.
   *
   * @param message The validation error message.
   */
  public ValidationException(String message) {
    super(message);
  }

  /**
   * Constructs a ValidationException with a list of error messages.
   *
   * @param errors The list of validation errors.
   */
  public ValidationException(List<String> errors) {
    super(String.join("\n", errors));
  }

  /**
   * Constructs a ValidationException with an error message and location.
   * @param filename The filename where the validation error occurred.
   * @param line The line number where the validation error occurred.
   * @param column The column number where the validation error occurred.
   * @param message The error message.
   */
  public ValidationException(String filename, int line, int column, String message) {
    super(String.format("%s:%d:%d: %s", filename, line, column, message));
  }

  /**
   * Constructs a ValidationException with an error message and location.
   *
   * @param location The location where the validation error occurred.
   * @param message  The error message.
   */
  public ValidationException(ErrorHandler.Location location, String message) {
    super(ErrorHandler.formatValidationError(location, message));
  }
}
