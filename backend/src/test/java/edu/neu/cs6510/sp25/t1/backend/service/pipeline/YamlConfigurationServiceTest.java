package edu.neu.cs6510.sp25.t1.backend.service.pipeline;

import edu.neu.cs6510.sp25.t1.backend.utils.YamlPipelineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class YamlConfigurationServiceTest {

    @InjectMocks
    private YamlConfigurationService yamlConfigurationService;

    @TempDir
    Path tempDir;

    private Path validYamlFile;
    private Path nonExistentFile;
    private Path nonReadableFile;

    @BeforeEach
    public void setUp() throws IOException {
        // Create a valid YAML file
        validYamlFile = tempDir.resolve("valid-pipeline.yaml");
        Files.writeString(validYamlFile, "name: test-pipeline\nstages:\n  - build\n  - test");

        // Define a path for a non-existent file
        nonExistentFile = tempDir.resolve("non-existent.yaml");

        // For the non-readable file test, we will handle it separately in the test method
    }

    @Test
    public void testResolveAndValidatePipelinePath_ValidPath() {
        // Act
        Path result = yamlConfigurationService.resolveAndValidatePipelinePath(validYamlFile.toString());

        // Assert
        assertEquals(validYamlFile.toAbsolutePath(), result.toAbsolutePath());
    }

    @Test
    public void testResolveAndValidatePipelinePath_RelativePath() throws IOException {
        // Arrange
        String originalUserDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", tempDir.toString());
            String relativePath = validYamlFile.getFileName().toString();

            // Act
            Path result = yamlConfigurationService.resolveAndValidatePipelinePath(relativePath);

            // Assert
            assertEquals(validYamlFile.toAbsolutePath(), result.toAbsolutePath());
        } finally {
            // Restore the original user.dir
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    public void testResolveAndValidatePipelinePath_NullPath() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            yamlConfigurationService.resolveAndValidatePipelinePath(null);
        });

        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    public void testResolveAndValidatePipelinePath_EmptyPath() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            yamlConfigurationService.resolveAndValidatePipelinePath("   ");
        });

        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }

    @Test
    public void testResolveAndValidatePipelinePath_NonExistentFile() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            yamlConfigurationService.resolveAndValidatePipelinePath(nonExistentFile.toString());
        });

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    public void testResolveAndValidatePipelinePath_NonReadableFile() throws IOException {
        // This test may not work on all systems due to file permission limitations
        // Particularly in CI environments or containerized tests
        // We'll implement it but be aware it might be skipped

        Path nonReadableFile = tempDir.resolve("non-readable.yaml");
        Files.writeString(nonReadableFile, "name: test-pipeline\n");

        try {
            // Try to make the file non-readable - this might not work on all systems
            boolean setReadable = nonReadableFile.toFile().setReadable(false);
            if (!setReadable || nonReadableFile.toFile().canRead()) {
                System.out.println("Skipping non-readable file test as file permissions cannot be modified.");
                return;
            }

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                yamlConfigurationService.resolveAndValidatePipelinePath(nonReadableFile.toString());
            });

            assertTrue(exception.getMessage().contains("not readable"));
        } finally {
            // Reset permissions to allow cleanup
            nonReadableFile.toFile().setReadable(true);
        }
    }

    @Test
    public void testParseAndValidatePipelineYaml_ValidYaml() throws Exception {
        // Arrange
        Map<String, Object> expectedConfig = new HashMap<>();
        expectedConfig.put("name", "test-pipeline");

        try (MockedStatic<YamlPipelineUtils> mockedStatic = mockStatic(YamlPipelineUtils.class)) {
            mockedStatic.when(() -> YamlPipelineUtils.readPipelineYaml(anyString()))
                    .thenReturn(expectedConfig);

            mockedStatic.when(() -> YamlPipelineUtils.validatePipelineConfig(any(Map.class)))
                    .thenAnswer(invocation -> null);

            // Act
            Map<String, Object> result = yamlConfigurationService.parseAndValidatePipelineYaml(validYamlFile.toString());

            // Assert
            assertEquals(expectedConfig, result);

            // Verify the calls
            mockedStatic.verify(() -> YamlPipelineUtils.readPipelineYaml(validYamlFile.toString()));
            mockedStatic.verify(() -> YamlPipelineUtils.validatePipelineConfig(expectedConfig));
        }
    }

    @Test
    public void testParseAndValidatePipelineYaml_ReadFailure() throws Exception {
        // Arrange
        String errorMessage = "File read error";

        try (MockedStatic<YamlPipelineUtils> mockedStatic = mockStatic(YamlPipelineUtils.class)) {
            mockedStatic.when(() -> YamlPipelineUtils.readPipelineYaml(anyString()))
                    .thenThrow(new IOException(errorMessage));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                yamlConfigurationService.parseAndValidatePipelineYaml(validYamlFile.toString());
            });

            assertTrue(exception.getMessage().contains("YAML parsing failed"));
            assertTrue(exception.getCause() instanceof IOException);
            assertEquals(errorMessage, exception.getCause().getMessage());
        }
    }

    @Test
    public void testParseAndValidatePipelineYaml_ValidationFailure() throws Exception {
        // Arrange
        Map<String, Object> configWithError = new HashMap<>();
        String errorMessage = "Missing required fields";

        try (MockedStatic<YamlPipelineUtils> mockedStatic = mockStatic(YamlPipelineUtils.class)) {
            mockedStatic.when(() -> YamlPipelineUtils.readPipelineYaml(anyString()))
                    .thenReturn(configWithError);

            mockedStatic.when(() -> YamlPipelineUtils.validatePipelineConfig(any(Map.class)))
                    .thenThrow(new IllegalArgumentException(errorMessage));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                yamlConfigurationService.parseAndValidatePipelineYaml(validYamlFile.toString());
            });

            assertTrue(exception.getMessage().contains("YAML parsing failed"));
            assertTrue(exception.getCause() instanceof IllegalArgumentException);
            assertEquals(errorMessage, exception.getCause().getMessage());
        }
    }
}