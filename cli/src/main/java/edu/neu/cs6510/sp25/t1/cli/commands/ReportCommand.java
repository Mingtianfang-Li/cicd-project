package edu.neu.cs6510.sp25.t1.cli.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.neu.cs6510.sp25.t1.cli.api.CliBackendClient;
import edu.neu.cs6510.sp25.t1.cli.service.K8sService;
import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;
import picocli.CommandLine;

/**
 * Implements the `report` command to fetch past pipeline execution reports using the backend API.
 */
@CommandLine.Command(
        name = "report",
        description = "Retrieves pipeline execution reports from the CI/CD backend."
)

/*
 * CLI command that retrieves and displays pipeline execution reports from the backend service.
 * Supports fetching full pipeline histories, stage/job summaries, and formatting the results
 * in either JSON or human-readable text.
 *
 * Used with the `report` subcommand in the CLI.
 */
public class ReportCommand implements Callable<Integer> {

  @CommandLine.Option(names = {"--pipeline", "-p"}, description = "Specify the pipeline name.", required = true)
  private String pipelineName;

  @CommandLine.Option(names = {"--run"}, description = "Specify the run number for a detailed report.")
  private Integer runNumber;

  @CommandLine.Option(names = {"--stage"}, description = "Specify the stage name for a detailed report.")
  private String stageName;

  @CommandLine.Option(names = {"--job"}, description = "Specify the job name for a detailed report.")
  private String jobName;

  @CommandLine.Option(names = {"--format"}, description = "Output format (text, json).", defaultValue = "text")
  private String format;

  private final CliBackendClient backendClient = new CliBackendClient("http://localhost:8080");
  private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  /**
   * Main entry point for the `report` command.
   *
   * @return 0 if successful, 1 if API request failed
   */
  @Override
  public Integer call() {
    try {
      // If job is specified but stage is not, that's an error
      if (jobName != null && stageName == null) {
        System.err.println("Error: --stage parameter is required when using --job");
        return 1;
      }

      // Now handle various parameter combinations
      if (runNumber != null) {
        // We have a run number specified
        if (stageName != null) {
          if (jobName != null) {
            return fetchJobSummary();
          } else {
            return fetchStageSummary();
          }
        } else {
          return fetchPipelineRunSummary();
        }
      } else {
        // No run number, but we might have stage or job
        if (stageName != null) {
          return fetchStageHistory();
        } else {
          return fetchPipelineHistory();
        }
      }
    } catch (IOException e) {
      PipelineLogger.error("API request failed: " + e.getMessage());
      return 1;
    } catch (Exception e) {
      PipelineLogger.error("Error: " + e.getMessage());
      return 1;
    }
  }

  /**
   * Fetch past runs of a specific pipeline.
   *
   * @return 0 if successful, 1 if API request failed
   */
  private Integer fetchPipelineHistory() throws IOException {
    PipelineLogger.info("Fetching past runs for pipeline: " + pipelineName);
    String podName = K8sService.startBackendEnvironment(pipelineName);
    String response = backendClient.fetchPipelineReport(pipelineName, -1, null, null);

    // Format and print the response
    String formattedResponse = formatResponse(response);

    K8sService.stopPortForward();
    K8sService.stopPod(podName);
    System.out.println(formattedResponse);
    return 0;
  }

  /**
   * Fetch summary of a specific pipeline run.
   *
   * @return 0 if successful, 1 if API request failed
   * @throws IOException if API request fails
   */
  private Integer fetchPipelineRunSummary() throws IOException {
    PipelineLogger.info("Fetching run summary for pipeline: " + pipelineName + ", Run: " + runNumber);
    String podName = K8sService.startBackendEnvironment(pipelineName);
    String response = backendClient.fetchPipelineReport(pipelineName, runNumber, null, null);

    K8sService.stopPortForward();
    K8sService.stopPod(podName);
    // Format and print the response
    String formattedResponse = formatResponse(response);
    System.out.println(formattedResponse);
    return 0;
  }

