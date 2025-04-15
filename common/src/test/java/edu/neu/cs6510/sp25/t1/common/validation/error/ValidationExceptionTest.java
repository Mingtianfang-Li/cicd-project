package edu.neu.cs6510.sp25.t1.common.validation.error;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ValidationExceptionTest {

    @Test
    void testConstructorWithSingleMessage() {
        // Arrange
        String errorMessage = "Pipeline name is missing.";

        // Act
        ValidationException exception = new ValidationException(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testConstructorWithListOfSingleError() {
        // Arrange
        List<String> errors = Collections.singletonList("Stage configuration is invalid.");

        // Act
        ValidationException exception = new ValidationException(errors);

        // Assert
        assertEquals("Stage configuration is invalid.", exception.getMessage());
    }

    @Test
    void testConstructorWithListOfMultipleErrors() {
        // Arrange
        List<String> errors = Arrays.asList(
                "Pipeline name is missing.",
                "Stage 'build' has no jobs.",
                "Job 'test' references unknown stage."
        );

        // Act
        ValidationException exception = new ValidationException(errors);

        // Assert
        assertEquals("Pipeline name is missing.\nStage 'build' has no jobs.\nJob 'test' references unknown stage.",
                exception.getMessage());
    }

    @Test
    void testConstructorWithEmptyErrorsList() {
        // Arrange
        List<String> errors = Collections.emptyList();

        // Act
        ValidationException exception = new ValidationException(errors);

        // Assert
        assertEquals("", exception.getMessage());
    }

    @Test
    void testConstructorWithFilenameLineColumnMessage() {
        // Arrange
        String filename = "pipeline.yaml";
        int line = 15;
        int column = 8;
        String message = "Invalid stage reference.";

        // Act
        ValidationException exception = new ValidationException(filename, line, column, message);

        // Assert
        assertEquals("pipeline.yaml:15:8: Invalid stage reference.", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideLocationAndMessages")
    void testConstructorWithLocationAndMessage(String filename, int line, int column,
                                               String path, String message, String expected) {
        // Arrange
        ErrorHandler.Location location = new ErrorHandler.Location(filename, line, column, path);

        // Act
        ValidationException exception = new ValidationException(location, message);

        // Assert
        assertEquals(expected, exception.getMessage());
    }

    private static Stream<Arguments> provideLocationAndMessages() {
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
                        ".pipelines/pipeline.yaml:100:20: No jobs defined for stage")
        );
    }

    @Test
    void testSuperclassInheritance() {
        // Verify ValidationException is a subclass of Exception
        ValidationException exception = new ValidationException("Test message");
        assertTrue(exception instanceof Exception);
    }

    @Test
    void testConstructorWithSpecialCharactersInMessage() {
        // Arrange
        String message = "Error: Invalid character '*' at position $variable[index].";

        // Act
        ValidationException exception = new ValidationException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testConstructorWithNonAsciiCharacters() {
        // Arrange
        String message = "错误：阶段名称缺失。"; // "Error: Stage name is missing." in Chinese

        // Act
        ValidationException exception = new ValidationException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testConstructorWithLongPathNames() {
        // Arrange
        String filename = "extremely_long_filename_with_many_characters_and_underscores_that_might_cause_issues.yaml";
        int line = 42;
        int column = 10;
        String message = "This is a test for very long filenames that might cause formatting issues.";

        // Act
        ValidationException exception = new ValidationException(filename, line, column, message);

        // Assert
        assertEquals(filename + ":42:10: " + message, exception.getMessage());
    }

    @Test
    void testNullMessageInListConstructor() {
        // Arrange
        List<String> errors = Arrays.asList("Valid message", null, "Another valid message");

        // Act
        ValidationException exception = new ValidationException(errors);

        // Assert
        assertEquals("Valid message\nnull\nAnother valid message", exception.getMessage());
    }

    @Test
    void testMessagePreservation() {
        // Test that getMessage() returns the original error message
        String errorMessage = "This is an error message that should be preserved exactly as is.";
        ValidationException exception = new ValidationException(errorMessage);

        String retrievedMessage = exception.getMessage();

        assertEquals(errorMessage, retrievedMessage,
                "The error message should be preserved exactly");
    }
}