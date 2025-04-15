package edu.neu.cs6510.sp25.t1.common.validation.parser;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;
import edu.neu.cs6510.sp25.t1.common.model.Pipeline;
import edu.neu.cs6510.sp25.t1.common.validation.error.ValidationException;

class YamlParserTest {

    @TempDir
    private Path tempDir;

    private File validYamlFile;
    private File emptyYamlFile;
    private File invalidYamlFile;
    private File nonExistentFile;

    // Pattern to extract filename, line, and column from error message
    private static final Pattern ERROR_PATTERN =
            Pattern.compile("(.*?):(\\d+):(\\d+):(.*)");

    // Corrected YAML structure that matches Pipeline class properties
    private static final String VALID_YAML =
            "name: test-pipeline\n" +
                    "stages:\n" +
                    "  - build\n" +
                    "  - test\n" +
                    "jobs:\n" +
                    "  - name: compile\n" +
                    "    stage: build\n" +
                    "    image: gradle:8.12-jdk21\n" +
                    "    script:\n" +
                    "      - ./gradlew classes\n";

    private static final String INVALID_YAML =
            "name: test-pipeline\n" +
                    "stages:\n" +
                    "  - build\n" +
                    "  - test\n" +
                    "jobs:\n" +
                    "  - name: compile\n" +
                    "    stage: build\n" +
                    "    image: gradle:8.12-jdk21\n" +
                    "  invalid-indent\n";  // Invalid indentation

    @BeforeEach
    void setUp() throws IOException {
        // Create test YAML files
        validYamlFile = tempDir.resolve("valid.yaml").toFile();
        Files.writeString(validYamlFile.toPath(), VALID_YAML);

        emptyYamlFile = tempDir.resolve("empty.yaml").toFile();
        Files.writeString(emptyYamlFile.toPath(), "");

        invalidYamlFile = tempDir.resolve("invalid.yaml").toFile();
        Files.writeString(invalidYamlFile.toPath(), INVALID_YAML);

        nonExistentFile = tempDir.resolve("nonexistent.yaml").toFile();
    }

    @Test
    void parseYamlShouldSucceedWithValidYaml() throws Exception {
        try (MockedStatic<PipelineLogger> mockLogger = mockStatic(PipelineLogger.class)) {
            Pipeline pipeline = YamlParser.parseYaml(validYamlFile);

            // Verify pipeline was parsed correctly
            assertNotNull(pipeline);
            assertEquals("test-pipeline", pipeline.getName());

            // Verify logging occurred
            mockLogger.verify(() -> PipelineLogger.info(contains("Parsing YAML file")));
            mockLogger.verify(() -> PipelineLogger.info(contains("YAML structure validated successfully")));
        }
    }

    @Test
    void parseYamlShouldThrowExceptionWithEmptyFile() {
        try (MockedStatic<PipelineLogger> mockLogger = mockStatic(PipelineLogger.class)) {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> YamlParser.parseYaml(emptyYamlFile));

            // Verify exception message contains expected information
            String message = exception.getMessage();
            assertTrue(message.contains(emptyYamlFile.getName()),
                    "Exception message should contain the filename");
            assertTrue(message.contains("YAML file is empty"),
                    "Exception message should indicate file is empty");

            // Verify logging occurred
            mockLogger.verify(() -> PipelineLogger.info(contains("Parsing YAML file")));
            mockLogger.verify(() -> PipelineLogger.error(contains("YAML file is empty")));
        }
    }

    @Test
    void parseYamlShouldThrowExceptionWithInvalidYaml() {
        try (MockedStatic<PipelineLogger> mockLogger = mockStatic(PipelineLogger.class)) {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> YamlParser.parseYaml(invalidYamlFile));

            // Verify exception message contains expected information
            String message = exception.getMessage();
            assertTrue(message.contains(invalidYamlFile.getName()),
                    "Exception message should contain the filename");
            assertTrue(message.contains("YAML parsing error"),
                    "Exception message should indicate parsing error");

            // Verify logging occurred
            mockLogger.verify(() -> PipelineLogger.info(contains("Parsing YAML file")));
            mockLogger.verify(() -> PipelineLogger.error(contains("YAML parsing error")));
        }
    }

    @Test
    void parseYamlShouldThrowExceptionWithNonExistentFile() {
        try (MockedStatic<PipelineLogger> mockLogger = mockStatic(PipelineLogger.class)) {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> YamlParser.parseYaml(nonExistentFile));

            // Verify exception message contains expected information
            String message = exception.getMessage();
            assertTrue(message.contains(nonExistentFile.getName()),
                    "Exception message should contain the filename");
            assertTrue(message.contains("YAML file not found"),
                    "Exception message should indicate file not found");

            // Verify logging occurred
            mockLogger.verify(() -> PipelineLogger.error(contains("YAML file not found")));
        }
    }


}