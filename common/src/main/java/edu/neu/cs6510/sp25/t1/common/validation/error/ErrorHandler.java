package edu.neu.cs6510.sp25.t1.common.validation.error;

import lombok.Getter;

/**
 * Utility for structured error handling in validation and YAML parsing.
 * <p>
 * Provides:
 * - **Structured error locations** (`ErrorHandler.Location`).
 * - **Standardized error formatting** for validation and parsing issues.
 * - **Consistent logging for debugging**.
 */
public class ErrorHandler {

  /**
   * Represents a specific location in a YAML file.
   */
  public static class Location {
    private final String filename;
    private final int line;
    private final int column;

    @Getter
    private final String path;

    /**
     * Constructs a Location object.
     *
     * @param filename The file where the error occurred.
     * @param line     The line number of the error.
     * @param column   The column number of the error.
     * @param path     The YAML path to the error location.
     */
    public Location(String filename, int line, int column, String path) {
      this.filename = filename;
      this.line = line;
      this.column = column;
      this.path = path;
    }
  }

  /**
   * Formats an error message with type and location.
   *
   * @param location The location where the error occurred.
   * @param message  The error message.
   * @return The formatted error message.
   */
  private static String formatError(Location location, String message) {
    return String.format("%s:%d:%d: %s", location.filename, location.line, location.column, message);
  }

  /**
   * Formats a validation error.
   *
   * @param location The location of the error.
   * @param message  The validation error message.
   * @return A formatted validation error message.
   */
  public static String formatValidationError(Location location, String message) {
    return formatError(location, message);
  }

}
