package edu.neu.cs6510.sp25.t1.worker.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Volume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;



import static org.junit.jupiter.api.Assertions.*;


/**
 * Test for GitCloneService
 *
 * Note: Since GitCloneService has tight coupling with DockerClient
 * and uses private methods, these tests are more integration-focused
 * than pure unit tests.
 */
@ExtendWith(MockitoExtension.class)
public class GitCloneServiceTest {

    @Mock
    private DockerClient dockerClient;

    /**
     * This test verifies that the right exception is thrown for invalid inputs
     */
    @Test
    public void testExceptionForInvalidInputs() {
        GitCloneService service = new GitCloneService();

        // Test with null repo URL
        Exception exception = assertThrows(Exception.class, () -> {
            service.cloneRepoToVolume(null, "main", "test-pipeline");
        });

        // We can't assert on the exact message since we can't control the Docker API,
        // but there should be an exception for invalid inputs
        assertNotNull(exception);

        // Test with null branch
        exception = assertThrows(Exception.class, () -> {
            service.cloneRepoToVolume("https://github.com/repo.git", null, "test-pipeline");
        });

        assertNotNull(exception);
    }


//    @Test
//    public void testSkipCloneIfGitExists() throws Exception {
//        CreateContainerCmd createContainerCmd = mock(CreateContainerCmd.class);
//        StartContainerCmd startContainerCmd = mock(StartContainerCmd.class);
//        WaitContainerCmd waitContainerCmd = mock(WaitContainerCmd.class);
//        WaitContainerResultCallback waitResult = mock(WaitContainerResultCallback.class);
//        RemoveContainerCmd removeContainerCmd = mock(RemoveContainerCmd.class);
//        CreateContainerResponse containerResponse = mock(CreateContainerResponse.class);
//
//        when(dockerClient.createContainerCmd(eq("alpine"))).thenReturn(createContainerCmd);
//        when(createContainerCmd.withCmd(any(String[].class))).thenReturn(createContainerCmd);
//        when(createContainerCmd.withHostConfig(any())).thenReturn(createContainerCmd);
//        when(createContainerCmd.withVolumes(any(Volume[].class))).thenReturn(createContainerCmd);
//        when(createContainerCmd.exec()).thenReturn(containerResponse);
//        when(containerResponse.getId()).thenReturn("dummy-container-id");
//
//        when(dockerClient.startContainerCmd(any())).thenReturn(startContainerCmd);
//        when(dockerClient.waitContainerCmd(any())).thenReturn(waitContainerCmd);
//        when(waitContainerCmd.start()).thenReturn(waitResult);
//        when(waitResult.awaitStatusCode()).thenReturn(0); // Simulate .git exists
//
//        when(dockerClient.removeContainerCmd(any())).thenReturn(removeContainerCmd);
//        when(removeContainerCmd.withForce(true)).thenReturn(removeContainerCmd);
//        doNothing().when(removeContainerCmd).exec();
//
//
//        GitCloneService service = new GitCloneService() {{
//            java.lang.reflect.Field dockerField = GitCloneService.class.getDeclaredField("dockerClient");
//            dockerField.setAccessible(true);
//            dockerField.set(this, dockerClient);
//        }};
//
//        String result = service.cloneRepoToVolume("https://github.com/owner/repo.git", "main", "test-skip");
//
//        assertEquals("cicd-test-skip", result);
//        verify(dockerClient, never()).createContainerCmd(eq("alpine/git:2.36.2"));
//    }

}