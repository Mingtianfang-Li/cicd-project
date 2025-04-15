package edu.neu.cs6510.sp25.t1.cli.service;

import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Yaml;
import io.kubernetes.client.openapi.apis.CoreV1Api;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Utility service for managing Kubernetes operations related to the CI/CD pipeline.
 * Includes functionality to start pods, apply configurations, port-forward services,
 * and monitor pod readiness for local or remote execution environments.
 */
public class K8sService {

  private static final String NAMESPACE = "default";
  private static Process portForwardProcess;
  private static String podName;

  /**
   * Starts the full CI/CD environment by applying the cicd-pod YAML configuration
   * and waiting for the pod to become ready.
   *
   * @param pipelineName the name of the pipeline, used to generate a unique pod name
   */
  public static void startCicdEnvironment(String pipelineName) {
    try {
      ApiClient client = Config.defaultClient();
      Configuration.setDefaultApiClient(client);
      CoreV1Api api = new CoreV1Api();

      applyYaml("k8s/cicd-pod.yaml", api, pipelineName);
      waitForPod(podName.toLowerCase(), api);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to start CI/CD environment", e);
    }
  }

  /**
   * Starts only the backend pod defined in the Kubernetes YAML configuration and
   * port-forwards it to localhost on port 8080.
   *
   * @param pipelineName the name of the pipeline, used to generate a unique pod name
   * @return the generated pod name
   */
  public static String startBackendEnvironment(String pipelineName) {
    try {
      ApiClient client = Config.defaultClient();
      Configuration.setDefaultApiClient(client);
      CoreV1Api api = new CoreV1Api();

      if (pipelineName.endsWith(".yaml")) {
        pipelineName = pipelineName.replaceFirst("\\.yaml$", "");
      }

      applyYaml("k8s/backend-pod.yaml", api, pipelineName);
      waitForPod(podName.toLowerCase(), api);
      portForwardBackendService();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return podName;
  }

  /**
   * Applies a Kubernetes YAML configuration file (either Pod or Service).
   * Sets the pod name dynamically based on the pipeline name and the type of resource (cicd, backend, or generic).
   *
   * @param filePath the path to the Kubernetes YAML file (e.g., pod or service definition)
   * @param api the CoreV1Api client used to apply the resource
   * @param pipelineName the name of the pipeline, used to generate a unique pod name
   * @throws Exception if the YAML file cannot be applied or parsed
   */
  private static void applyYaml(String filePath, CoreV1Api api, String pipelineName) throws Exception {
    try (InputStream is = new FileInputStream(filePath)) {
      Object obj = Yaml.load(new java.io.InputStreamReader(is));

      // Sanitize pipelineName to comply with Kubernetes naming rules
      String safePipelineName = pipelineName.toLowerCase().replaceAll("[^a-z0-9.-]", "");

      // Determine pod name based on file path
      if (filePath.contains("cicd")) {
        podName = "cicd-pod-" + safePipelineName;
      } else if (filePath.contains("backend")) {
        podName = "backend-pod-" + safePipelineName;
      } else {
        podName = "generic-pod-" + safePipelineName;
      }

      if (obj instanceof V1Pod pod) {
        pod.getMetadata().setName(podName);
        try {
          api.createNamespacedPod(NAMESPACE, pod, null, null, null, null);
        } catch (ApiException e) {
          System.err.println("🔥 Kubernetes API Exception:");
          System.err.println("Status code: " + e.getCode());
          System.err.println("Response body: " + e.getResponseBody());
          System.err.println("Response headers: " + e.getResponseHeaders());
          e.printStackTrace();

          throw new RuntimeException("Failed to apply pod " + podName, e);
        }
        // api.createNamespacedPod(NAMESPACE, pod, null, null, null, null);
        System.out.println("Applied Pod: " + pod.getMetadata().getName());
      } else if (obj instanceof V1Service svc) {
        api.createNamespacedService(NAMESPACE, svc, null, null, null, null);
        System.out.println("Applied Service: " + svc.getMetadata().getName());
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to apply pod " + podName, e);
    }
  }

  /**
   * Waits for the specified Kubernetes pod to reach the "Ready" state.
   * Polls the pod status for up to 60 seconds before timing out.
   *
   * @param podName the name of the pod to wait for
   * @param api the CoreV1Api client used to query pod status
   * @throws Exception if the pod does not become ready within the timeout period
   */
  private static void waitForPod(String podName, CoreV1Api api) throws Exception {
    System.out.println("Waiting for pod to be ready: " + podName);
    int retries = 60;

    for (int i = 0; i < retries; i++) {
      V1Pod pod = api.readNamespacedPod(podName, NAMESPACE, null);
      var status = pod.getStatus().getConditions();

      if (status != null && status.stream().anyMatch(
          c -> "Ready".equals(c.getType()) && "True".equals(c.getStatus()))) {
        System.out.println("Pod ready: " + podName);
        return;
      }

      Thread.sleep(1000);
    }

    throw new RuntimeException("Pod " + podName + " did not become ready in time.");
  }

  /**
   * Sets up port-forwarding from the backend pod to localhost:8080.
   * Sleeps for a short period before executing to allow backend startup.
   */
  public static void portForwardBackendService() {
    // waitForBackendStartupInsidePod();

    try {
      System.out.println("Sleeping 50 seconds before port-forwarding to allow backend to fully start...");
      Thread.sleep(50000); // Sleep 50 seconds before port-forward

      System.out.println("Port forwarding backend service on port 8080...");
      final String podCommand = "pod/" + podName;
      ProcessBuilder pb = new ProcessBuilder("kubectl", "port-forward", podCommand, "8080:8080");
      pb.inheritIO();
      portForwardProcess = pb.start();
      Thread.sleep(3000); // Give it time to establish
    } catch (Exception e) {
      throw new RuntimeException("Failed to port-forward backend service", e);
    }
  }

  /**
   * Stops the current port-forward process if it is running.
   */
  public static void stopPortForward() {
    if (portForwardProcess != null && portForwardProcess.isAlive()) {
      System.out.println("🔌 Closing port-forward on 8080...");
      portForwardProcess.destroy();
    }
  }

  /**
   * Repeatedly checks whether the backend service is reachable via localhost:8080.
   * Waits until the `/health` endpoint returns a 200 OK response or times out.
   */
  public static void waitForBackendStartupInsidePod() {
    String internalUrl = "http://localhost:8080/health";
    int maxRetries = 100;
    int delayMillis = 1000;

    PipelineLogger.info("Checking if backend is responding on port 8080...");

    for (int i = 0; i < maxRetries; i++) {
      try {
        HttpURLConnection connection = (HttpURLConnection) new URL(internalUrl).openConnection();
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

      if (i % 10 == 0) {
        PipelineLogger.info("Waiting for backend to become ready... (" + (i + 1) + ")");
      }
      try {
        Thread.sleep(delayMillis);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }

    throw new RuntimeException("Backend failed to become ready after timeout.");
  }

  /**
   * Deletes the specified pod in the default Kubernetes namespace.
   *
   * @param podName the name of the pod to delete
   */
  public static void stopPod(String podName) {
    try {
      ApiClient client = Config.defaultClient();
      Configuration.setDefaultApiClient(client);
      CoreV1Api api = new CoreV1Api();

      V1DeleteOptions deleteOptions = new V1DeleteOptions(); // default deletion policy

      System.out.println("Deleting pod: " + podName);
      api.deleteNamespacedPod(
          podName,
          NAMESPACE,
          null, // pretty
          null, // dryRun
          null, // gracePeriodSeconds
          null, // orphanDependents (deprecated)
          null, // propagationPolicy
          deleteOptions);
      System.out.println("Pod deletion requested: " + podName);
    } catch (ApiException e) {
      System.err.println("Failed to delete pod via Kubernetes API");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Response body: " + e.getResponseBody());
    } catch (IOException e) {
      throw new RuntimeException("Failed to initialize Kubernetes API client", e);
    }
  }

}
