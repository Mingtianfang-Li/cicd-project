package edu.neu.cs6510.sp25.t1.cli.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateVolumeResponse;
import com.github.dockerjava.core.DockerClientImpl;
import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DockerVolumeUtilTest {

    @TempDir
    Path tempDir;

    private File testFile;

    @BeforeEach
    void setUp() throws Exception {
        Path nestedPath = tempDir.resolve("a/b/input.yaml");
        nestedPath.toFile().getParentFile().mkdirs();
        assertTrue(nestedPath.toFile().createNewFile());
        testFile = nestedPath.toFile();
    }


    @Test
    void testCreateVolumeFromHostDirSuccess() throws Exception {
        UUID fakeUUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        try (
                MockedStatic<UUID> uuidMock = mockStatic(UUID.class);
                MockedStatic<PipelineLogger> loggerMock = mockStatic(PipelineLogger.class);
                MockedStatic<DockerClientImpl> implMock = mockStatic(DockerClientImpl.class);
                MockedConstruction<ProcessBuilder> mockedConstruction = mockConstruction(ProcessBuilder.class,
                        (mockBuilder, context) -> {
                            Process mockProcess = mock(Process.class);
                            when(mockProcess.waitFor()).thenReturn(0);
                            when(mockBuilder.start()).thenReturn(mockProcess);
                        })
        ) {
            // Mock UUID
            uuidMock.when(UUID::randomUUID).thenReturn(fakeUUID);

            // Mock DockerClient
            DockerClient mockDockerClient = mock(DockerClient.class);
            implMock.when(() -> DockerClientImpl.getInstance(any(), any())).thenReturn(mockDockerClient);

            // Simulate volume not found initially
            doThrow(new RuntimeException("volume not found"))
                    .when(mockDockerClient).inspectVolumeCmd(anyString());

            // Mock createVolumeCmd chain
            var createVolumeCmd = mock(com.github.dockerjava.api.command.CreateVolumeCmd.class);
            when(mockDockerClient.createVolumeCmd()).thenReturn(createVolumeCmd);
            when(createVolumeCmd.withName(anyString())).thenReturn(createVolumeCmd);
            when(createVolumeCmd.withDriver(anyString())).thenReturn(createVolumeCmd);
            when(createVolumeCmd.exec()).thenReturn(mock(CreateVolumeResponse.class));

            // 🧪 Execute
            String result = DockerVolumeUtil.createVolumeFromHostDir(testFile.getAbsolutePath());
            System.out.println(result);

            // ✅ Assert result contains our UUID
            assertNotNull(result);
            assertTrue(result.contains(fakeUUID.toString()));
        }
    }

    @Test
    void testCreateVolumeFromHostDirIOException() throws Exception {
        UUID fakeUUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        try (
                MockedStatic<UUID> uuidMock = mockStatic(UUID.class);
                MockedStatic<PipelineLogger> loggerMock = mockStatic(PipelineLogger.class);
                MockedStatic<DockerClientImpl> implMock = mockStatic(DockerClientImpl.class);
                MockedConstruction<ProcessBuilder> mockedConstruction = mockConstruction(ProcessBuilder.class,
                        (mockBuilder, context) -> {
                            when(mockBuilder.start()).thenThrow(new IOException("simulated IOException"));
                        })
        ) {
            // Mock UUID
            uuidMock.when(UUID::randomUUID).thenReturn(fakeUUID);

            // Mock DockerClient
            DockerClient mockDockerClient = mock(DockerClient.class);
            implMock.when(() -> DockerClientImpl.getInstance(any(), any())).thenReturn(mockDockerClient);

            // Simulate volume not found initially
            doThrow(new RuntimeException("volume not found"))
                    .when(mockDockerClient).inspectVolumeCmd(anyString());

            // Mock createVolumeCmd chain
            var createVolumeCmd = mock(com.github.dockerjava.api.command.CreateVolumeCmd.class);
            when(mockDockerClient.createVolumeCmd()).thenReturn(createVolumeCmd);
            when(createVolumeCmd.withName(anyString())).thenReturn(createVolumeCmd);
            when(createVolumeCmd.withDriver(anyString())).thenReturn(createVolumeCmd);
            when(createVolumeCmd.exec()).thenReturn(mock(CreateVolumeResponse.class));

            // 🧪 Execute
            String result = DockerVolumeUtil.createVolumeFromHostDir(testFile.getAbsolutePath());

            // ✅ Assert it returns null
            assertNull(result);

            // ✅ Optional: Verify logger was called
            loggerMock.verify(() -> PipelineLogger.error(contains("Docker volume creation or file copy failed")));
        }
    }


    @Test
    void testCreateVolumeFromHostDirInterruptedException() throws Exception {
        UUID fakeUUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        try (
                MockedStatic<UUID> uuidMock = mockStatic(UUID.class);
                MockedStatic<PipelineLogger> loggerMock = mockStatic(PipelineLogger.class);
                MockedStatic<DockerClientImpl> implMock = mockStatic(DockerClientImpl.class);
                MockedConstruction<ProcessBuilder> mockedConstruction = mockConstruction(ProcessBuilder.class,
                        (mockBuilder, context) -> {
                            Process mockProcess = mock(Process.class);
                            when(mockBuilder.start()).thenReturn(mockProcess);
                            when(mockProcess.waitFor()).thenThrow(new InterruptedException("simulated InterruptedException"));
                        })
        ) {
            // Mock UUID
            uuidMock.when(UUID::randomUUID).thenReturn(fakeUUID);

            // Mock DockerClient
            DockerClient mockDockerClient = mock(DockerClient.class);
            implMock.when(() -> DockerClientImpl.getInstance(any(), any())).thenReturn(mockDockerClient);

            // Simulate volume not found initially
            doThrow(new RuntimeException("volume not found"))
                    .when(mockDockerClient).inspectVolumeCmd(anyString());

            // Mock createVolumeCmd chain
            var createVolumeCmd = mock(com.github.dockerjava.api.command.CreateVolumeCmd.class);
            when(mockDockerClient.createVolumeCmd()).thenReturn(createVolumeCmd);
            when(createVolumeCmd.withName(anyString())).thenReturn(createVolumeCmd);
            when(createVolumeCmd.withDriver(anyString())).thenReturn(createVolumeCmd);
            when(createVolumeCmd.exec()).thenReturn(mock(CreateVolumeResponse.class));

            // 🧪 Execute
            String result = DockerVolumeUtil.createVolumeFromHostDir(testFile.getAbsolutePath());

            // ✅ Assert it returns null
            assertNull(result);

            // ✅ Optional: Verify logger was called
            loggerMock.verify(() -> PipelineLogger.error(contains("Docker volume creation or file copy failed")));
        }
    }

    @Test
    void testCreateVolumeFromHostDirCopyProcessFailure() throws Exception {
        UUID fakeUUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        try (
                // Mock UUID.randomUUID() to return a fixed UUID
                MockedStatic<UUID> uuidMock = mockStatic(UUID.class);
                MockedStatic<PipelineLogger> loggerMock = mockStatic(PipelineLogger.class);
                MockedStatic<DockerClientImpl> implMock = mockStatic(DockerClientImpl.class);
                // Mock all ProcessBuilder constructor calls
                MockedConstruction<ProcessBuilder> mockedConstruction = mockConstruction(ProcessBuilder.class,
                        (mockBuilder, context) -> {
                            // Mock the process to simulate a failure (non-zero exit code)
                            Process mockProcess = mock(Process.class);
                            when(mockBuilder.start()).thenReturn(mockProcess);
                            when(mockProcess.waitFor()).thenReturn(1); // Simulate process failure
                        })
        ) {
            // Mock UUID generation
            uuidMock.when(UUID::randomUUID).thenReturn(fakeUUID);

            // Mock DockerClient and its methods
            DockerClient mockDockerClient = mock(DockerClient.class);
            implMock.when(() -> DockerClientImpl.getInstance(any(), any())).thenReturn(mockDockerClient);

            // Simulate volume not found so that volume creation path is triggered
            doThrow(new RuntimeException("volume not found"))
                    .when(mockDockerClient).inspectVolumeCmd(anyString());

            // Mock volume creation chain
            var createVolumeCmd = mock(com.github.dockerjava.api.command.CreateVolumeCmd.class);
            when(mockDockerClient.createVolumeCmd()).thenReturn(createVolumeCmd);
            when(createVolumeCmd.withName(anyString())).thenReturn(createVolumeCmd);
            when(createVolumeCmd.withDriver(anyString())).thenReturn(createVolumeCmd);
            when(createVolumeCmd.exec()).thenReturn(mock(CreateVolumeResponse.class));

            // Execute the method under test
            String result = DockerVolumeUtil.createVolumeFromHostDir(testFile.getAbsolutePath());

            // Verify that the method returns null due to failed file copy
            assertNull(result);

            // Verify the error logger was called with the expected message
            loggerMock.verify(() ->
                    PipelineLogger.error(contains("Failed to copy files from host to Docker volume."))
            );
        }
    }

}
