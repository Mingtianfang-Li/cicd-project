package edu.neu.cs6510.sp25.t1.cli.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import edu.neu.cs6510.sp25.t1.backend.utils.YamlPipelineUtils;
import edu.neu.cs6510.sp25.t1.common.utils.GitCloneUtil;
import edu.neu.cs6510.sp25.t1.common.validation.error.ValidationException;

import picocli.CommandLine;

/**
 * Implements the `dry-run` command to validate a pipeline file
 * and print the execution order in structured YAML format.
 */
@CommandLine.Command(name = "dry-run", description = "Validates a pipeline file and prints execution order in YAML format.")
public class DryRunCommand implements Callable<Integer> {

  @CommandLine.Option(names = { "--file",
      "-f" }, description = "Path to the pipeline YAML configuration file.", required = true)
  private String filePath;

  /**
   * Sets the file path for the pipeline YAML configuration.
   * Primarily used for testing or programmatic invocation.
   *
   * @param filePath the path to the pipeline YAML file
   */
  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  /**
   * Executes the dry-run command.
   * Validates the pipeline YAML file specified by the user,
   * and prints a topologically sorted execution plan grouped by stages in YAML format.
   *
   * @return 0 if validation and formatting are successful, 1 otherwise.
   */
  @Override
  public Integer call() {
    if (filePath == null) {
      System.err.println("[Error] File path cannot be null");
      return 1;
    }
    if (!filePath.endsWith(".yaml") && !filePath.endsWith(".yml")) {
      System.err.println("[Error] File extension must be a YAML file");
      return 1;
    }
    // The following check may be optional depending on your requirements
    if (!GitCloneUtil.isInsideGitRepo(new File(filePath))) {
      System.err.println("[Error] GitClone is not inside the git repo");
      return 1;
    }

    try {
      System.out.println("Validating pipeline configuration: " + filePath);

      // Use YamlPipelineUtils to read and validate the pipeline
      Map<String, Object> pipelineConfig = YamlPipelineUtils.readPipelineYaml(filePath);
      YamlPipelineUtils.validatePipelineConfig(pipelineConfig);

      // Process the pipeline configuration to create a topologically sorted execution
      // plan
      Map<String, List<Map<String, Object>>> executionPlan = createExecutionPlan(pipelineConfig);

      System.out.println("\nExecution Plan:");
      printExecutionPlan(executionPlan);

      return 0;
    } catch (ValidationException e) {
      System.err.println("[ERROR] Invalid pipeline: " + e.getMessage());
      return 1;
    } catch (Exception e) {
      System.err.println("[ERROR] Unexpected error: " + e.getMessage());
      return 1;
    }
  }

