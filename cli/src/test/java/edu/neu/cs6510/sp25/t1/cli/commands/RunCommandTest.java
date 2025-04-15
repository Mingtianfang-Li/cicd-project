package edu.neu.cs6510.sp25.t1.cli.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import edu.neu.cs6510.sp25.t1.cli.CliApp;
import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;
import edu.neu.cs6510.sp25.t1.common.utils.GitCloneUtil;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RunCommandTest {

    @Mock
    private CliApp parentMock;

    // Create a testable version that doesn't try to override methods
    static class TestableRunCommand extends RunCommand {
        // Fields to store arguments
        private String repoValue;
        private  String branchValue;
        private  String commitValue;
        private  String pipelineValue;
        private  String filePathValue;
        private  boolean localRunValue;
        private  boolean dryRunValue;

        public TestableRunCommand(CliApp parent) {
            super();
            try {
                Field parentField = RunCommand.class.getDeclaredField("parent");
                parentField.setAccessible(true);
                parentField.set(this, parent);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set parent", e);
            }


        }

        // Helper method to set up the object for testing
        public void setupForTest() throws Exception {
            // Use reflection to set the private fields in the parent class
            setPrivateField(this, "repo", repoValue);
            setPrivateField(this, "branch", branchValue);
            setPrivateField(this, "commit", commitValue);
            setPrivateField(this, "pipeline", pipelineValue);
            setPrivateField(this, "filePath", filePathValue);
            setPrivateField(this, "localRun", localRunValue);
            setPrivateField(this, "dryRun", dryRunValue);

        }

        private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
            Field field = RunCommand.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        }

        // Helper method to create a request directly (since we can't override the private method)
        public Request createTestRequest(String url, String payload) {
            RequestBody body = RequestBody.create(payload, MediaType.get("application/json; charset=utf-8"));
            return new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build();
        }
    }

    @Mock
    private OkHttpClient httpClientMock;

    @Mock
    private Call callMock;

    @Mock
    private Response responseMock;

    @Mock
    private ResponseBody responseBodyMock;

    private TestableRunCommand testableRunCommand;

    @BeforeEach
    public void setUp() {
        testableRunCommand = new TestableRunCommand(parentMock);
    }


    @Test
    public void testExtractRepoName_ValidGithubUrl() throws Exception {
        // Use reflection to access private method
        Method method = RunCommand.class.getDeclaredMethod("extractRepoName", String.class);
        method.setAccessible(true);

        // Test https URL format
        String repoName1 = (String) method.invoke(testableRunCommand, "https://github.com/username/repo-name");
        assertEquals("repo-name", repoName1);

        // Test SSH URL format
        String repoName2 = (String) method.invoke(testableRunCommand, "git@github.com:username/repo-name.git");
        assertEquals("repo-name", repoName2);
    }

    @Test
    public void testExtractRepoName_InvalidUrl() throws Exception {
        // Use reflection to access private method
        Method method = RunCommand.class.getDeclaredMethod("extractRepoName", String.class);
        method.setAccessible(true);

        // Test with invalid URL - will throw IllegalArgumentException wrapped in an InvocationTargetException
        Exception exception = assertThrows(InvocationTargetException.class, () -> {
            method.invoke(testableRunCommand, "invalid-url");
        });

        // Assert that the cause is IllegalArgumentException
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertTrue(exception.getCause().getMessage().contains("Invalid GitHub repository URL"));
    }

    @Test
    public void testCreatePostRequest() throws Exception {
        // Use reflection to access private method
        Method method = RunCommand.class.getDeclaredMethod("createPostRequest", String.class, String.class);
        method.setAccessible(true);

        // Arrange
        String testUrl = "http://example.com";
        String testPayload = "{\"key\":\"value\"}";

        // Act
        Request request = (Request) method.invoke(testableRunCommand, testUrl, testPayload);

        // Assert
        assertNotNull(request);
        assertEquals(testUrl + "/", request.url().toString());
        assertEquals("POST", request.method());
        assertTrue(request.header("Content-Type").contains("application/json"));
        assertTrue(request.header("Accept").contains("application/json"));

        // Verify the request has a body
        assertNotNull(request.body());

        // We can indirectly verify content length if needed
        try {
            assertEquals(testPayload.length(), request.body().contentLength());
        } catch (IOException e) {
            fail("Failed to get content length: " + e.getMessage());
        }
    }

    @Test
    public void testCreatePostRequestWithComplexPayload() throws Exception {
        // Use reflection to access private method
        Method method = RunCommand.class.getDeclaredMethod("createPostRequest", String.class, String.class);
        method.setAccessible(true);

        // Arrange
        String testUrl = "http://localhost:8080/api/pipeline/run";
        String jsonPayload = String.format(
                "{\"repo\": \"%s\", \"branch\": \"%s\", \"commit\": \"%s\", \"pipeline\": \"%s\", \"filePath\": \"%s\", \"local\": %s}",
                "https://github.com/test/repo", "main", "abc123", "test-pipeline", "/path/to/file", true);

        // Act
        Request request = (Request) method.invoke(testableRunCommand, testUrl, jsonPayload);

        // Assert
        assertNotNull(request);
        assertEquals(testUrl, request.url().toString());
        assertEquals("POST", request.method());
        assertEquals("application/json", request.header("Content-Type"));
        assertEquals("application/json", request.header("Accept"));

        // Verify content-type and length
        MediaType contentType = request.body().contentType();
        assertNotNull(contentType);
        assertEquals("application", contentType.type());
        assertEquals("json", contentType.subtype());

        try {
            assertEquals(jsonPayload.length(), request.body().contentLength());
        } catch (IOException e) {
            fail("Failed to get content length: " + e.getMessage());
        }
    }

    @Test
    public void testErrorHandling() throws Exception {
        // Set up the command with valid values
        testableRunCommand.repoValue = "https://github.com/org/repo";
        testableRunCommand.branchValue = "main";
        testableRunCommand.setupForTest();

        try (MockedStatic<PipelineLogger> loggerMock = mockStatic(PipelineLogger.class)) {
            // Set up to throw an exception
            loggerMock.when(() -> PipelineLogger.info(anyString())).thenThrow(new RuntimeException("Test exception"));

            // Call the method
            int result = testableRunCommand.call();

            // Verify the error is logged
            assertEquals(1, result);
            loggerMock.verify(() -> PipelineLogger.error(contains("Execution Error:")));
        }
    }

    @Test
    public void testMissingRepoAndFilePath_shouldError() throws Exception {
        testableRunCommand.localRunValue = true;
        testableRunCommand.repoValue = null;
        testableRunCommand.filePathValue = null;
        testableRunCommand.setupForTest();

        try (MockedStatic<PipelineLogger> loggerMock = mockStatic(PipelineLogger.class)) {
            int result = testableRunCommand.call();
            assertEquals(1, result);
            loggerMock.verify(() -> PipelineLogger.error(contains("Pipeline configuration file must be specified")));
        }
    }

    @Test
    public void testFilePathInsideGitRepo_shouldSetRepo() throws Exception {
        testableRunCommand.localRunValue = true;
        testableRunCommand.repoValue = null;
        testableRunCommand.filePathValue = "/mock/path";
        testableRunCommand.setupForTest();

        try (
                MockedStatic<PipelineLogger> loggerMock = mockStatic(PipelineLogger.class);
                MockedStatic<GitCloneUtil> gitMock = mockStatic(GitCloneUtil.class)
        ) {
            gitMock.when(() -> GitCloneUtil.isInsideGitRepo(any(File.class))).thenReturn(true);
            gitMock.when(() -> GitCloneUtil.getRepoUrlFromFile(any(File.class))).thenReturn("https://github.com/mock/repo");

            testableRunCommand.call();

            gitMock.verify(() -> GitCloneUtil.getRepoUrlFromFile(any(File.class)));
            loggerMock.verify(() -> PipelineLogger.info(contains("Git clone repository url")));
        }
    }

    @Test
    public void testFilePathNotInsideGitRepo_shouldLogError() throws Exception {
        testableRunCommand.dryRunValue = true;
        testableRunCommand.repoValue = null;
        testableRunCommand.filePathValue = "/mock/path";
        testableRunCommand.setupForTest();

        try (
                MockedStatic<PipelineLogger> loggerMock = mockStatic(PipelineLogger.class);
                MockedStatic<GitCloneUtil> gitMock = mockStatic(GitCloneUtil.class)
        ) {
            gitMock.when(() -> GitCloneUtil.isInsideGitRepo(any(File.class))).thenReturn(false);

            testableRunCommand.call();

            gitMock.verify(() -> GitCloneUtil.isInsideGitRepo(any(File.class)));
            loggerMock.verify(() -> PipelineLogger.error(contains("not inside git repo")));
        }
    }


    @Test
    public void testWaitForBackend_backendRespondsImmediately() throws Exception {
        // Spy HttpURLConnection to return 200
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(200);

        // Mock URL.openConnection to return mockConnection
        URL mockUrl = mock(URL.class);
        when(mockUrl.openConnection()).thenReturn(mockConnection);

        try (
                MockedStatic<PipelineLogger> loggerMock = mockStatic(PipelineLogger.class);
                MockedConstruction<URL> urlMock = mockConstruction(URL.class, (mock, context) ->
                        when(mock.openConnection()).thenReturn(mockConnection))
        ) {
            Method method = RunCommand.class.getDeclaredMethod("waitForBackendToBeAvailable");
            method.setAccessible(true);
            method.invoke(testableRunCommand);

            loggerMock.verify(() -> PipelineLogger.info(contains("Backend is UP and responding.")), times(1));
        }
    }

    @Test
    public void testDryRunDelegatesToDryRunCommand() throws Exception {
        testableRunCommand.dryRunValue = true;
        testableRunCommand.filePathValue = "/mock/path";
        testableRunCommand.setupForTest();

        try (
                MockedStatic<PipelineLogger> loggerMock = mockStatic(PipelineLogger.class);
                MockedConstruction<DryRunCommand> dryRunMock = mockConstruction(DryRunCommand.class,
                        (mock, context) -> {
                            doNothing().when(mock).setFilePath(anyString());
                            when(mock.call()).thenReturn(0);
                        })
        ) {
            int result = testableRunCommand.call();
            assertEquals(0, result);
        }
    }

    @Test
    public void testWaitForBackendInterrupted() throws Exception {
        Method method = RunCommand.class.getDeclaredMethod("waitForBackendToBeAvailable");
        method.setAccessible(true);

        HttpURLConnection mockConn = mock(HttpURLConnection.class);
        when(mockConn.getResponseCode()).thenThrow(new IOException("Simulate IO Error"));

        Thread.currentThread().interrupt();

        try (
                MockedStatic<PipelineLogger> loggerMock = mockStatic(PipelineLogger.class);
                MockedConstruction<URL> urlMock = mockConstruction(URL.class, (mock, context) -> {
                    when(mock.openConnection()).thenReturn(mockConn);
                })
        ) {
            method.invoke(testableRunCommand);
            assertTrue(Thread.interrupted());
        }
    }



}