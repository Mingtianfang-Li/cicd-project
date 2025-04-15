package edu.neu.cs6510.sp25.t1.cli.commands;

import edu.neu.cs6510.sp25.t1.cli.commands.DryRunCommand;
import edu.neu.cs6510.sp25.t1.backend.utils.YamlPipelineUtils;
import edu.neu.cs6510.sp25.t1.common.utils.GitCloneUtil;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;


import java.util.Map;

class DryRunCommandTest {

    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;
    private File tempYamlFile;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        if (tempYamlFile != null && tempYamlFile.exists()) {
            tempYamlFile.delete();
        }
    }

    private void runWithYaml(String yamlContent) throws Exception {
        tempYamlFile = File.createTempFile("dry-run-test", ".yaml");
        try (FileWriter writer = new FileWriter(tempYamlFile)) {
            writer.write(yamlContent);
        }

        try (MockedStatic<GitCloneUtil> gitMock = mockStatic(GitCloneUtil.class);
             MockedStatic<YamlPipelineUtils> yamlMock = mockStatic(YamlPipelineUtils.class)) {

            gitMock.when(() -> GitCloneUtil.isInsideGitRepo(any(File.class))).thenReturn(true);
            yamlMock.when(() -> YamlPipelineUtils.readPipelineYaml(anyString()))
                    .thenCallRealMethod();
            yamlMock.when(() -> YamlPipelineUtils.validatePipelineConfig(any(Map.class)))
                    .thenAnswer(invocation -> null);

            DryRunCommand command = new DryRunCommand();
            command.setFilePath(tempYamlFile.getAbsolutePath());
            int result = command.call();
            assertEquals(0, result);
        }
    }

    @Test
    void testValidYamlExecutionPlan() throws Exception {
        runWithYaml(
                "stages:\n" +
                        "  - build\n" +
                        "  - deploy\n" +
                        "jobs:\n" +
                        "  - name: build-job\n" +
                        "    stage: build\n" +
                        "    script: [\"echo build\"]\n" +
                        "  - name: deploy-job\n" +
                        "    stage: deploy\n" +
                        "    script: [\"echo deploy\"]\n" +
                        "    dependencies: [\"build-job\"]\n"
        );

        String output = outContent.toString();
        assertTrue(output.contains("build-job"));
        assertTrue(output.contains("deploy-job"));
    }

    @Test
    void testEmptyJobsReturnsEmptyExecutionPlan() throws Exception {
        runWithYaml(
                "stages:\n" +
                        "  - build\n" +
                        "  - test\n"
        );

        String output = outContent.toString();
        assertTrue(output.contains("build:\n"));
        assertTrue(output.contains("test:\n"));
    }

    @Test
    void testMissingScriptDefaultsToEcho() throws Exception {
        runWithYaml(
                "stages:\n" +
                        "  - build\n" +
                        "jobs:\n" +
                        "  - name: no-script-job\n" +
                        "    stage: build\n"
        );

        String output = outContent.toString();
        assertTrue(output.contains("- echo \"No script specified\""));
    }

    @Test
    void testScriptAsSingleLineString() throws Exception {
        runWithYaml(
                "stages:\n" +
                        "  - build\n" +
                        "jobs:\n" +
                        "  - name: single-line-job\n" +
                        "    stage: build\n" +
                        "    script: \"echo Hello\"\n"
        );

        String output = outContent.toString();
        assertTrue(output.contains("- echo Hello"));
    }

    @Test
    void testScriptAsMultilineString() throws Exception {
        runWithYaml(
                "stages:\n" +
                        "  - build\n" +
                        "jobs:\n" +
                        "  - name: multi-line-job\n" +
                        "    stage: build\n" +
                        "    script: |\n" +
                        "      echo Line1\n" +
                        "      echo Line2\n"
        );

        String output = outContent.toString();
        assertTrue(output.contains("- echo Line1"));
        assertTrue(output.contains("- echo Line2"));
    }

    @Test
    void testScriptAsList() throws Exception {
        runWithYaml(
                "stages:\n" +
                        "  - build\n" +
                        "jobs:\n" +
                        "  - name: list-script-job\n" +
                        "    stage: build\n" +
                        "    script:\n" +
                        "      - echo A\n" +
                        "      - echo B\n"
        );

        String output = outContent.toString();
        assertTrue(output.contains("- echo A"));
        assertTrue(output.contains("- echo B"));
    }

    @Test
    void testEmptyScriptList() throws Exception {
        runWithYaml(
                "stages:\n" +
                        "  - build\n" +
                        "jobs:\n" +
                        "  - name: empty-script-job\n" +
                        "    stage: build\n" +
                        "    script: []\n"
        );

        String output = outContent.toString();
        assertTrue(output.contains("- echo \"Empty script\""));
    }


    @Test
    void testJobWithoutNameIsSkipped() throws Exception {
        runWithYaml(
                "stages:\n" +
                        "  - build\n" +
                        "jobs:\n" +
                        "  - stage: build\n" +
                        "    script: echo no-name\n" +
                        "  - name: job-with-name\n" +
                        "    stage: build\n" +
                        "    script: echo real\n"
        );
        String output = outContent.toString();
        assertTrue(output.contains("job-with-name"));
        assertFalse(output.contains("no-name")); // not printed
    }

    @Test
    void testDifferentImageFieldNames() throws Exception {
        runWithYaml(
                "stages:\n" +
                        "  - build\n" +
                        "jobs:\n" +
                        "  - name: job1\n" +
                        "    stage: build\n" +
                        "    dockerImage: alpine:1\n" +
                        "    script: echo 1\n" +
                        "  - name: job2\n" +
                        "    stage: build\n" +
                        "    image: alpine:2\n" +
                        "    script: echo 2\n" +
                        "  - name: job3\n" +
                        "    stage: build\n" +
                        "    docker-image: alpine:3\n" +
                        "    script: echo 3\n" +
                        "  - name: job4\n" +
                        "    stage: build\n" +
                        "    docker_image: alpine:4\n" +
                        "    script: echo 4\n"
        );

        String output = outContent.toString();
        assertTrue(output.contains("image: alpine:1"));
        assertTrue(output.contains("image: alpine:2"));
        assertTrue(output.contains("image: alpine:3"));
        assertTrue(output.contains("image: alpine:4"));
    }

    @Test
    void testStageAsMapWithEmbeddedJobs() throws Exception {
        runWithYaml(
                "stages:\n" +
                        "  - name: build\n" +
                        "    jobs:\n" +
                        "      - name: job-a\n" +
                        "        script: echo A\n" +
                        "  - name: test\n" +
                        "    jobs:\n" +
                        "      - name: job-b\n" +
                        "        script: echo B\n"
        );

        String output = outContent.toString();

        assertTrue(output.contains("build:"));
        assertTrue(output.contains("job-a"));
        assertTrue(output.contains("- echo A"));

        assertTrue(output.contains("test:"));
        assertTrue(output.contains("job-b"));
        assertTrue(output.contains("- echo B"));
    }

}
