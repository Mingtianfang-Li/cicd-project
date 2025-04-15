package edu.neu.cs6510.sp25.t1.cli.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;

/**
 * Client for communicating with the backend API.
 */
public class CliBackendClient {
  private final String baseUrl;
  private final HttpClient httpClient;

  /**
   * Constructor for CliBackendClient.
   *
   * @param baseUrl the base URL of the backend API
   */
  public CliBackendClient(String baseUrl) {
    this.baseUrl = baseUrl;
    this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
  }

  /**
   * Fetch pipeline reports from the backend.
   *
   * @param pipelineName the pipeline name
   * @param runNumber the run number, or -1 for all runs
   * @param stageName the stage name (optional)
   * @param jobName the job name (optional)
   * @return formatted response from the backend
   * @throws IOException if the API request fails
   */
  public String fetchPipelineReport(String pipelineName, Integer runNumber,
                                    String stageName, String jobName) throws IOException {
    String encodedPipeline = URLEncoder.encode(pipelineName, StandardCharsets.UTF_8);
    StringBuilder urlBuilder = new StringBuilder(baseUrl);

    if (runNumber == null || runNumber == -1) {
      // Pipeline history endpoint
      urlBuilder.append("/api/report/pipeline/history/").append(encodedPipeline);

      // If stage is provided, add as query parameter
      if (stageName != null && !stageName.isEmpty()) {
        String encodedStage = URLEncoder.encode(stageName, StandardCharsets.UTF_8);
        urlBuilder.append("?stage=").append(encodedStage);

        // If job is also provided, add as query parameter
        if (jobName != null && !jobName.isEmpty()) {
          String encodedJob = URLEncoder.encode(jobName, StandardCharsets.UTF_8);
          urlBuilder.append("&job=").append(encodedJob);
        }
      }
    } else {
      // Specific run number
      urlBuilder.append("/api/report/pipeline/")
              .append(encodedPipeline)
              .append("/run/")
              .append(runNumber);

      // Add stage if provided
      if (stageName != null && !stageName.isEmpty()) {
        String encodedStage = URLEncoder.encode(stageName, StandardCharsets.UTF_8);
        urlBuilder.append("/stage/").append(encodedStage);

        // Add job if provided
        if (jobName != null && !jobName.isEmpty()) {
          String encodedJob = URLEncoder.encode(jobName, StandardCharsets.UTF_8);
          urlBuilder.append("/job/").append(encodedJob);
        }
      }
    }

    return sendGetRequest(urlBuilder.toString());
  }

  /**
   * Send a GET request to the backend API.
   *
   * @param url the URL to send the request to
   * @return the response body as a string
   * @throws IOException if the API request fails
   */
  private String sendGetRequest(String url) throws IOException {
    try {
      PipelineLogger.info("Sending GET request to: " + url);

      HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(url))
              .GET()
              .header("Accept", "application/json")
              .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() >= 400) {
        PipelineLogger.error("API request failed with status code: " + response.statusCode());
        PipelineLogger.error("Response body: " + response.body());
        throw new IOException("API request failed with status code: " + response.statusCode());
      }

      return response.body();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("API request was interrupted", e);
    } catch (Exception e) {
      throw new IOException("API request failed: " + e.getMessage(), e);
    }
  }
}