  /**
   * Creates a topologically sorted execution plan from a pipeline YAML
   * configuration.
   * 
   * @param pipelineConfig The parsed pipeline YAML configuration.
   * @return A map of stage names to ordered lists of jobs.
   * @throws Exception If an error occurs while creating the execution plan.
   */
  @SuppressWarnings("unchecked")
  private Map<String, List<Map<String, Object>>> createExecutionPlan(Map<String, Object> pipelineConfig)
      throws Exception {
    Map<String, List<Map<String, Object>>> executionPlan = new LinkedHashMap<>();

    // Extract stages
    List<?> stages = (List<?>) pipelineConfig.get("stages");
    if (stages == null) {
      throw new Exception("Pipeline must have 'stages' defined");
    }

    // Build a map of stage names and extract jobs
    List<String> stageNames = new ArrayList<>();
    List<Map<String, Object>> allJobs = new ArrayList<>();
    Map<String, Map<String, Object>> stageMap = new HashMap<>();

    // Process stages and their jobs
    for (Object stageObj : stages) {
      if (stageObj instanceof Map) {
        Map<String, Object> stageMap2 = (Map<String, Object>) stageObj;
        String stageName = (String) stageMap2.get("name");
        stageNames.add(stageName);
        stageMap.put(stageName, stageMap2);

        // Check if this stage has jobs
        if (stageMap2.containsKey("jobs")) {
          List<Map<String, Object>> stageJobs = (List<Map<String, Object>>) stageMap2.get("jobs");
          for (Map<String, Object> job : stageJobs) {
            // Set the stage field on each job if not present
            if (!job.containsKey("stage")) {
              job.put("stage", stageName);
            }
            allJobs.add(job);
          }
        }
      } else if (stageObj instanceof String) {
        stageNames.add((String) stageObj);
      }
    }

    // Handle top-level jobs list if present
    List<Map<String, Object>> topLevelJobs = (List<Map<String, Object>>) pipelineConfig.get("jobs");
    if (topLevelJobs != null) {
      allJobs.addAll(topLevelJobs);
    }

    // If we have no jobs at all, return empty execution plan
    if (allJobs.isEmpty()) {
      for (String stageName : stageNames) {
        executionPlan.put(stageName, new ArrayList<>());
      }
      return executionPlan;
    }

    // Initialize the execution plan with empty lists for each stage
    for (String stageName : stageNames) {
      executionPlan.put(stageName, new ArrayList<>());
    }

    // Create a map of job name to job for easy reference
    Map<String, Map<String, Object>> jobMap = new HashMap<>();
    for (Map<String, Object> job : allJobs) {
      jobMap.put((String) job.get("name"), job);
    }

    // Group jobs by stage
    Map<String, List<Map<String, Object>>> jobsByStage = new HashMap<>();
    for (Map<String, Object> job : allJobs) {
      String stageName = (String) job.get("stage");
      if (!jobsByStage.containsKey(stageName)) {
        jobsByStage.put(stageName, new ArrayList<>());
      }
      jobsByStage.get(stageName).add(job);
    }

    // Perform topological sort on stages based on job dependencies (if we have
    // dependencies)
    List<String> sortedStages = topologicalSortStages(stageNames, allJobs, jobMap);

    // For each stage, perform topological sort on jobs
    for (String stageName : sortedStages) {
      List<Map<String, Object>> stageJobs = jobsByStage.getOrDefault(stageName, new ArrayList<>());
      if (!stageJobs.isEmpty()) {
        List<Map<String, Object>> sortedJobs = topologicalSortJobs(stageJobs, jobMap);
        executionPlan.put(stageName, sortedJobs);
      }
    }

    // Create final ordered execution plan
    Map<String, List<Map<String, Object>>> orderedPlan = new LinkedHashMap<>();
    for (String stageName : sortedStages) {
      if (executionPlan.containsKey(stageName)) {
        orderedPlan.put(stageName, executionPlan.get(stageName));
      }
    }

    return orderedPlan;
  }

  /**
   * Performs topological sort on stages based on job dependencies.
   * 
   * @param stageNames List of stage names.
   * @param allJobs    List of all jobs.
   * @param jobMap     Map of job name to job.
   * @return List of stage names in topological order.
   */
  @SuppressWarnings("unchecked")
  private List<String> topologicalSortStages(
      List<String> stageNames,
      List<Map<String, Object>> allJobs,
      Map<String, Map<String, Object>> jobMap) {

    // If there are no jobs or no stage names, return an empty list
    if (allJobs == null || allJobs.isEmpty() || stageNames == null || stageNames.isEmpty()) {
      return new ArrayList<>(stageNames);
    }

    // Create a map of stage dependencies
    Map<String, List<String>> stageDependencies = new HashMap<>();
    for (String stageName : stageNames) {
      stageDependencies.put(stageName, new ArrayList<>());
    }

    // Determine stage dependencies based on job dependencies
    boolean hasDependencies = false;
    for (Map<String, Object> job : allJobs) {
      String jobStage = (String) job.get("stage");
      if (job.containsKey("dependencies")) {
        List<String> dependencies = new ArrayList<>();
        Object depsObj = job.get("dependencies");

        if (depsObj instanceof String) {
          dependencies.add((String) depsObj);
          hasDependencies = true;
        } else if (depsObj instanceof List && !((List<?>) depsObj).isEmpty()) {
          dependencies.addAll((List<String>) depsObj);
          hasDependencies = true;
        }

        for (String depName : dependencies) {
          Map<String, Object> depJob = jobMap.get(depName);
          if (depJob != null) {
            String depStage = (String) depJob.get("stage");
            if (!depStage.equals(jobStage) && !stageDependencies.get(jobStage).contains(depStage)) {
              stageDependencies.get(jobStage).add(depStage);
            }
          }
        }
      }
    }

    // If no dependencies, just return stages in original order
    if (!hasDependencies) {
      return new ArrayList<>(stageNames);
    }

    // Perform topological sort on stages
    List<String> result = new ArrayList<>();
    Set<String> visited = new HashSet<>();
    Set<String> processing = new HashSet<>();

    for (String stageName : stageNames) {
      if (!visited.contains(stageName)) {
        dfsStageSort(stageName, stageDependencies, visited, processing, result);
      }
    }

    return result;
  }