  /**
   * Fetch summary of a specific stage in a pipeline run.
   *
   * @return 0 if successful, 1 if API request failed
   * @throws IOException if API request fails
   */
  private Integer fetchStageSummary() throws IOException {
    PipelineLogger.info("Fetching stage summary for pipeline: " + pipelineName + ", Run: " + runNumber + ", Stage: " + stageName);
    String podName = K8sService.startBackendEnvironment(pipelineName);
    String response = backendClient.fetchPipelineReport(pipelineName, runNumber, stageName, null);

    K8sService.stopPortForward();
    K8sService.stopPod(podName);
    // Format and print the response
    String formattedResponse = formatResponse(response);
    System.out.println(formattedResponse);
    return 0;
  }

  /**
   * Fetch summary of a specific job in a pipeline stage.
   *
   * @return 0 if successful, 1 if API request failed
   * @throws IOException if API request fails
   */
  private Integer fetchJobSummary() throws IOException {
    PipelineLogger.info("Fetching job summary for pipeline: " + pipelineName + ", Run: " + runNumber + ", Stage: " + stageName + ", Job: " + jobName);
    String podName = K8sService.startBackendEnvironment(pipelineName);
    String response = backendClient.fetchPipelineReport(pipelineName, runNumber, stageName, jobName);

    K8sService.stopPortForward();
    K8sService.stopPod(podName);
    // Format and print the response
    String formattedResponse = formatResponse(response);
    System.out.println(formattedResponse);
    return 0;
  }

  /**
   * Fetch history of a specific stage across all pipeline runs.
   * This method also handles the case when a job name is specified.
   *
   * @return 0 if successful, 1 if API request failed
   * @throws IOException if API request fails
   */
  private Integer fetchStageHistory() throws IOException {
    if (jobName != null) {
      PipelineLogger.info("Fetching history for job: " + jobName + " in stage: " + stageName + " in pipeline: " + pipelineName);
    } else {
      PipelineLogger.info("Fetching history for stage: " + stageName + " in pipeline: " + pipelineName);
    }

    String podName = K8sService.startBackendEnvironment(pipelineName);
    // Pass both stageName and jobName to backend client
    String response = backendClient.fetchPipelineReport(pipelineName, null, stageName, jobName);

    K8sService.stopPortForward();
    K8sService.stopPod(podName);
    // Format and print the response
    String formattedResponse = formatResponse(response);
    System.out.println(formattedResponse);
    return 0;
  }

  /**
   * Format the response based on the format option.
   *
   * @param jsonResponse The JSON response from the API
   * @return The formatted response
   */
  private String formatResponse(String jsonResponse) {
    if (format.equalsIgnoreCase("json")) {
      try {
        // Parse the JSON
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // For array of objects (likely pipeline reports)
        if (rootNode.isArray() && rootNode.size() > 0) {
          ArrayNode modifiedRootNode = objectMapper.createArrayNode();

          for (JsonNode node : rootNode) {
            ObjectNode modifiedNode = (ObjectNode) node.deepCopy();

            // Remove null pipelineName field
            if (modifiedNode.has("pipelineName") && modifiedNode.get("pipelineName").isNull()) {
              modifiedNode.remove("pipelineName");
            }

            ((ArrayNode) modifiedRootNode).add(modifiedNode);
          }

          // Use the modified node
          return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(modifiedRootNode);
        } else if (rootNode.isObject()) {
          // For single object
          ObjectNode modifiedNode = (ObjectNode) rootNode.deepCopy();

          // Remove null pipelineName field
          if (modifiedNode.has("pipelineName") && modifiedNode.get("pipelineName").isNull()) {
            modifiedNode.remove("pipelineName");
          }

          return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(modifiedNode);
        }

        // If we couldn't modify the JSON, just pretty print it
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
      } catch (Exception e) {
        PipelineLogger.warn("Failed to process JSON: " + e.getMessage());
        return jsonResponse; // Return the original JSON if parsing fails
      }
    } else if (format.equalsIgnoreCase("text")) {
      try {
        // For text format, convert JSON to a more readable text format
        return convertJsonToText(jsonResponse);
      } catch (Exception e) {
        PipelineLogger.warn("Failed to convert JSON to text: " + e.getMessage());
        return jsonResponse; // Return the original JSON if conversion fails
      }
    } else {
      // Default to original response for unknown formats
      return jsonResponse;
    }
  }

