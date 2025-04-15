package edu.neu.cs6510.sp25.t1.worker.execution;

import com.github.dockerjava.api.DockerClient;
import edu.neu.cs6510.sp25.t1.common.dto.JobDTO;
import edu.neu.cs6510.sp25.t1.common.dto.JobExecutionDTO;
import edu.neu.cs6510.sp25.t1.worker.error.DockerExecutionException;
import edu.neu.cs6510.sp25.t1.worker.error.JobExecutionConfigException;
import edu.neu.cs6510.sp25.t1.worker.service.GitCloneService;
import edu.neu.cs6510.sp25.t1.worker.utils.FindPipelineBranch;
import edu.neu.cs6510.sp25.t1.worker.utils.FindPipelineName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Test class for DockerExecutor focusing on input validation and error handling
 */
@ExtendWith(MockitoExtension.class)
public class DockerExecutorTest {
    @Mock
    private FindPipelineName findPipelineName;

    @Mock
    private GitCloneService gitCloneService;

    @Mock
    private FindPipelineBranch findPipelineBranch;

    private DockerExecutor dockerExecutor;

    @Mock
    private DockerClient dockerClient;

    private JobExecutionDTO jobExecution;
    private JobDTO job;

    @BeforeEach
    void setUp() throws Exception {
        // Create a real DockerExecutor
        dockerExecutor = new DockerExecutor(findPipelineName, gitCloneService, findPipelineBranch);

        // Use reflection to inject our mock dockerClient
        ReflectionTestUtils.setField(dockerExecutor, "dockerClient", dockerClient);

        // Set up basic test data
        job = new JobDTO();
        job.setId(UUID.randomUUID());
        job.setName("test-job");
        job.setDockerImage("alpine:latest");
        job.setWorkingDir("https://github.com/test/repo.git");
        job.setScript(Arrays.asList("echo 'Hello'", "ls -la"));

        jobExecution = new JobExecutionDTO();
        jobExecution.setId(UUID.randomUUID());
        jobExecution.setJob(job);

        // Set up basic mock behavior
//        when(findPipelineName.getPipelineName(any())).thenReturn("test-pipeline");
//        when(findPipelineBranch.getBranch(any())).thenReturn("main");
//        when(gitCloneService.cloneRepoToVolume(anyString(), anyString(), anyString())).thenReturn("test-volume");
    }

    /**
     * Test that an exception is thrown when job details are missing
     */
    @Test
    public void testMissingJobDetails() {
        // Set job to null
        jobExecution.setJob(null);

        // Execute test
        JobExecutionConfigException exception = assertThrows(
                JobExecutionConfigException.class,
                () -> dockerExecutor.execute(jobExecution)
        );

        // Verify exception message
        assertEquals("Job details are missing", exception.getMessage());
    }

    /**
     * Test that an exception is thrown when script is missing
     */
    @Test
    public void testMissingScript() {
        // Set script to null
        job.setScript(null);

        // Execute test
        JobExecutionConfigException exception = assertThrows(
                JobExecutionConfigException.class,
                () -> dockerExecutor.execute(jobExecution)
        );

        // Verify exception message
        assertEquals("Script commands are missing", exception.getMessage());
    }

    /**
     * Test that an exception is thrown when script is empty
     */
    @Test
    public void testEmptyScript() {
        // Set empty script list
        job.setScript(Collections.emptyList());

        // Execute test
        JobExecutionConfigException exception = assertThrows(
                JobExecutionConfigException.class,
                () -> dockerExecutor.execute(jobExecution)
        );

        // Verify exception message
        assertEquals("Script commands are missing", exception.getMessage());
    }

    /**
     * Test that an exception is thrown when Docker image is missing
     */
    @Test
    public void testMissingDockerImage() {
        // Set dockerImage to null
        job.setDockerImage(null);

        // Execute test
        JobExecutionConfigException exception = assertThrows(
                JobExecutionConfigException.class,
                () -> dockerExecutor.execute(jobExecution)
        );

        // Verify exception message
        assertEquals("Docker image is not specified", exception.getMessage());
    }

    /**
     * Test that an exception is thrown when Docker image is empty
     */
    @Test
    public void testEmptyDockerImage() {
        // Set empty dockerImage (with spaces)
        job.setDockerImage("  ");

        // Execute test
        JobExecutionConfigException exception = assertThrows(
                JobExecutionConfigException.class,
                () -> dockerExecutor.execute(jobExecution)
        );

        // Verify exception message
        assertEquals("Docker image is not specified", exception.getMessage());
    }

    /**
     * Test that an exception is thrown when Git clone fails
     */
    @Test
    public void testGitCloneFailure() throws Exception {
        // Mock git clone failure
        when(gitCloneService.cloneRepoToVolume(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Git clone failed"));

        // Execute test
        DockerExecutionException exception = assertThrows(
                DockerExecutionException.class,
                () -> dockerExecutor.execute(jobExecution)
        );

        // Verify exception message contains correct information
        assertTrue(exception.getMessage().contains("Git clone failed"));
    }
}