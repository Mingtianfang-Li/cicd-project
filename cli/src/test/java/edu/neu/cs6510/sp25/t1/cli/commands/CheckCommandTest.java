package edu.neu.cs6510.sp25.t1.cli.commands;

import edu.neu.cs6510.sp25.t1.backend.utils.YamlPipelineUtils;
import edu.neu.cs6510.sp25.t1.common.utils.GitCloneUtil;
import edu.neu.cs6510.sp25.t1.common.validation.error.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CheckCommandTest {

    @TempDir
    private Path tempDir;

    private File createTempYamlFile(String content) throws IOException {
        File tempFile = tempDir.resolve("pipeline.yaml").toFile();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        return tempFile;
    }

    @Test
    void testValidYaml() throws Exception {
        File file = createTempYamlFile("pipeline: test");

        try (
                MockedStatic<GitCloneUtil> gitMock = mockStatic(GitCloneUtil.class);
                MockedStatic<YamlPipelineUtils> yamlMock = mockStatic(YamlPipelineUtils.class)
        ) {
            gitMock.when(() -> GitCloneUtil.isInsideGitRepo(any(File.class))).thenReturn(true);
            yamlMock.when(() -> YamlPipelineUtils.readPipelineYaml(file.getAbsolutePath()))
                    .thenReturn(Map.of("pipeline", "test"));
            yamlMock.when(() -> YamlPipelineUtils.validatePipelineConfig(any()))
                    .thenAnswer(invocation -> null); // simulate void method

            CheckCommand command = new CheckCommand();
            String[] args = {"-f", file.getAbsolutePath()};
            int exitCode = new CommandLine(command).execute(args);
            assertEquals(0, exitCode);
        }
    }

    @Test
    void testValidationExceptionWrapped() throws Exception {
        File file = createTempYamlFile("pipeline: test");

        try (
                MockedStatic<GitCloneUtil> gitMock = mockStatic(GitCloneUtil.class);
                MockedStatic<YamlPipelineUtils> yamlMock = mockStatic(YamlPipelineUtils.class)
        ) {
            gitMock.when(() -> GitCloneUtil.isInsideGitRepo(any(File.class))).thenReturn(true);
            yamlMock.when(() -> YamlPipelineUtils.readPipelineYaml(file.getAbsolutePath()))
                    .thenReturn(Map.of("pipeline", "test"));

            yamlMock.when(() -> YamlPipelineUtils.validatePipelineConfig(any()))
                    .thenAnswer(invocation -> {
                        throw new RuntimeException(new ValidationException("Invalid pipeline"));
                    });

            CheckCommand command = new CheckCommand();
            String[] args = {"-f", file.getAbsolutePath()};
            int exitCode = new CommandLine(command).execute(args);
            assertEquals(1, exitCode);
        }
    }

    @Test
    void testInvalidExtension() {
        CheckCommand command = new CheckCommand();
        String[] args = {"-f", "invalid.txt"};
        int exitCode = new CommandLine(command).execute(args);
        assertEquals(1, exitCode);
    }

    @Test
    void testNotInGitRepo() throws Exception {
        File file = createTempYamlFile("pipeline: test");

        try (MockedStatic<GitCloneUtil> gitMock = mockStatic(GitCloneUtil.class)) {
            gitMock.when(() -> GitCloneUtil.isInsideGitRepo(any(File.class))).thenReturn(false);

            CheckCommand command = new CheckCommand();
            String[] args = {"-f", file.getAbsolutePath()};
            int exitCode = new CommandLine(command).execute(args);
            assertEquals(1, exitCode);
        }
    }
}
