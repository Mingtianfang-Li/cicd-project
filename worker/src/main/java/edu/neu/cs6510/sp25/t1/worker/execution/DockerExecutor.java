package edu.neu.cs6510.sp25.t1.worker.execution;

import com.github.dockerjava.api.DockerClient;

import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.okhttp.OkHttpDockerCmdExecFactory;
import edu.neu.cs6510.sp25.t1.worker.error.DockerExecutionException;
import edu.neu.cs6510.sp25.t1.worker.error.JobExecutionConfigException;
import edu.neu.cs6510.sp25.t1.worker.service.GitCloneService;
import edu.neu.cs6510.sp25.t1.worker.utils.FindPipelineBranch;
import edu.neu.cs6510.sp25.t1.worker.utils.FindPipelineName;
import org.springframework.stereotype.Component;
import java.util.List;
import edu.neu.cs6510.sp25.t1.common.dto.JobExecutionDTO;
import edu.neu.cs6510.sp25.t1.common.dto.JobDTO;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Executes jobs inside Docker containers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DockerExecutor {

  // adding docker client
  private final DockerClient dockerClient = createDockerClient();
  private final FindPipelineName findPipelineName;
  private final GitCloneService gitCloneService;
  private final FindPipelineBranch findPipelineBranch;

  private DockerClient createDockerClient() {
    DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
        .withDockerHost("unix:///var/run/docker.sock") // explicitly use host socket
        .build();

    return DockerClientBuilder.getInstance(config)
        .withDockerCmdExecFactory(new OkHttpDockerCmdExecFactory())
        .build();
  }

  /**
   * Executes a job inside a Docker container.
   *
   * @param jobExecution The job execution details.
   * @return The execution status.
   */
  public ExecutionStatus execute(JobExecutionDTO jobExecution) {
    JobDTO job = jobExecution.getJob();
    if (job == null) {
      throw new JobExecutionConfigException("Job details are missing");
    }

    // get pipelineName
    String pipelineName = findPipelineName.getPipelineName(jobExecution);

    //get branch
    String branch = findPipelineBranch.getBranch(jobExecution);

//    // 55-56 is about to make container path unique for every job
//    String jobId = String.valueOf(job.getId());

//    // create container path
//    String containerPath = "/app/" + pipelineName;

    String dockerImage = job.getDockerImage();
    List<String> script = job.getScript();

    // retrieve working Directory from job database
    String url = job.getWorkingDir();
    // Extract the name section of the working directory
//    String[] pathParts = workingDirectory.split("/");
//    String workingDirectoryName = pathParts[pathParts.length - 1];
    log.info("Extracted url name: {}", url);

    // clone into workingDir
    String volumeName;
    try {
      volumeName = gitCloneService.cloneRepoToVolume(url, branch, pipelineName);
    } catch (Exception e) {
      log.error("Git clone to volume failed: {}", e.getMessage(), e);
      throw new DockerExecutionException("Git clone failed", e);
    }

    if (script == null || script.isEmpty()) {
      throw new JobExecutionConfigException("Script commands are missing");
    }

    if (dockerImage == null || dockerImage.trim().isEmpty()) {
      throw new JobExecutionConfigException("Docker image is not specified");
    }

    // confirm we have workDir
//    if (workingDirectory == null || workingDirectory.trim().isEmpty()) {
//      throw new JobExecutionConfigException("Working directory is not specified");
//    }
    if (volumeName == null || volumeName.trim().isEmpty()) {
      throw new JobExecutionConfigException("Working directory is not specified");
    }

    // verified the file exists in the working Dir
//    File dir = new File(workingDirectory);
//    if (!dir.exists() || !dir.isDirectory()) {
//      throw new JobExecutionConfigException("Working directory does not exist or is not a directory");
//    }

//    printWorkingDirectoryContents(dir);
    String command = String.join(" && ", script);
    String containerID = null;
    boolean debugging = false;

    try {
      dockerClient.pullImageCmd(dockerImage).start().awaitCompletion();
      log.info("Pulled Docker image: {}", dockerImage);
      /*
      Volume containerVolume = new Volume(workingDirectory);
      Bind hostBind = new Bind(workingDirectory, containerVolume, AccessMode.rw);
      */
//      CreateVolumeResponse volumeResponse = dockerClient.createVolumeCmd()
//        .withName("cicd")
//        .withDriver("local")
//        .exec();
//      log.info("Created or retrieved external volume: {}, {}", volumeResponse.getName(), volumeResponse.getMountpoint());
//      ;
//      Volume containerVolume = new Volume("/mnt/pipeline/"+workingDirectoryName);
//      Bind hostBind = new Bind(volumeResponse.getMountpoint()+"/"+workingDirectoryName, containerVolume, AccessMode.rw);

//      var container = dockerClient.createContainerCmd(dockerImage)
//          .withCmd("sh", "-c", command)
//          .withWorkingDir(workingDirectory)  // same path as host bind mount
//          .withHostConfig(new com.github.dockerjava.api.model.HostConfig().withBinds(hostBind))
//          .withVolumes(containerVolume)
//          .exec();
      String targetMountPath = "/app"; // inside container

      var jobContainer = dockerClient.createContainerCmd(dockerImage)
          .withCmd("sh", "-c", command)
          .withWorkingDir(targetMountPath + "/repo")
          .withHostConfig(new HostConfig().withMounts(
              List.of(new Mount()
                  .withType(MountType.VOLUME)
                  .withSource(volumeName)
                  .withTarget(targetMountPath)
              )
          ))
          .exec();


      containerID = jobContainer.getId();
      dockerClient.startContainerCmd(containerID).exec();

      dockerClient.logContainerCmd(containerID)
          .withStdOut(true)
          .withStdErr(true)
          .withFollowStream(true)
          .exec(new com.github.dockerjava.core.command.LogContainerResultCallback() {
            @Override
            public void onNext(com.github.dockerjava.api.model.Frame frame) {
              String logLine = new String(frame.getPayload()).trim();
              log.info("[{}] {}", frame.getStreamType(), logLine);
            }
          }).awaitCompletion();

      Integer exitCode = dockerClient.waitContainerCmd(containerID).start().awaitStatusCode();
      log.info("Container exited with code: {}", exitCode);

      return exitCode == 0 ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILED;
    } catch (Exception e) {
      log.error("Docker execution failed: {}", e.getMessage(), e);
      throw new DockerExecutionException("Docker execution failed: " + e.getMessage(), e);
    } finally {
      if (containerID != null) {
        try {
          if (debugging) {
            log.info("Debug mode, skip cleaning up container with ID: {}", containerID);
          } else {
            dockerClient.removeContainerCmd(containerID).withForce(true).exec();
          }
          log.info("Cleaned up container {}", containerID);
        } catch (Exception cleanupEx) {
          log.error("Failed to cleanup container {}", containerID, cleanupEx);
        }
      }
    }
  }
}