  /**
   * DFS helper for topological sorting of stages.
   * 
   * @param stageName         The name of the current stage.
   * @param stageDependencies Map of stage dependencies.
   * @param visited           Set of visited stages.
   * @param processing        Set of currently processing stages.
   * @param result            List to store the sorted stages.
   * @throws RuntimeException If a cyclic dependency is detected.
   */
  private void dfsStageSort(
      String stageName,
      Map<String, List<String>> stageDependencies,
      Set<String> visited,
      Set<String> processing,
      List<String> result) {

    if (processing.contains(stageName)) {
      throw new RuntimeException("Cyclic dependency detected between stages with stage: " + stageName);
    }

    if (visited.contains(stageName)) {
      return;
    }

    processing.add(stageName);

    // Process stage dependencies
    for (String depStage : stageDependencies.getOrDefault(stageName, new ArrayList<>())) {
      dfsStageSort(depStage, stageDependencies, visited, processing, result);
    }

    processing.remove(stageName);
    visited.add(stageName);
    result.add(stageName);
  }

  /**
   * Performs topological sort on jobs within a stage.
   * 
   * @param jobs   List of jobs in the stage.
   * @param jobMap Map of job name to job.
   * @return List of jobs in topological order.
   */
  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> topologicalSortJobs(
      List<Map<String, Object>> jobs,
      Map<String, Map<String, Object>> jobMap) {

    // If there are no jobs, return an empty list
    if (jobs == null || jobs.isEmpty()) {
      return new ArrayList<>();
    }

    // Check if any job in this stage has dependencies
    boolean hasDependencies = false;
    for (Map<String, Object> job : jobs) {
      if (job.containsKey("dependencies")) {
        Object deps = job.get("dependencies");
        if (deps instanceof String || (deps instanceof List && !((List<?>) deps).isEmpty())) {
          hasDependencies = true;
          break;
        }
      }
    }

    // If no dependencies, just return jobs in original order
    if (!hasDependencies) {
      return new ArrayList<>(jobs);
    }

    List<Map<String, Object>> result = new ArrayList<>();
    Set<String> visited = new HashSet<>();
    Set<String> processing = new HashSet<>();

    for (Map<String, Object> job : jobs) {
      String jobName = (String) job.get("name");
      if (!visited.contains(jobName)) {
        dfsJobSort(job, jobMap, visited, processing, result);
      }
    }

    return result;
  }

