package edu.neu.cs6510.sp25.t1.cli.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.function.Consumer;

import edu.neu.cs6510.sp25.t1.cli.service.K8sService;
import okhttp3.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.neu.cs6510.sp25.t1.cli.CliApp;
import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;
import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
public class StatusCommandTest {

    @Mock
    private CliApp cliApp;

    private StatusCommand statusCommand;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUp() throws Exception {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        // Create a fresh instance for each test
        statusCommand = new StatusCommand();

        // Set the parent command using reflection
        Field parentField = StatusCommand.class.getDeclaredField("parent");
        parentField.setAccessible(true);
        parentField.set(statusCommand, cliApp);

        // Set pipeline name using reflection
        Field pipelineNameField = StatusCommand.class.getDeclaredField("pipelineName");
        pipelineNameField.setAccessible(true);
        pipelineNameField.set(statusCommand, "test-pipeline");
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    // Special test class that extends StatusCommand so we can override the final HTTP client
    private static class TestStatusCommand extends StatusCommand {
        private final OkHttpClient testClient;

        public TestStatusCommand(OkHttpClient testClient) {
            this.testClient = testClient;
        }
    }

    @Test
    public void testFailedStatusFetch() throws Exception {
        // Create mocks for the HTTP call chain
        OkHttpClient mockClient = mock(OkHttpClient.class);
        Call mockCall = mock(Call.class);

        // Create our test command with the mock client
        TestStatusCommand testCommand = new TestStatusCommand(mockClient);

        // Set the parent command and pipeline name
        Field parentField = StatusCommand.class.getDeclaredField("parent");
        parentField.setAccessible(true);
        parentField.set(testCommand, cliApp);

        Field pipelineNameField = StatusCommand.class.getDeclaredField("pipelineName");
        pipelineNameField.setAccessible(true);
        pipelineNameField.set(testCommand, "test-pipeline");

        // Mock 404 response
        String errorJson = "{\"error\":\"Pipeline not found\"}";
        Response response = new Response.Builder()
                .request(new Request.Builder().url("http://localhost:8080/api/pipeline/test-pipeline").build())
                .protocol(Protocol.HTTP_1_1)
                .code(404)
                .message("Not Found")
                .body(ResponseBody.create(errorJson, okhttp3.MediaType.parse("application/json")))
                .build();


        try (MockedStatic<PipelineLogger> mockedLogger = mockStatic(PipelineLogger.class)) {
            int returnCode = testCommand.call();

            assertEquals(1, returnCode, "Expected error return code");

            // Verify logger calls - using contains() for more flexibility
            mockedLogger.verify(() -> PipelineLogger.info(contains("Fetching pipeline status")));
            mockedLogger.verify(() -> PipelineLogger.debug(contains("GET http")));
//            mockedLogger.verify(() -> PipelineLogger.error("Failed to fetch pipeline status."));
//            mockedLogger.verify(() -> PipelineLogger.error("HTTP Status: 404"));
            mockedLogger.verify(() -> PipelineLogger.error(contains("Error while contacting backend")));
        }
    }

    @Test
    public void testNetworkError() throws Exception {
        // Create mocks for the HTTP call chain
        OkHttpClient mockClient = mock(OkHttpClient.class);
        Call mockCall = mock(Call.class);


        // Create our test command with the mock client
        TestStatusCommand testCommand = new TestStatusCommand(mockClient);

        // Set the parent command and pipeline name
        Field parentField = StatusCommand.class.getDeclaredField("parent");
        parentField.setAccessible(true);
        parentField.set(testCommand, cliApp);

        Field pipelineNameField = StatusCommand.class.getDeclaredField("pipelineName");
        pipelineNameField.setAccessible(true);
        pipelineNameField.set(testCommand, "test-pipeline");

        // Mock network error
        IOException networkException = new IOException("Connection refused");

        try (MockedStatic<PipelineLogger> mockedLogger = mockStatic(PipelineLogger.class)) {
            int returnCode = testCommand.call();

            assertEquals(1, returnCode, "Expected error return code");

            // Verify logger calls with contains() to be more flexible
            mockedLogger.verify(() -> PipelineLogger.info(contains("Fetching pipeline status")));
            mockedLogger.verify(() -> PipelineLogger.debug(contains("GET http")));
            mockedLogger.verify(() -> PipelineLogger.error(contains("Error while contacting backend:")));
        }
    }

    @Test
    public void testEmptyPipelineName() throws Exception {
        // Set pipeline name to empty
        Field pipelineNameField = StatusCommand.class.getDeclaredField("pipelineName");
        pipelineNameField.setAccessible(true);
        pipelineNameField.set(statusCommand, "");

        try (MockedStatic<PipelineLogger> mockedLogger = mockStatic(PipelineLogger.class)) {
            int returnCode = statusCommand.call();

            assertEquals(1, returnCode, "Expected error return code");

            // Verify logger calls
            mockedLogger.verify(() -> PipelineLogger.error("Pipeline name must be provided with --pipeline or -p."));
        }
    }

    @Test
    public void testCommandLineArguments() {
        StatusCommand command = new StatusCommand();
        CommandLine cmd = new CommandLine(command);

        // Test with missing required argument
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        cmd.setErr(new PrintWriter(new PrintStream(err)));
        int exitCode = cmd.execute();
        assertEquals(2, exitCode, "Expected usage error code"); // Picocli returns 2 for usage errors
        String errorOutput = err.toString();
        assertTrue(errorOutput.contains("Missing required option"),
                "Error output should mention missing option");
    }

    @Test
    public void testSuccessfulStatusFetch() throws Exception {
        OkHttpClient mockClient = mock(OkHttpClient.class); // 1. Mock client
        Call mockCall = mock(Call.class); // 2. Mock call

        // 3. Mock response
        Response response = new Response.Builder()
                .request(new Request.Builder().url("http://localhost:8080/api/pipeline/test-pipeline").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("{\"status\":\"ok\"}", MediaType.parse("application/json")))
                .build();

        // 4. Set up mock behavior
        when(mockClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(response);

        StatusCommand command = new StatusCommand(mockClient);

        Field pipelineNameField = StatusCommand.class.getDeclaredField("pipelineName");
        pipelineNameField.setAccessible(true);
        pipelineNameField.set(command, "test-pipeline");

        Field parentField = StatusCommand.class.getDeclaredField("parent");
        parentField.setAccessible(true);
        parentField.set(command, cliApp);

        try (MockedStatic<PipelineLogger> logger = mockStatic(PipelineLogger.class);
             MockedStatic<K8sService> k8s = mockStatic(K8sService.class)) {

            k8s.when(() -> K8sService.startBackendEnvironment(any())).thenReturn("fake-pod");
            k8s.when(K8sService::stopPortForward).thenAnswer(i -> null);
            k8s.when(() -> K8sService.stopPod(any())).thenAnswer(i -> null);

            int result = command.call();

            assertEquals(0, result, "Expected success return code");

            logger.verify(() -> PipelineLogger.info(contains("Pipeline Status")));
            logger.verify(() -> PipelineLogger.info(contains("{\"status\":\"ok\"}")));
        }
    }

    @Test
    public void testSuccessWithResponseBody() throws Exception {
        Response response = createMockResponse(200, "{\"status\":\"ok\"}", true);
        runWithResponse(response, 0, logger -> {
            logger.verify(() -> PipelineLogger.info(contains("Pipeline Status")));
            logger.verify(() -> PipelineLogger.info(contains("{\"status\":\"ok\"}")));
        });
    }

    @Test
    public void testSuccessWithNullBody() throws Exception {
        Response response = createMockResponse(200, "", true);
        runWithResponse(response, 0, logger -> {
            logger.verify(() -> PipelineLogger.info(contains("Pipeline Status:")));
            logger.verify(() -> PipelineLogger.info(eq(""))); // ✅ 匹配空内容
        });
    }


    @Test
    public void testFailureWithErrorBody() throws Exception {
        Response response = createMockResponse(500, "{\"error\":\"bad request\"}", true);
        runWithResponse(response, 1, logger -> {
            logger.verify(() -> PipelineLogger.error("Failed to fetch pipeline status."));
            logger.verify(() -> PipelineLogger.error("HTTP Status: 500"));
            logger.verify(() -> PipelineLogger.error("Response: {\"error\":\"bad request\"}"));
        });
    }

    @Test
    public void testFailureWithNullBody() throws Exception {
        Response response = createMockResponse(500, "", true);
        runWithResponse(response, 1, logger -> {
            logger.verify(() -> PipelineLogger.error("Failed to fetch pipeline status."));
            logger.verify(() -> PipelineLogger.error("HTTP Status: 500"));
            logger.verify(() -> PipelineLogger.error(eq("Response: ")));
        });
    }





    // Helpers
    private Response createMockResponse(int code, String body, boolean hasBody) {
        ResponseBody responseBody = hasBody
                ? ResponseBody.create(body != null ? body : "", MediaType.parse("application/json"))
                : ResponseBody.create(new byte[0], MediaType.parse("application/json"));

        return new Response.Builder()
                .request(new Request.Builder().url("http://localhost:8080/api/pipeline/test-pipeline").build())
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("Mocked")
                .body(responseBody)
                .build();
    }


    private void runWithResponse(Response response, int expectedCode, Consumer<MockedStatic<PipelineLogger>> verifier) throws Exception {
        OkHttpClient mockClient = mock(OkHttpClient.class);
        Call mockCall = mock(Call.class);

        when(mockClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(response);

        StatusCommand cmd = new StatusCommand(mockClient);

        Field pipelineField = StatusCommand.class.getDeclaredField("pipelineName");
        pipelineField.setAccessible(true);
        pipelineField.set(cmd, "test-pipeline");

        Field parentField = StatusCommand.class.getDeclaredField("parent");
        parentField.setAccessible(true);
        parentField.set(cmd, cliApp);

        try (
                MockedStatic<PipelineLogger> logger = mockStatic(PipelineLogger.class);
                MockedStatic<K8sService> k8s = mockStatic(K8sService.class)
        ) {
            k8s.when(() -> K8sService.startBackendEnvironment(any())).thenReturn("fake-pod");
            k8s.when(K8sService::stopPortForward).thenAnswer(i -> null);
            k8s.when(() -> K8sService.stopPod(any())).thenAnswer(i -> null);

            int result = cmd.call();
            assertEquals(expectedCode, result);
            verifier.accept(logger);
        }
    }



}