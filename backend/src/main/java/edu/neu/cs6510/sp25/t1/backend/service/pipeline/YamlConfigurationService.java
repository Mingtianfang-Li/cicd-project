package edu.neu.cs6510.sp25.t1.backend.service.pipeline;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.stereotype.Service;

import edu.neu.cs6510.sp25.t1.backend.utils.YamlPipelineUtils;
import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;
import lombok.RequiredArgsConstructor;

/**
 * Service responsible for handling YAML configuration files for pipelines.
 * This includes resolving file paths, reading YAML files, and validating their contents.
 */
@Service
@RequiredArgsConstructor
public class YamlConfigurationService {

  /**
   * Resolves and validates the pipeline file path.
   *
   * @param filePath the file path from the request
   * @return the resolved path
   */
  public Path resolveAndValidatePipelinePath(String filePath) {
    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IllegalArgumentException("Pipeline file path cannot be null or empty");
    }
    
    Path resolvedPath = Paths.get(filePath);
    if (!resolvedPath.isAbsolute()) {
      resolvedPath = Paths.get(System.getProperty("user.dir")).resolve(filePath).normalize();
    }
    
    PipelineLogger.info("Corrected pipeline file path: " + resolvedPath.toAbsolutePath());
    
    // Validate file exists and is readable
    if (!Files.exists(resolvedPath)) {
      PipelineLogger.error("Pipeline configuration file not found: " + resolvedPath.toAbsolutePath());
      throw new IllegalArgumentException("Pipeline configuration file not found: " + resolvedPath.toAbsolutePath());
    }
    
    if (!Files.isReadable(resolvedPath)) {
      PipelineLogger.error("Pipeline configuration file is not readable: " + resolvedPath.toAbsolutePath());
      throw new IllegalArgumentException("Pipeline configuration file is not readable: " + resolvedPath.toAbsolutePath());
    }
    
    return resolvedPath;
  }

  /**
   * Parses and validates the pipeline YAML configuration.
   *
   * @param pipelinePath the path to the pipeline YAML file
   * @return the parsed pipeline configuration
   */
  public Map<String, Object> parseAndValidatePipelineYaml(String pipelinePath) {
    try {
      PipelineLogger.info("Attempting to read pipeline YAML...");
      Map<String, Object> pipelineConfig = YamlPipelineUtils.readPipelineYaml(pipelinePath);

      PipelineLogger.info("Successfully read pipeline YAML. Now validating...");
      YamlPipelineUtils.validatePipelineConfig(pipelineConfig);

      PipelineLogger.info("YAML validation completed for: " + pipelinePath);
      return pipelineConfig;
    } catch (Exception e) {
      PipelineLogger.error("ERROR reading pipeline YAML: " + e.getMessage());
      throw new RuntimeException("YAML parsing failed: " + e.getMessage(), e);
    }
  }
}