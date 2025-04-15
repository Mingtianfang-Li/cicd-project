package edu.neu.cs6510.sp25.t1.cli.commands;

import java.io.IOException;
import java.util.concurrent.Callable;

import edu.neu.cs6510.sp25.t1.cli.CliApp;
import edu.neu.cs6510.sp25.t1.cli.service.K8sService;
import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import picocli.CommandLine;



/*
 * CLI command that fetches the current status of a CI/CD pipeline by querying the backend service.
 * This command automatically starts a Kubernetes backend pod for the specified pipeline,
 * performs an HTTP GET request to the backend, and prints the pipeline status in the terminal.
 *
 * Used with the `status` subcommand in the CLI.
 */
@CommandLine.Command(
    name = "status",
    description = "Fetches the status of a CI/CD pipeline."
)
public class StatusCommand implements Callable<Integer> {

//  private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
private final OkHttpClient httpClient;

  public StatusCommand() {
    this(new OkHttpClient());
  }

  public StatusCommand(OkHttpClient client) {
    this.httpClient = client;
  }
  private static final String BASE_URL = "http://localhost:8080/api/pipeline/";

  @CommandLine.ParentCommand
  private CliApp parent;

  @CommandLine.Option(names = {"--pipeline", "-p"}, required = true, description = "Specify the pipeline name.")
  private String pipelineName;

  @Override
  public Integer call() {
    if (pipelineName == null || pipelineName.trim().isEmpty()) {
      PipelineLogger.error("Pipeline name must be provided with --pipeline or -p.");
      return 1;
    }

    String podName = K8sService.startBackendEnvironment(pipelineName);

    String url = BASE_URL + pipelineName;
    PipelineLogger.info("Fetching pipeline status for: " + pipelineName);
    PipelineLogger.debug("GET " + url);

    Request request = new Request.Builder()
        .url(url)
        .get()
        .addHeader("Accept", "application/json")
        .build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        String errorBody = response.body() != null ? response.body().string() : "Empty response";
        PipelineLogger.error("Failed to fetch pipeline status.");
        PipelineLogger.error("HTTP Status: " + response.code());
        PipelineLogger.error("Response: " + errorBody);
        K8sService.stopPortForward();
        K8sService.stopPod(podName);
        return 1;
      }

      String responseBody = response.body() != null ? response.body().string() : "No content";
      PipelineLogger.info("Pipeline Status:");
      PipelineLogger.info(responseBody);
      K8sService.stopPortForward();
      K8sService.stopPod(podName);
      return 0;

    } catch (IOException e) {
      PipelineLogger.error("Error while contacting backend: " + e.getMessage());
      K8sService.stopPortForward();
      K8sService.stopPod(podName);
      return 1;
    }

  }
}
