package edu.neu.cs6510.sp25.t1.cli.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateVolumeResponse;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.okhttp.OkDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Utility class for creating Docker volumes from a host directory.
 * Copies the contents of a host project directory into a Docker volume
 * under a unique subdirectory.
 */
public class DockerVolumeUtil {

  private static final String VOLUME_NAME = "cicd";
  private static final String CONTAINER_BASE_PATH = "/mnt/pipeline";

  /**
   * Creates a Docker volume if not exists and copies the project directory
   * containing the given file into a unique subdirectory of the volume.
   *
   * @param hostPath Absolute path to a file within the project directory.
   * @return The container path to the file inside the Docker volume, or null if failed.
   */
  public static String createVolumeFromHostDir(String hostPath) {
    try {
      File file = new File(hostPath);
      File hostProjectDir = file.getParentFile().getParentFile(); // Full project dir

      if (!hostProjectDir.exists() || !hostProjectDir.isDirectory()) {
        throw new IllegalArgumentException("Invalid host directory: " + hostProjectDir.getAbsolutePath());
      }

      // Initialize Docker client
      DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
      DockerHttpClient httpClient = new OkDockerHttpClient.Builder()
          .dockerHost(config.getDockerHost())
          .sslConfig(config.getSSLConfig())
          .connectTimeout(30)
          .readTimeout(30)
          .build();
      DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

      // Create volume if it doesn't exist
      try {
        dockerClient.inspectVolumeCmd(VOLUME_NAME).exec();
        PipelineLogger.info("Volume '" + VOLUME_NAME + "' already exists.");
      } catch (Exception e) {
        PipelineLogger.info("Creating Docker volume: " + VOLUME_NAME);
        CreateVolumeResponse volume = dockerClient.createVolumeCmd()
            .withName(VOLUME_NAME)
            .withDriver("local")
            .exec();
      }

      // Generate UUID for unique subdirectory
      String uuid = UUID.randomUUID().toString();
      String mountSubDir = CONTAINER_BASE_PATH + "/" + uuid;

      // Build and run the Docker copy command
      String containerName = "temp-copy-container-" + uuid;
      ProcessBuilder copyBuilder = new ProcessBuilder(
          "docker", "run", "--rm",
          "--name", containerName,
          "-v", hostProjectDir.getAbsolutePath() + ":/from:ro",
          "-v", VOLUME_NAME + ":" + CONTAINER_BASE_PATH,
          "alpine", "sh", "-c", "mkdir -p " + mountSubDir + " && cp -r /from/. " + mountSubDir
      );

      PipelineLogger.info("Copying project to Docker volume '" + VOLUME_NAME + "' under " + mountSubDir + "...");
      Process copyProcess = copyBuilder.start();
      int exitCode = copyProcess.waitFor();

      if (exitCode != 0) {
        PipelineLogger.error("Failed to copy files from host to Docker volume.");
        return null;
      }

      // Build and return the container file path for the input file
      String relativePathInProject = file.getAbsolutePath().substring(hostProjectDir.getAbsolutePath().length());
      String containerFilePath = mountSubDir + relativePathInProject.replace("\\", "/");
      return containerFilePath;

    } catch (IOException | InterruptedException e) {
      PipelineLogger.error("Docker volume creation or file copy failed: " + e.getMessage());
      return null;
    }
  }
}
