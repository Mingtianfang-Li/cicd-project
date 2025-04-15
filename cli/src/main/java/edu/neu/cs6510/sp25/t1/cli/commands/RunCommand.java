package edu.neu.cs6510.sp25.t1.cli.commands;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.neu.cs6510.sp25.t1.cli.CliApp;
import edu.neu.cs6510.sp25.t1.cli.service.K8sService;
import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;
import edu.neu.cs6510.sp25.t1.common.utils.GitCloneUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import picocli.CommandLine;

/*
 * CLI command that validates and runs a CI/CD pipeline.
 * Supports local or Kubernetes-based execution, and can optionally perform a dry-run.
 * Uses backend APIs to trigger the pipeline execution and forwards the results to the user.
 *
 * Used with the `run` subcommand in the CLI.
 */
@CommandLine.Command(name = "run", description = "Runs a CI/CD pipeline after validating its configuration.")
public class RunCommand implements Callable<Integer> {

  private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
  private static final String BACKEND_URL = "http://localhost:8080/api/pipeline/run"; // Unified URL

  @CommandLine.ParentCommand
  private CliApp parent; // Inherits global CLI options

  @CommandLine.Option(names = { "--repo", "-r" }, description = "Specify the repository URL.")
  private String repo;

  @CommandLine.Option(names = { "--branch", "-b" }, description = "Specify the Git branch.", defaultValue = "main")
  private String branch;

  @CommandLine.Option(names = { "--commit", "-c" }, description = "Specify the commit hash. Defaults to latest commit.")
  private String commit;

  @CommandLine.Option(names = { "-f", "--file" }, description = "Specify the pipeline config file.")
  private String filePath;

  @CommandLine.Option(names = { "--pipeline", "-p" }, description = "Specify the pipeline name to run.")
  private String pipeline;

  @CommandLine.Option(names = { "--local" }, description = "Run the pipeline locally.")
  private boolean localRun;
  
  @CommandLine.Option(names = { "--dry-run" }, description = "Validate the pipeline and print execution order without running.")
  private boolean dryRun;

  /**
   * Validates the pipeline configuration file and runs the pipeline.
   *
   * @return 0 if successful, 1 if an error occurred.
   */
  @Override
  public Integer call() {
    try {
      // Ensure a valid file path is provided when running locally
      if (localRun || dryRun) {
        if ((repo == null) && (filePath == null || filePath.isEmpty())) {
          PipelineLogger.error("Pipeline configuration file must be specified when running locally or using dry-run (-f).");
          return 1;
        } else if (repo == null && filePath != null) {
          File pipelineFile = new File(filePath);
          PipelineLogger.info("parse file path to repo");
          if (GitCloneUtil.isInsideGitRepo(pipelineFile)) {
            this.repo = GitCloneUtil.getRepoUrlFromFile(pipelineFile);
            PipelineLogger.info("Git clone repository url: " + this.repo);
          } else {
            PipelineLogger.error("Git clone repository url is not inside git repo");
          }
        }
      }
      
      // If dry-run is requested, delegate to the DryRunCommand
      if (dryRun) {
        DryRunCommand dryRunCmd = new DryRunCommand();
        dryRunCmd.setFilePath(filePath);
        return dryRunCmd.call();
      }

      return triggerPipelineExecution();

    } catch (Exception e) {
      PipelineLogger.error("Execution Error:" + e.getMessage());
      return 1;
    }
  }

  /**
   * Triggers a pipeline execution via the backend API.
   *
   * @return 0 if successful, 1 if an error occurred.
   */
  private Integer triggerPipelineExecution() {
    try {

      String pipelineName = extractRepoName(repo);
      // Start the full K8s CI/CD environment (using emptyDir)
      PipelineLogger.info("start k8s environment");
      K8sService.startCicdEnvironment(pipelineName);

      PipelineLogger.info("starts forwarding 8080");

      // Port-forward backend
      K8sService.portForwardBackendService();

      waitForBackendToBeAvailable();

      String jsonPayload = String.format(
          "{\"repo\": \"%s\", \"branch\": \"%s\", \"commit\": \"%s\", \"pipeline\": \"%s\", \"filePath\": \"%s\", \"local\": %s}",
          (repo != null ? repo : ""), branch, commit, (pipeline != null ? pipeline : ""), repo, localRun);

      PipelineLogger.debug("Sending request to backend: " + BACKEND_URL);
      PipelineLogger.debug("Payload: " + jsonPayload);

      Request request = createPostRequest(BACKEND_URL, jsonPayload);
      Response response = HTTP_CLIENT.newCall(request).execute();

      if (!response.isSuccessful()) {
        String responseBody = response.body() != null ? response.body().string() : "Empty response";
        PipelineLogger.error("Failed pipeline execution.");
        PipelineLogger.error("HTTP Status: " + response.code());
        PipelineLogger.error("Response: " + responseBody);
        return 1;
      }

      PipelineLogger.info("Pipeline execution started.");
      PipelineLogger.info("Response: " + response.body().string());
      K8sService.stopPortForward();
      return 0;

    } catch (IOException e) {
      PipelineLogger.error("Failed to communicate with backend: " + e.getMessage());
      K8sService.stopPortForward();
      return 1;
      // } catch (InterruptedException e) {
      // throw new RuntimeException(e);
      // }
    }

  }

  /**
   * Creates a POST request with the given payload.
   *
   * @param url     The request URL.
   * @param payload The request payload.
   * @return The constructed request object.
   */
  private Request createPostRequest(String url, String payload) {
    RequestBody body = RequestBody.create(payload, MediaType.get("application/json; charset=utf-8"));
    return new Request.Builder()
        .url(url)
        .post(body)
        .addHeader("Content-Type", "application/json")
        .addHeader("Accept", "application/json")
        .build();
  }

  private void waitForBackendToBeAvailable() {
    int maxRetries = 60;
    int delayMillis = 1000;

    for (int i = 0; i < maxRetries; i++) {
      try {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8080/health").openConnection();
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(1000);
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
          PipelineLogger.info("Backend is UP and responding.");
          return;
        }
      } catch (IOException ignored) {
      }

      PipelineLogger.info("Waiting for backend to become ready... (" + (i + 1) + ")");
      try {
        Thread.sleep(delayMillis);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }

    PipelineLogger.error("Backend did not become ready after timeout.");
  }

  private String extractRepoName(String repoUrl) {
    // Regular expression to match GitHub repository URLs
    String regex = "^(?:https://github\\.com/|git@github\\.com:)([^/]+)/([^/]+?)(?:\\.git)?$";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(repoUrl);

    if (matcher.matches()) {
      return matcher.group(2); // The repository name
    } else {
      throw new IllegalArgumentException("Invalid GitHub repository URL: " + repoUrl);
    }
  }

}