  /**
   * Convert JSON response to a more readable text format.
   *
   * @param jsonResponse The JSON response
   * @return A formatted text representation
   */
  private String convertJsonToText(String jsonResponse) throws IOException {
    JsonNode rootNode = objectMapper.readTree(jsonResponse);
    StringBuilder sb = new StringBuilder();

    if (rootNode.isArray()) {
      // Handle array of objects
      for (JsonNode node : rootNode) {
        // Determine what type of object we're dealing with
        if (node.has("stages") && node.has("name")) {
          // Full pipeline report
          formatPipelineNode(sb, node);
        } else if (node.has("jobs") && node.has("name")) {
          // Stage report
          formatStageNode(sb, node, true);
        } else if (node.has("executions") && node.has("name")) {
          // Job report
          formatJobNode(sb, node, true);
        }
        sb.append("\n----------------------------------------\n");
      }
    } else if (rootNode.has("name") && rootNode.has("stages")) {
      // Handle single pipeline report
      formatPipelineNode(sb, rootNode);
    } else if (rootNode.has("name") && rootNode.has("jobs")) {
      // Handle stage report
      formatStageNode(sb, rootNode, true);
    } else if (rootNode.has("name") && rootNode.has("executions")) {
      // Handle job report
      formatJobNode(sb, rootNode, true);
    } else {
      // Fallback for unknown structures
      return jsonResponse;
    }

    return sb.toString();
  }


  /**
   * Format a pipeline node into text representation.
   *
   * @param sb The StringBuilder to append to
   * @param pipelineNode The pipeline JsonNode
   */
  private void formatPipelineNode(StringBuilder sb, JsonNode pipelineNode) {
    sb.append("Pipeline ID: ").append(pipelineNode.path("id").asText()).append("\n");
    sb.append("Pipeline Name: ").append(pipelineNode.path("name").asText()).append("\n");
    sb.append("Run Number: ").append(pipelineNode.path("runNumber").asText()).append("\n");

    // Add commit hash even if null
    sb.append("Commit Hash: ").append(
            pipelineNode.path("commitHash").isNull() ? "N/A" : pipelineNode.path("commitHash").asText()
    ).append("\n");

    sb.append("Status: ").append(pipelineNode.path("status").asText()).append("\n");

    // Format timestamps
    String startTime = formatTimestamp(pipelineNode.path("startTime").asText());
    String completionTime = pipelineNode.path("completionTime").isNull() ?
            "In Progress" : formatTimestamp(pipelineNode.path("completionTime").asText());

    sb.append("Start Time: ").append(startTime).append("\n");
    sb.append("Completion Time: ").append(completionTime).append("\n");

    // Include pipelineName if present (might be redundant but preserving all data)
    if (pipelineNode.has("pipelineName") && !pipelineNode.path("pipelineName").isNull()) {
      sb.append("Pipeline Reference Name: ").append(pipelineNode.path("pipelineName").asText()).append("\n");
    }

    sb.append("\n");

    // Process stages
    JsonNode stagesNode = pipelineNode.path("stages");
    if (stagesNode.isArray()) {
      sb.append("Stages:\n");
      for (JsonNode stageNode : stagesNode) {
        formatStageNode(sb, stageNode, false);
      }
    }
  }

  /**
   * Format a stage node into text representation.
   *
   * @param sb The StringBuilder to append to
   * @param stageNode The stage JsonNode
   * @param isDetailed Whether to include detailed information
   */
  private void formatStageNode(StringBuilder sb, JsonNode stageNode, boolean isDetailed) {
    String indent = isDetailed ? "" : "  ";

    sb.append(indent).append("Stage ID: ").append(stageNode.path("id").asText()).append("\n");
    sb.append(indent).append("Stage Name: ").append(stageNode.path("name").asText()).append("\n");
    sb.append(indent).append("Status: ").append(stageNode.path("status").asText()).append("\n");

    // Try to get pipeline name from jobs if available
    String pipelineName = "N/A";
    JsonNode jobsNode = stageNode.path("jobs");
    if (jobsNode.isArray() && jobsNode.size() > 0) {
      JsonNode firstJob = jobsNode.get(0);
      if (firstJob.has("pipelineName") && !firstJob.path("pipelineName").isNull()) {
        pipelineName = firstJob.path("pipelineName").asText();
      }
    }

    // Display pipeline name at the top level for stage report
    if (isDetailed) {
      sb.append(indent).append("Pipeline Name: ").append(pipelineName).append("\n");
    }

    // Add timestamps
    String startTime = formatTimestamp(stageNode.path("startTime").asText());
    String completionTime = stageNode.path("completionTime").isNull() ?
            "In Progress" : formatTimestamp(stageNode.path("completionTime").asText());

    sb.append(indent).append("Start Time: ").append(startTime).append("\n");
    sb.append(indent).append("Completion Time: ").append(completionTime).append("\n");

    // Process jobs in stage
    if (jobsNode.isArray() && jobsNode.size() > 0) {
      sb.append(indent).append("Jobs:\n");

      for (JsonNode jobNode : jobsNode) {
        // For detailed stage view, don't repeat pipeline name in each job
        formatJobNode(sb, jobNode, true, indent + "  ", isDetailed);
      }
    }
  }


