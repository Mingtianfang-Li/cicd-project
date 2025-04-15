package edu.neu.cs6510.sp25.t1.cli.commands;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;

import edu.neu.cs6510.sp25.t1.common.utils.GitCloneUtil;
import edu.neu.cs6510.sp25.t1.common.validation.error.ValidationException;
import edu.neu.cs6510.sp25.t1.backend.utils.YamlPipelineUtils;

import picocli.CommandLine;

/**
 * Implements the `check` command to validate a pipeline YAML file.
 */
@CommandLine.Command(name = "check", description = "Validates a pipeline configuration file without running it.")
public class CheckCommand implements Callable<Integer> {

  @CommandLine.Option(names = { "--file",
      "-f" }, description = "Path to the pipeline YAML configuration file.", required = true)
  private String filePath;

  /**
   * Validates a pipeline configuration file.
   *
   * @return 0 if the pipeline is valid, 1 if validation fails.
   */
  @Override
  public Integer call() {

    if (filePath == null) {
      System.err.println("[Error] File path cannot be null");
      return 1;
    }
    if (!filePath.endsWith(".yaml")) {
      System.err.println("[Error] File extension must be a YAML file");
      return 1;
    }
    if (!GitCloneUtil.isInsideGitRepo(new File(filePath))) {
      System.err.println("[Error] GitClone is not inside the git repo");
      return 1;
    }
    try {
      System.out.println("Checking pipeline configuration: " + filePath);

      Map<String, Object> pipelineConfig = YamlPipelineUtils.readPipelineYaml(filePath);

      YamlPipelineUtils.validatePipelineConfig(pipelineConfig);

      System.out.println("Pipeline configuration is valid!");
      return 0;
    } catch (ValidationException e) {
      System.err.println("[ERROR] Invalid pipeline: " + e.getMessage());
      return 1;
    } catch (Exception e) {
      System.err.println("[ERROR] Unexpected error: " + e.getMessage());
      return 1;
    }
  }
}
