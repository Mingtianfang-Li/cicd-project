package edu.neu.cs6510.sp25.t1.cli.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CliBackendClientTest {

    private static final String BASE_URL = "http://test-server.com";

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockResponse;

    private CliBackendClient clientUnderTest;

    @BeforeEach
    void setUp() throws Exception {
        // Set up default response behavior
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"status\":\"success\"}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        // Create client and inject mock HttpClient using reflection
        clientUnderTest = new CliBackendClient(BASE_URL);

        // Use reflection to replace the httpClient field with our mock
        Field httpClientField = CliBackendClient.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(clientUnderTest, mockHttpClient);
    }

    @Test
    void testFetchPipelineReportWithRunNumber() throws IOException, InterruptedException {
        // Arrange
        String pipelineName = "test-pipeline";
        Integer runNumber = 123;
        String stageName = "build";
        String jobName = "compile";

        // Act
        String result = clientUnderTest.fetchPipelineReport(pipelineName, runNumber, stageName, jobName);

        // Assert
        assertEquals("{\"status\":\"success\"}", result);

        // Verify the correct URL was constructed
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

        URI capturedUri = requestCaptor.getValue().uri();
        String expectedUrl = BASE_URL + "/api/report/pipeline/test-pipeline/run/123/stage/build/job/compile";
        assertEquals(expectedUrl, capturedUri.toString());
    }

    @Test
    void testFetchPipelineHistoryWithNoStageOrJob() throws IOException, InterruptedException {
        // Arrange
        String pipelineName = "test-pipeline";
        Integer runNumber = -1; // History mode

        // Act
        String result = clientUnderTest.fetchPipelineReport(pipelineName, runNumber, null, null);

        // Assert
        assertEquals("{\"status\":\"success\"}", result);

        // Verify the correct URL was constructed
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

        URI capturedUri = requestCaptor.getValue().uri();
        String expectedUrl = BASE_URL + "/api/report/pipeline/history/test-pipeline";
        assertEquals(expectedUrl, capturedUri.toString());
    }

    @Test
    void testFetchPipelineHistoryWithStageOnly() throws IOException, InterruptedException {
        // Arrange
        String pipelineName = "test-pipeline";
        Integer runNumber = null; // History mode
        String stageName = "test-stage";

        // Act
        String result = clientUnderTest.fetchPipelineReport(pipelineName, runNumber, stageName, null);

        // Assert
        assertEquals("{\"status\":\"success\"}", result);

        // Verify the correct URL was constructed
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

        URI capturedUri = requestCaptor.getValue().uri();
        String expectedUrl = BASE_URL + "/api/report/pipeline/history/test-pipeline?stage=test-stage";
        assertEquals(expectedUrl, capturedUri.toString());
    }

    @Test
    void testFetchPipelineHistoryWithStageAndJob() throws IOException, InterruptedException {
        // Arrange
        String pipelineName = "test-pipeline";
        Integer runNumber = -1; // History mode
        String stageName = "test-stage";
        String jobName = "test-job";

        // Act
        String result = clientUnderTest.fetchPipelineReport(pipelineName, runNumber, stageName, jobName);

        // Assert
        assertEquals("{\"status\":\"success\"}", result);

        // Verify the correct URL was constructed
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

        URI capturedUri = requestCaptor.getValue().uri();
        String expectedUrl = BASE_URL + "/api/report/pipeline/history/test-pipeline?stage=test-stage&job=test-job";
        assertEquals(expectedUrl, capturedUri.toString());
    }

    @Test
    void testApiRequestFailsWith400Status() throws IOException, InterruptedException {
        // Arrange
        when(mockResponse.statusCode()).thenReturn(404);
        when(mockResponse.body()).thenReturn("{\"error\":\"Pipeline not found\"}");

        // Act & Assert
        Exception exception = assertThrows(IOException.class, () -> {
            clientUnderTest.fetchPipelineReport("test-pipeline", 123, null, null);
        });

        assertTrue(exception.getMessage().contains("API request failed with status code: 404"));
    }

}