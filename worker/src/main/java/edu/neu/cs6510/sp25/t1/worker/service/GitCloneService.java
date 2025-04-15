package edu.neu.cs6510.sp25.t1.worker.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.okhttp.OkHttpDockerCmdExecFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GitCloneService {

  private final DockerClient dockerClient = createDockerClient();

  private DockerClient createDockerClient() {
    DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
        .withDockerHost("unix:///var/run/docker.sock")
        .build();

    return DockerClientBuilder.getInstance(config)
        .withDockerCmdExecFactory(new OkHttpDockerCmdExecFactory())
        .build();
  }

  /**
   * Clones the Git repo into a Docker volume using a helper container.
   *
   * @param repoUrl      Git repository URL
   * @param branch       Branch to clone
   * @param pipelineName Used to name the Docker volume
   * @return The Docker volume name
   * @throws Exception if cloning or volume creation fails
   */
  public String cloneRepoToVolume(String repoUrl, String branch, String pipelineName) throws Exception {
    String volumeName = "cicd-" + pipelineName;
    String containerMountPath = "/data"; // in-container path
    String cloneTarget = containerMountPath + "/repo";
    String gitDirPath = cloneTarget + "/.git";

//    // Step 1: Create volume
//    CreateVolumeResponse volume = dockerClient.createVolumeCmd()
//        .withName(volumeName)
//        .withDriver("local")
//        .exec();

    log.info("Created Docker volume: {}", volumeName);

    // Step 2: Check if .git directory already exists
    Volume containerVolume = new Volume(containerMountPath);
    String testGitDirCmd = "[ -d \"" + gitDirPath + "\" ]";

    var checkGitContainer = dockerClient.createContainerCmd("alpine")
        .withCmd("sh", "-c", testGitDirCmd)
        .withHostConfig(new HostConfig().withBinds(new Bind(volumeName, containerVolume)))
        .withVolumes(containerVolume)
        .exec();

    String checkId = checkGitContainer.getId();
    dockerClient.startContainerCmd(checkId).exec();
    int gitDirExists = dockerClient.waitContainerCmd(checkId).start().awaitStatusCode();
    dockerClient.removeContainerCmd(checkId).withForce(true).exec();

    if (gitDirExists == 0) {
      log.info("Repo already exists in volume. Skipping clone.");
      return volumeName;
    }

    // Step 2: Git clone command
    String gitCloneCmd = String.format("git clone --depth 1 --branch %s %s %s", branch, repoUrl, cloneTarget);
    log.info("Running git clone command in container: {}", gitCloneCmd);

    // Step 3: Create and start helper container
    var helperContainer = dockerClient.createContainerCmd("alpine/git:2.36.2")
        .withCmd("clone", "--depth", "1", "--branch", branch, repoUrl, "/data/repo")
        .withHostConfig(new HostConfig().withBinds(new Bind(volumeName, containerVolume)))
        .withVolumes(containerVolume)
        .exec();

    String containerId = helperContainer.getId();
    dockerClient.startContainerCmd(containerId).exec();

    dockerClient.logContainerCmd(containerId)
        .withStdOut(true)
        .withStdErr(true)
        .withFollowStream(false)
        .exec(new LogContainerResultCallback() {
          @Override
          public void onNext(Frame frame) {
            log.error("[git-clone-container] {}", new String(frame.getPayload()).trim());
          }
        }).awaitCompletion();

    // Step 4: Wait for it to finish
    Integer exitCode = dockerClient.waitContainerCmd(containerId).start().awaitStatusCode();
    log.info("Clone container exited with code: {}", exitCode);

    // Step 5: Cleanup
    //dockerClient.removeContainerCmd(containerId).withForce(true).exec();

    if (exitCode != 0) {
      throw new RuntimeException("Git clone failed inside container");
    }

    return volumeName;
  }
}
