package edu.neu.cs6510.sp25.t1.common.validation.manager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;
import edu.neu.cs6510.sp25.t1.common.validation.error.ErrorHandler;
import edu.neu.cs6510.sp25.t1.common.validation.error.ValidationException;

/**
 * PipelineNameManager ensures that pipeline YAML file names are unique inside `.pipelines/`.
 * It provides methods to validate names and suggest alternative names if conflicts exist.
 */
public class PipelineNameManager {
  private final Path pipelinesDir = Paths.get(System.getProperty("user.dir"), ".pipelines");
  private final Set<String> existingFileNames;

  /**
   * Initializes the PipelineNameManager and loads existing YAML file names.
   *
   * @throws ValidationException If the `.pipelines/` directory is missing.
   */
  public PipelineNameManager() throws ValidationException {
    this.existingFileNames = new HashSet<>();
    loadExistingYamlFileNames();
  }

  /**
   * Loads existing YAML file names from the `.pipelines/` directory.
   *
   * @throws ValidationException If the `.pipelines/` directory does not exist.
   */
  private void loadExistingYamlFileNames() throws ValidationException {
    File directory = pipelinesDir.toFile();
    if (!directory.exists() || !directory.isDirectory()) {
      ErrorHandler.Location location = new ErrorHandler.Location(pipelinesDir.toString(), 1, 1, "directory");
      String errorMessage = ErrorHandler.formatValidationError(location, "Pipeline directory not found: " + pipelinesDir);
      PipelineLogger.error(errorMessage);
      throw new ValidationException(errorMessage);
    }

    File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".yaml") || name.toLowerCase().endsWith(".yml"));
    if (files != null) {
      for (File file : files) {
        existingFileNames.add(file.getName().toLowerCase());  // Store names as lowercase for case-insensitive checks
      }
      PipelineLogger.info("Loaded " + files.length + " pipeline YAML files from " + pipelinesDir);
    }
  }

  /**
   * Checks if a given pipeline name is unique inside `.pipelines/`.
   *
   * @param fileName The YAML file name to check.
   * @return {@code true} if the name is unique, {@code false} otherwise.
   */
  public boolean isPipelineNameUnique(String fileName) {
    boolean isUnique = !existingFileNames.contains(fileName.toLowerCase());
    PipelineLogger.debug("Checking uniqueness for '" + fileName + "': " + (isUnique ? "Unique" : "Duplicate"));
    return isUnique;
  }
}