  /**
   * Format a job node into text representation.
   *
   * @param sb The StringBuilder to append to
   * @param jobNode The job JsonNode
   * @param isDetailed Whether to include detailed information
   * @param indent The indentation to use
   * @param skipPipelineName Whether to skip displaying pipeline name (to avoid repetition)
   */
  private void formatJobNode(StringBuilder sb, JsonNode jobNode, boolean isDetailed, String indent, boolean skipPipelineName) {
    String jobName = jobNode.path("name").asText();

    sb.append(indent).append("Job Name: ").append(jobName).append("\n");

    // Include all relevant job metadata
    if (!skipPipelineName && jobNode.has("pipelineName")) {
      sb.append(indent).append("Pipeline Name: ").append(
              jobNode.path("pipelineName").isNull() ? "N/A" : jobNode.path("pipelineName").asText()
      ).append("\n");
    }

    if (jobNode.has("runNumber")) {
      sb.append(indent).append("Run Number: ").append(jobNode.path("runNumber").asText()).append("\n");
    }

    if (jobNode.has("stageName")) {
      sb.append(indent).append("Stage Name: ").append(jobNode.path("stageName").asText()).append("\n");
    }

    if (jobNode.has("commitHash")) {
      sb.append(indent).append("Commit Hash: ").append(
              jobNode.path("commitHash").isNull() ? "N/A" : jobNode.path("commitHash").asText()
      ).append("\n");
    }

    sb.append(indent).append("Executions:\n");

    // Process executions
    JsonNode executionsNode = jobNode.path("executions");
    if (executionsNode.isArray() && executionsNode.size() > 0) {
      for (JsonNode executionNode : executionsNode) {
        String execIndent = indent + "  ";
        sb.append(execIndent).append("Execution ID: ").append(executionNode.path("id").asText()).append("\n");
        sb.append(execIndent).append("Status: ").append(executionNode.path("status").asText()).append("\n");
        sb.append(execIndent).append("Allow Failure: ").append(executionNode.path("allowFailure").asBoolean()).append("\n");

        // Add timestamps for all executions
        String execStartTime = formatTimestamp(executionNode.path("startTime").asText());
        String execCompletionTime = executionNode.path("completionTime").isNull() ?
                "In Progress" : formatTimestamp(executionNode.path("completionTime").asText());

        sb.append(execIndent).append("Start Time: ").append(execStartTime).append("\n");
        sb.append(execIndent).append("Completion Time: ").append(execCompletionTime).append("\n");
        sb.append("\n");
      }
    }
  }

  /**
   * Format a job node into text representation.
   * Overloaded method for compatibility with existing calls.
   *
   * @param sb The StringBuilder to append to
   * @param jobNode The job JsonNode
   * @param isDetailed Whether to include detailed information
   */
  private void formatJobNode(StringBuilder sb, JsonNode jobNode, boolean isDetailed) {
    formatJobNode(sb, jobNode, isDetailed, "", false);
  }
  /**
   * Format an ISO timestamp to a more readable format.
   *
   * @param timestamp The ISO timestamp string
   * @return A formatted timestamp string
   */
  private String formatTimestamp(String timestamp) {
    if (timestamp == null || timestamp.isEmpty()) {
      return "N/A";
    }

    try {
      Instant instant = Instant.parse(timestamp);
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
              .withZone(ZoneId.systemDefault());
      return formatter.format(instant);
    } catch (Exception e) {
      return timestamp; // Return the original if parsing fails
    }
  }
}