  /**
   * DFS helper for topological sorting of jobs.
   * 
   * @param job        The current job.
   * @param jobMap     Map of job name to job.
   * @param visited    Set of visited jobs.
   * @param processing Set of currently processing jobs.
   * @param result     List to store the sorted jobs.
   * @throws RuntimeException If a cyclic dependency is detected.
   */
  @SuppressWarnings("unchecked")
  private void dfsJobSort(
      Map<String, Object> job,
      Map<String, Map<String, Object>> jobMap,
      Set<String> visited,
      Set<String> processing,
      List<Map<String, Object>> result) {

    if (job == null) {
      return;
    }

    String jobName = (String) job.get("name");
    if (jobName == null) {
      return;
    }

    String stageName = (String) job.get("stage");
    if (stageName == null) {
      stageName = ""; // Default to empty string to avoid NPE
    }

    if (processing.contains(jobName)) {
      throw new RuntimeException("Cyclic dependency detected for job: " + jobName);
    }

    if (visited.contains(jobName)) {
      return;
    }

    processing.add(jobName);

    // Process job dependencies
    if (job.containsKey("dependencies")) {
      List<String> dependencies = new ArrayList<>();
      Object depsObj = job.get("dependencies");

      if (depsObj instanceof String) {
        dependencies.add((String) depsObj);
      } else if (depsObj instanceof List) {
        for (Object dep : (List<?>) depsObj) {
          if (dep instanceof String) {
            dependencies.add((String) dep);
          }
        }
      }

      for (String depName : dependencies) {
        Map<String, Object> depJob = jobMap.get(depName);
        if (depJob != null) {
          String depStage = (String) depJob.get("stage");

          // Only process dependencies within the same stage here
          if (depStage != null && depStage.equals(stageName)) {
            dfsJobSort(depJob, jobMap, visited, processing, result);
          }
        }
      }
    }

    processing.remove(jobName);
    visited.add(jobName);
    result.add(job);
  }

  /**
   * Prints the execution plan in YAML format.
   * 
   * @param executionPlan The execution plan to print.
   * @throws Exception           If an error occurs while printing the execution
   *                             plan.
   * @throws ValidationException If the execution plan is invalid.
   * @throws RuntimeException    If a cyclic dependency is detected.
   */
  @SuppressWarnings("unchecked")
  private void printExecutionPlan(Map<String, List<Map<String, Object>>> executionPlan) {
    StringBuilder yamlOutput = new StringBuilder();

    // Output plan as YAML with stage names as keys and job names as subkeys
    for (Map.Entry<String, List<Map<String, Object>>> stageEntry : executionPlan.entrySet()) {
      String stageName = stageEntry.getKey();
      List<Map<String, Object>> jobs = stageEntry.getValue();

      yamlOutput.append(stageName).append(":\n");

      if (jobs == null || jobs.isEmpty()) {
        continue; // Skip empty stages
      }

      for (Map<String, Object> job : jobs) {
        if (job == null) {
          continue; // Skip null jobs
        }

        String jobName = (String) job.get("name");
        if (jobName == null) {
          continue; // Skip jobs without names
        }

        // Find image (supporting multiple field names)
        Object imageObj = null;
        for (String fieldName : new String[] { "dockerImage", "image", "docker-image", "docker_image" }) {
          if (job.containsKey(fieldName)) {
            imageObj = job.get(fieldName);
            break;
          }
        }
        String image = imageObj != null ? imageObj.toString() : "alpine:latest"; // Default to alpine if not specified

        yamlOutput.append("  ").append(jobName).append(":\n");
        yamlOutput.append("    image: ").append(image).append("\n");
        yamlOutput.append("    script:\n");

        // Handle script which might be a string or a list
        Object scriptObj = job.get("script");
        if (scriptObj == null) {
          // If no script, add an echo command as placeholder
          yamlOutput.append("      - echo \"No script specified\"\n");
        } else if (scriptObj instanceof String) {
          String scriptStr = (String) scriptObj;
          if (scriptStr.contains("\n")) {
            // Multiline string - split on newlines
            String[] lines = scriptStr.split("\n");
            for (String line : lines) {
              yamlOutput.append("      - ").append(line.trim()).append("\n");
            }
          } else {
            // Single line
            yamlOutput.append("      - ").append(scriptStr).append("\n");
          }
        } else if (scriptObj instanceof List) {
          List<?> scriptLines = (List<?>) scriptObj;
          if (scriptLines.isEmpty()) {
            yamlOutput.append("      - echo \"Empty script\"\n");
          } else {
            for (Object scriptLine : scriptLines) {
              if (scriptLine != null) {
                yamlOutput.append("      - ").append(scriptLine).append("\n");
              }
            }
          }
        }
      }
    }

    System.out.println(yamlOutput);
  }
}
