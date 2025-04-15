package edu.neu.cs6510.sp25.t1.common.validation.error;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorHandlerTest {

    @Test
    void testLocationConstructor() {
        // Arrange
        String filename = "test.yaml";
        int line = 10;
        int column = 15;
        String path = "pipeline.stages[0].jobs[2].name";

        // Act
        ErrorHandler.Location location = new ErrorHandler.Location(filename, line, column, path);

        // Assert
        assertEquals(path, location.getPath());
    }

    @Test
    void testFormatValidationError() {
        // Arrange
        String filename = "pipeline.yaml";
        int line = 5;
        int column = 8;
        String path = "pipeline.stages[1]";
        String message = "Invalid stage configuration";
        ErrorHandler.Location location = new ErrorHandler.Location(filename, line, column, path);

        // Act
        String result = ErrorHandler.formatValidationError(location, message);

        // Assert
        assertEquals("pipeline.yaml:5:8: Invalid stage configuration", result);
    }

    @ParameterizedTest
    @MethodSource("provideLocationsAndMessages")
    void testFormatValidationErrorWithVariousInputs(
            String filename, int line, int column, String path, String message, String expected) {
        // Arrange
        ErrorHandler.Location location = new ErrorHandler.Location(filename, line, column, path);

        // Act
        String result = ErrorHandler.formatValidationError(location, message);

        // Assert
        assertEquals(expected, result);
    }

    private static Stream<Arguments> provideLocationsAndMessages() {
        return Stream.of(
                Arguments.of(
                        "config.yaml", 1, 1, "root",
                        "Missing required field",
                        "config.yaml:1:1: Missing required field"),
                Arguments.of(
                        "test.yml", 25, 30, "pipeline.jobs[0]",
                        "Invalid job configuration: Docker image not specified",
                        "test.yml:25:30: Invalid job configuration: Docker image not specified"),
                Arguments.of(
                        ".pipelines/pipeline.yaml", 100, 20, "pipeline.stages[2].jobs",
                        "No jobs defined for stage",
                        ".pipelines/pipeline.yaml:100:20: No jobs defined for stage"),
                Arguments.of(
                        "/home/user/project/.pipelines/config.yaml", 0, 0, "",
                        "File is empty",
                        "/home/user/project/.pipelines/config.yaml:0:0: File is empty")
        );
    }

    @Test
    void testLocationPathGetterOnly() {
        // Arrange
        String filename = "test.yaml";
        int line = 42;
        int column = 7;
        String path = "pipeline.stages[3].jobs[1].script[0]";
        ErrorHandler.Location location = new ErrorHandler.Location(filename, line, column, path);

        // Act & Assert
        assertEquals(path, location.getPath());
        // The other fields are private with no getters, so we can only test the path
    }

    @Test
    void testFormatValidationErrorWithEmptyMessage() {
        // Arrange
        ErrorHandler.Location location = new ErrorHandler.Location("file.yml", 1, 2, "path");
        String message = "";

        // Act
        String result = ErrorHandler.formatValidationError(location, message);

        // Assert
        assertEquals("file.yml:1:2: ", result);
    }

    @Test
    void testFormatValidationErrorWithNullMessage() {
        // Arrange
        ErrorHandler.Location location = new ErrorHandler.Location("file.yml", 1, 2, "path");
        String message = null;

        // Act
        String result = ErrorHandler.formatValidationError(location, message);

        // Assert
        assertEquals("file.yml:1:2: null", result);
    }

    @Test
    void testFormatValidationErrorWithSpecialCharacters() {
        // Arrange
        ErrorHandler.Location location = new ErrorHandler.Location("special_file$#.yaml", 10, 20, "path.with.special[chars]");
        String message = "Error: Unexpected character: '*'";

        // Act
        String result = ErrorHandler.formatValidationError(location, message);

        // Assert
        assertEquals("special_file$#.yaml:10:20: Error: Unexpected character: '*'", result);
    }

    @Test
    void testFormatValidationErrorWithExtremeValues() {
        // Arrange
        ErrorHandler.Location location = new ErrorHandler.Location("file.yaml", Integer.MAX_VALUE, Integer.MAX_VALUE, "path");
        String message = "Extreme values test";

        // Act
        String result = ErrorHandler.formatValidationError(location, message);

        // Assert
        assertEquals("file.yaml:" + Integer.MAX_VALUE + ":" + Integer.MAX_VALUE + ": Extreme values test", result);
    }
}