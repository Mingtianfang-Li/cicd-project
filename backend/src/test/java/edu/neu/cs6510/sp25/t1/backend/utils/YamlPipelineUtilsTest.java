package edu.neu.cs6510.sp25.t1.backend.utils;

import edu.neu.cs6510.sp25.t1.common.validation.error.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class YamlPipelineUtilsTest {

    @TempDir
    private Path tempDir;

    private Path validPipelineYaml;
    private Path invalidPipelineYaml;
    private Path emptyPipelineYaml;
    private Path cyclicDependencyYaml;
    private Path duplicateStageNameYaml;
    private Path duplicateJobNameYaml;
    private Path invalidImageYaml;
    private Path invalidAllowFailureYaml;
    private Path nonExistentDependencyYaml;
    private Path invalidStageTypeYaml; // Added for the new test case

    // Additional YAML files for dependency testing
    private Path validDependenciesYaml;
    private Path selfDependencyYaml;
    private Path invalidDependencyFormatYaml;
    private Path multipleDependenciesYaml;
    private Path complexDependencyGraphYaml;
    private Path complexCircularDependencyYaml;

    @BeforeEach
    void setUp() throws IOException {
        // Create valid pipeline YAML with nested format
        validPipelineYaml = tempDir.resolve("valid-pipeline.yaml");
        Files.writeString(validPipelineYaml,
                "stages:\n" +
                        "  - name: build\n" +
                        "    jobs:\n" +
                        "      - name: compile\n" +
                        "        image: gradle:jdk17\n" +
                        "        script: ./gradlew build\n" +
                        "  - name: test\n" +
                        "    jobs:\n" +
                        "      - name: unit-tests\n" +
                        "        image: gradle:jdk17\n" +
                        "        script: ./gradlew test\n" +
                        "      - name: integration-tests\n" +
                        "        image: gradle:jdk17\n" +
                        "        script: ./gradlew integrationTest\n" +
                        "        allow_failure: true\n"
        );

        // Create valid pipeline YAML with top-level format
        Path validTopLevelYaml = tempDir.resolve("valid-toplevel.yaml");
        Files.writeString(validTopLevelYaml,
                "stages:\n" +
                        "  - build\n" +
                        "  - test\n" +
                        "jobs:\n" +
                        "  - name: compile\n" +
                        "    stage: build\n" +
                        "    image: gradle:jdk17\n" +
                        "    script: ./gradlew build\n" +
                        "  - name: unit-tests\n" +
                        "    stage: test\n" +
                        "    image: gradle:jdk17\n" +
                        "    script: ./gradlew test\n"
        );

        // Create invalid pipeline YAML with stage of invalid type (number)
        invalidStageTypeYaml = tempDir.resolve("invalid-stage-type.yaml");
        Files.writeString(invalidStageTypeYaml,
                "stages:\n" +
                        "  - build\n" +
                        "  - 123\n" +  // Number instead of string or map
                        "  - test\n" +
                        "jobs:\n" +
                        "  - name: compile\n" +
                        "    stage: build\n" +
                        "    image: gradle:jdk17\n" +
                        "    script: ./gradlew build\n" +
                        "  - name: unit-tests\n" +
                        "    stage: test\n" +
                        "    image: gradle:jdk17\n" +
                        "    script: ./gradlew test\n"
        );

        // Create pipeline YAML matching the example format (with name, dockerImage, allowFailure)
        Path exampleFormatYaml = tempDir.resolve("example-format.yaml");
        Files.writeString(exampleFormatYaml,
                "name: my-cicd-pipeline\n" +
                        "\n" +
                        "stages:\n" +
                        "  - build\n" +
                        "  - test\n" +
                        "  - doc\n" +
                        "  - deploy\n" +
                        "\n" +
                        "jobs:\n" +
                        "  - name: \"compile\"\n" +
                        "    dockerImage: \"openjdk:21-jdk\"\n" +
                        "    script:\n" +
                        "      - \"./gradlew compile\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "\n" +
                        "  - name: \"unit-tests\"\n" +
                        "    dockerImage: \"openjdk:21-jdk\"\n" +
                        "    script:\n" +
                        "      - \"./gradlew test\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"test\"\n" +
                        "    dependencies: [\"compile\"]\n" +
                        "\n" +
                        "  - name: \"generate-docs\"\n" +
                        "    dockerImage: \"openjdk:21-jdk\"\n" +
                        "    script:\n" +
                        "      - \"./gradlew javadoc\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"doc\"\n" +
                        "    dependencies: [\"compile\"]\n" +
                        "\n" +
                        "  - name: \"build-jar\"\n" +
                        "    dockerImage: \"gradle:8.12-jdk21\"\n" +
                        "    script:\n" +
                        "      - \"./gradlew bootJar\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"deploy\"\n" +
                        "    dependencies: [\"compile\", \"unit-tests\"]\n" +
                        "\n" +
                        "  - name: \"dockerize\"\n" +
                        "    dockerImage: \"docker:latest\"\n" +
                        "    script:\n" +
                        "      - \"docker build -t my-cicd-app .\"\n" +
                        "      - \"docker run -d -p 5000:5000 my-cicd-app\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"deploy\"\n" +
                        "    dependencies: [\"build-jar\"]\n"
        );

        // Create invalid pipeline YAML (missing required fields)
        invalidPipelineYaml = tempDir.resolve("invalid-pipeline.yaml");
        Files.writeString(invalidPipelineYaml,
                "name: invalid-pipeline\n" +
                        "  - step1\n" +  // Invalid format, missing stages key
                        "  - step2\n"
        );

        // Create empty pipeline YAML
        emptyPipelineYaml = tempDir.resolve("empty-pipeline.yaml");
        Files.writeString(emptyPipelineYaml, "");

        // Create pipeline YAML with cyclic dependencies
        cyclicDependencyYaml = tempDir.resolve("cyclic-dependency.yaml");
        Files.writeString(cyclicDependencyYaml,
                "name: cyclic-dependency-pipeline\n" +
                        "\n" +
                        "stages:\n" +
                        "  - build\n" +
                        "\n" +
                        "jobs:\n" +
                        "  - name: \"job1\"\n" +
                        "    dockerImage: \"alpine:latest\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job1'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job3\"]\n" +
                        "\n" +
                        "  - name: \"job2\"\n" +
                        "    dockerImage: \"alpine:latest\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job2'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job1\"]\n" +
                        "\n" +
                        "  - name: \"job3\"\n" +
                        "    dockerImage: \"alpine:latest\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job3'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job2\"]\n"
        );

        // Create pipeline YAML with duplicate stage names
        duplicateStageNameYaml = tempDir.resolve("duplicate-stage-name.yaml");
        Files.writeString(duplicateStageNameYaml,
                "stages:\n" +
                        "  - name: build\n" +
                        "    jobs:\n" +
                        "      - name: compile\n" +
                        "        image: gradle:jdk17\n" +
                        "        script: ./gradlew build\n" +
                        "  - name: build\n" +  // Duplicate stage name
                        "    jobs:\n" +
                        "      - name: package\n" +
                        "        image: gradle:jdk17\n" +
                        "        script: ./gradlew package\n"
        );

        // Create pipeline YAML with duplicate job names
        duplicateJobNameYaml = tempDir.resolve("duplicate-job-name.yaml");
        Files.writeString(duplicateJobNameYaml,
                "stages:\n" +
                        "  - name: build\n" +
                        "    jobs:\n" +
                        "      - name: compile\n" +
                        "        image: gradle:jdk17\n" +
                        "        script: ./gradlew build\n" +
                        "      - name: compile\n" +  // Duplicate job name
                        "        image: gradle:jdk17\n" +
                        "        script: ./gradlew package\n"
        );

        // Create pipeline YAML with invalid Docker image
        invalidImageYaml = tempDir.resolve("invalid-image.yaml");
        Files.writeString(invalidImageYaml,
                "stages:\n" +
                        "  - name: build\n" +
                        "    jobs:\n" +
                        "      - name: compile\n" +
                        "        image: \"\"\n" +  // Empty image
                        "        script: ./gradlew build\n"
        );

        // Create pipeline YAML with invalid allow_failure value
        invalidAllowFailureYaml = tempDir.resolve("invalid-allow-failure.yaml");
        Files.writeString(invalidAllowFailureYaml,
                "stages:\n" +
                        "  - name: build\n" +
                        "    jobs:\n" +
                        "      - name: compile\n" +
                        "        image: gradle:jdk17\n" +
                        "        script: ./gradlew build\n" +
                        "        allow_failure: maybe\n"  // Invalid value - should be true/false or boolean
        );

        // Create pipeline YAML with non-existent dependency
        nonExistentDependencyYaml = tempDir.resolve("non-existent-dependency.yaml");
        Files.writeString(nonExistentDependencyYaml,
                "name: non-existent-dependency-pipeline\n" +
                        "\n" +
                        "stages:\n" +
                        "  - build\n" +
                        "\n" +
                        "jobs:\n" +
                        "  - name: \"compile\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"./gradlew build\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"non-existent-job\"]\n"  // Job that doesn't exist
        );

        // Create additional YAML files for dependency testing

        // Valid dependencies YAML
        validDependenciesYaml = tempDir.resolve("valid-dependencies.yaml");
        Files.writeString(validDependenciesYaml,
                "name: valid-dependencies-pipeline\n" +
                        "\n" +
                        "stages:\n" +
                        "  - build\n" +
                        "\n" +
                        "jobs:\n" +
                        "  - name: \"compile\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"./gradlew build\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "\n" +
                        "  - name: \"test\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"./gradlew test\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"compile\"]\n" +
                        "\n" +
                        "  - name: \"package\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"./gradlew package\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"compile\", \"test\"]\n"
        );

        // Self dependency YAML
        selfDependencyYaml = tempDir.resolve("self-dependency.yaml");
        Files.writeString(selfDependencyYaml,
                "name: self-dependency-pipeline\n" +
                        "\n" +
                        "stages:\n" +
                        "  - build\n" +
                        "\n" +
                        "jobs:\n" +
                        "  - name: \"compile\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"./gradlew build\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"compile\"]\n" // Self-dependency
        );

        // Invalid dependency format YAML (number instead of string)
        invalidDependencyFormatYaml = tempDir.resolve("invalid-dependency-format.yaml");
        Files.writeString(invalidDependencyFormatYaml,
                "name: invalid-dependency-format-pipeline\n" +
                        "\n" +
                        "stages:\n" +
                        "  - build\n" +
                        "\n" +
                        "jobs:\n" +
                        "  - name: \"compile\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"./gradlew build\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "\n" +
                        "  - name: \"test\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"./gradlew test\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: 123\n" // Number instead of string or list
        );

        // Multiple dependencies YAML
        multipleDependenciesYaml = tempDir.resolve("multiple-dependencies.yaml");
        Files.writeString(multipleDependenciesYaml,
                "name: multiple-dependencies-pipeline\n" +
                        "\n" +
                        "stages:\n" +
                        "  - build\n" +
                        "\n" +
                        "jobs:\n" +
                        "  - name: \"job1\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job1'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "\n" +
                        "  - name: \"job2\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job2'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "\n" +
                        "  - name: \"job3\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job3'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "\n" +
                        "  - name: \"job4\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job4'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job1\", \"job2\", \"job3\"]\n" // Multiple dependencies
        );

        // Complex dependency graph YAML
        complexDependencyGraphYaml = tempDir.resolve("complex-dependency-graph.yaml");
        Files.writeString(complexDependencyGraphYaml,
                "name: complex-dependency-graph-pipeline\n" +
                        "\n" +
                        "stages:\n" +
                        "  - build\n" +
                        "\n" +
                        "jobs:\n" +
                        "  - name: \"job1\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job1'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "\n" +
                        "  - name: \"job2\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job2'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job1\"]\n" +
                        "\n" +
                        "  - name: \"job3\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job3'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job1\"]\n" +
                        "\n" +
                        "  - name: \"job4\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job4'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job2\", \"job3\"]\n" +
                        "\n" +
                        "  - name: \"job5\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job5'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job4\"]\n" +
                        "\n" +
                        "  - name: \"job6\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job6'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job4\"]\n"
        );

        // Complex circular dependency YAML
        complexCircularDependencyYaml = tempDir.resolve("complex-circular-dependency.yaml");
        Files.writeString(complexCircularDependencyYaml,
                "name: complex-circular-dependency-pipeline\n" +
                        "\n" +
                        "stages:\n" +
                        "  - build\n" +
                        "\n" +
                        "jobs:\n" +
                        "  - name: \"job1\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job1'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "\n" +
                        "  - name: \"job2\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job2'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job1\"]\n" +
                        "\n" +
                        "  - name: \"job3\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job3'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job2\"]\n" +
                        "\n" +
                        "  - name: \"job4\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job4'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job3\"]\n" +
                        "\n" +
                        "  - name: \"job5\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job5'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job4\"]\n" +
                        "\n" +
                        "  - name: \"job6\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job6'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job5\"]\n" +
                        "\n" +
                        "  - name: \"job7\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job7'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job6\"]\n" +
                        "\n" +
                        "  - name: \"job8\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job8'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job7\"]\n" +
                        "\n" +
                        "  - name: \"job9\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job9'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job8\"]\n" +
                        "\n" +
                        "  - name: \"job10\"\n" +
                        "    dockerImage: \"gradle:jdk17\"\n" +
                        "    script:\n" +
                        "      - \"echo 'job10'\"\n" +
                        "    allowFailure: false\n" +
                        "    stage: \"build\"\n" +
                        "    dependencies: [\"job9\", \"job1\"]\n" // Creates a cycle with job1
        );
    }

    @Test
    void testReadPipelineYaml_ValidYaml() throws IOException, ValidationException {
        // Test reading a valid pipeline YAML file
        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(validPipelineYaml.toString());
        assertNotNull(config);
        assertTrue(config.containsKey("stages"));

        List<?> stages = (List<?>) config.get("stages");
        assertEquals(2, stages.size());
    }

    @Test
    void testReadPipelineYaml_FileNotFound() {
        // Test reading a non-existent file
        String nonExistentFile = tempDir.resolve("non-existent.yaml").toString();
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            YamlPipelineUtils.readPipelineYaml(nonExistentFile);
        });
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void testReadPipelineYaml_EmptyFile() {
        // Test reading an empty file
        IOException exception = assertThrows(IOException.class, () -> {
            YamlPipelineUtils.readPipelineYaml(emptyPipelineYaml.toString());
        });
        assertTrue(exception.getMessage().contains("Error parsing"));
    }

    @Test
    void testValidatePipelineConfig_ValidNestedFormat() throws IOException, ValidationException {
        // Test validating a valid pipeline configuration with nested format
        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(validPipelineYaml.toString());
        assertDoesNotThrow(() -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
    }

    @Test
    void testValidatePipelineConfig_ValidTopLevelFormat() throws IOException, ValidationException {
        // Test validating a valid pipeline configuration with top-level format
        Path validTopLevelYaml = tempDir.resolve("valid-toplevel.yaml");
        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(validTopLevelYaml.toString());
        assertDoesNotThrow(() -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
    }

    @Test
    void testValidatePipelineConfig_InvalidStageType() throws IOException, ValidationException {
        // Test validating a pipeline config with a stage of invalid type (number)
        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(invalidStageTypeYaml.toString());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });

        assertTrue(exception.getMessage().contains("Stage at index") &&
                exception.getMessage().contains("must be a string or map"));
    }

    @Test
    void testValidatePipelineConfig_ExampleFormat() throws IOException, ValidationException {
        // Test validating a pipeline configuration matching the example format
        Path exampleFormatYaml = tempDir.resolve("example-format.yaml");
        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(exampleFormatYaml.toString());
        assertDoesNotThrow(() -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
    }

    @Test
    void testValidatePipelineConfig_MissingStages() {
        // Test validating a pipeline config with missing stages
        Map<String, Object> config = Map.of("name", "missing-stages");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
        assertTrue(exception.getMessage().contains("Missing 'stages' field"));
    }

    @Test
    void testValidatePipelineConfig_StagesNotList() {
        // Test validating a pipeline config where stages is not a list
        Map<String, Object> config = Map.of(
                "name", "invalid-stages",
                "stages", "not-a-list"
        );
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
        assertTrue(exception.getMessage().contains("'stages' must be a list"));
    }

    @Test
    void testValidatePipelineConfig_CyclicDependencies() throws IOException, ValidationException {
        // Test validating a pipeline config with cyclic dependencies
        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(cyclicDependencyYaml.toString());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
        assertTrue(exception.getMessage().contains("Circular dependency detected"));
    }

    @Test
    void testValidatePipelineConfig_DuplicateJobNames() throws IOException, ValidationException {
        // Test validating a pipeline config with duplicate job names
        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(duplicateJobNameYaml.toString());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
        assertTrue(exception.getMessage().contains("Duplicate job name"));
    }

    @Test
    void testValidatePipelineConfig_InvalidImage() throws IOException, ValidationException {
        // Test validating a pipeline config with invalid Docker image
        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(invalidImageYaml.toString());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
        assertTrue(exception.getMessage().contains("Docker image") && exception.getMessage().contains("cannot be empty"));
    }

    @Test
    void testValidatePipelineConfig_InvalidAllowFailure() throws IOException, ValidationException {
        // Test validating a pipeline config with invalid allow_failure value
        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(invalidAllowFailureYaml.toString());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
        assertTrue(exception.getMessage().contains("allow_failure") && exception.getMessage().contains("must be 'true' or 'false'"));
    }

    @Test
    void testValidatePipelineConfig_NonExistentDependency() throws IOException, ValidationException {
        // Test validating a pipeline config with non-existent dependency
        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(nonExistentDependencyYaml.toString());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
        assertTrue(exception.getMessage().contains("depends on non-existent job"));
    }

    @Test
    void testCreateTempFileWithContent() throws IOException, ValidationException {
        // Helper method to create a temporary file with content for dynamic tests
        Path tempFile = tempDir.resolve("dynamic-test.yaml");
        String content =
                "stages:\n" +
                        "  - name: dynamic\n" +
                        "    jobs:\n" +
                        "      - name: dynamic-job\n" +
                        "        image: alpine:latest\n" +
                        "        script: echo 'dynamic test'\n";
        Files.writeString(tempFile, content);

        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(tempFile.toString());
        assertDoesNotThrow(() -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
    }

    @Test
    void testValidateScript_SingleString() throws IOException, ValidationException {
        // Create a YAML with script as a single string
        Path scriptStringYaml = tempDir.resolve("script-string.yaml");
        Files.writeString(scriptStringYaml,
                "stages:\n" +
                        "  - name: build\n" +
                        "    jobs:\n" +
                        "      - name: compile\n" +
                        "        image: gradle:jdk17\n" +
                        "        script: ./gradlew build\n"
        );

        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(scriptStringYaml.toString());
        assertDoesNotThrow(() -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
    }

    @Test
    void testValidateScript_ListOfStrings() throws IOException, ValidationException {
        // Create a YAML with script as a list of strings
        Path scriptListYaml = tempDir.resolve("script-list.yaml");
        Files.writeString(scriptListYaml,
                "stages:\n" +
                        "  - name: build\n" +
                        "    jobs:\n" +
                        "      - name: compile\n" +
                        "        image: gradle:jdk17\n" +
                        "        script:\n" +
                        "          - echo 'Starting build'\n" +
                        "          - ./gradlew build\n" +
                        "          - echo 'Build completed'\n"
        );

        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(scriptListYaml.toString());
        assertDoesNotThrow(() -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
    }

    @Test
    void testValidateScript_InvalidType() throws IOException, ValidationException {
        // Create a YAML with script as an invalid type (number)
        Path invalidScriptYaml = tempDir.resolve("invalid-script.yaml");
        Files.writeString(invalidScriptYaml,
                "stages:\n" +
                        "  - name: build\n" +
                        "    jobs:\n" +
                        "      - name: compile\n" +
                        "        image: gradle:jdk17\n" +
                        "        script: 123\n" // Number instead of string
        );

        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(invalidScriptYaml.toString());
        assertThrows(IllegalArgumentException.class, () -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
    }

    // Additional tests for job dependency validation

    @Test
    void testValidDependencies() throws IOException, ValidationException {
        // Test validation with valid dependencies
        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(validDependenciesYaml.toString());
        assertDoesNotThrow(() -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
    }

    @Test
    void testSelfDependency() throws IOException, ValidationException {
        // Test validation with self-dependency
        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(selfDependencyYaml.toString());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
        assertTrue(exception.getMessage().contains("cannot depend on itself"));
    }

    @Test
    void testInvalidDependencyFormat() throws IOException, ValidationException {
        // Test validation with invalid dependency format (number instead of string)
        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(invalidDependencyFormatYaml.toString());
        assertThrows(ClassCastException.class, () -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
    }

    @Test
    void testMultipleDependencies() throws IOException, ValidationException {
        // Test validation with multiple dependencies
        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(multipleDependenciesYaml.toString());
        assertDoesNotThrow(() -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
    }

    @Test
    void testComplexDependencyGraph() throws IOException, ValidationException {
        // Test complex dependency graph with valid dependencies
        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(complexDependencyGraphYaml.toString());
        assertDoesNotThrow(() -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
    }

    @Test
    void testValidateJobs_TopLevelFormat_InvalidJobsNotList() throws IOException, ValidationException {
        // Create a YAML where 'jobs' is not a list
        Path invalidJobsNotListYaml = tempDir.resolve("invalid-jobs-not-list.yaml");
        Files.writeString(invalidJobsNotListYaml,
                "name: my-cicd-pipeline\n" +
                        "stages:\n" +
                        "  - build\n" +
                        "  - test\n" +
                        "jobs: 'not a list but a string'\n"
        );

        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(invalidJobsNotListYaml.toString());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
        assertTrue(exception.getMessage().contains("must be a list"));
    }

    @Test
    void testValidateJobs_TopLevelFormat_InvalidJobNotMap() throws IOException, ValidationException {
        // Create a YAML where a job is not a map but a string
        Path invalidJobNotMapYaml = tempDir.resolve("invalid-job-not-map.yaml");
        Files.writeString(invalidJobNotMapYaml,
                "name: my-cicd-pipeline\n" +
                        "stages:\n" +
                        "  - build\n" +
                        "  - test\n" +
                        "jobs:\n" +
                        "  - 'string instead of a map'\n"
        );

        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(invalidJobNotMapYaml.toString());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
        assertTrue(exception.getMessage().contains("must be a map"));
    }

    @Test
    void testValidateJobs_TopLevelFormat_EmptyJobsList() throws IOException, ValidationException {
        // Create a YAML with empty jobs list
        Path emptyJobsListYaml = tempDir.resolve("empty-jobs-list.yaml");
        Files.writeString(emptyJobsListYaml,
                "name: my-cicd-pipeline\n" +
                        "stages:\n" +
                        "  - build\n" +
                        "  - test\n" +
                        "jobs: []\n"
        );

        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(emptyJobsListYaml.toString());
        assertDoesNotThrow(() -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });
    }

    @Test
    void testValidateTopLevelJob_scriptLegacyKey() throws IOException, ValidationException {
        Path yaml = tempDir.resolve("legacy-script-key.yaml");
        Files.writeString(yaml,
                "stages:\n" +
                        "  - build\n" +
                        "jobs:\n" +
                        "  - name: legacy-job\n" +
                        "    stage: build\n" +
                        "    image: alpine\n" +
                        "    script: echo 'hello from legacy script key'\n"
        );

        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(yaml.toString());
        assertDoesNotThrow(() -> YamlPipelineUtils.validatePipelineConfig(config));
    }

    @Test
    void testValidateDockerImage_warnOnInvalidPattern() throws IOException, ValidationException {
        Path yaml = tempDir.resolve("invalid-docker-image.yaml");
        Files.writeString(yaml,
                "stages:\n" +
                        "  - build\n" +
                        "jobs:\n" +
                        "  - name: compile\n" +
                        "    stage: build\n" +
                        "    image: gradle@jdk17\n" +  // Invalid format, triggers warning
                        "    script: ./gradlew build\n"
        );

        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(yaml.toString());
        assertDoesNotThrow(() -> YamlPipelineUtils.validatePipelineConfig(config));
    }

    @Test
    void testValidateNestedJobWithStageField() throws IOException, ValidationException {
        Path yaml = tempDir.resolve("nested-job-with-stage.yaml");
        Files.writeString(yaml,
                "stages:\n" +
                        "  - name: build\n" +
                        "    jobs:\n" +
                        "      - name: compile\n" +
                        "        image: gradle:jdk17\n" +
                        "        stage: build\n" +  // Unexpected but should be ignored
                        "        script: ./gradlew build\n"
        );

        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(yaml.toString());
        assertDoesNotThrow(() -> YamlPipelineUtils.validatePipelineConfig(config));
    }

    @Test
    void testValidateAllowFailure_MixedCaseString() throws IOException, ValidationException {
        Path yaml = tempDir.resolve("allow-failure-mixed-case.yaml");
        Files.writeString(yaml,
                "stages:\n" +
                        "  - name: build\n" +
                        "    jobs:\n" +
                        "      - name: compile\n" +
                        "        image: gradle:jdk17\n" +
                        "        allow_failure: \"True\"\n" +
                        "        script: ./gradlew build\n"
        );

        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(yaml.toString());
        assertDoesNotThrow(() -> YamlPipelineUtils.validatePipelineConfig(config));
    }

    @Test
    void testValidateDependencies_EmptyArray() throws IOException, ValidationException {
        Path yaml = tempDir.resolve("empty-dependencies.yaml");
        Files.writeString(yaml,
                "stages:\n" +
                        "  - build\n" +
                        "jobs:\n" +
                        "  - name: compile\n" +
                        "    stage: build\n" +
                        "    image: gradle:jdk17\n" +
                        "    dependencies: []\n" +
                        "    script: ./gradlew build\n"
        );

        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(yaml.toString());
        assertDoesNotThrow(() -> YamlPipelineUtils.validatePipelineConfig(config));
    }

    @Test
    void testValidateDependencies_ListWithNonString() throws IOException, ValidationException {
        Path yaml = tempDir.resolve("non-string-dependency.yaml");
        Files.writeString(yaml,
                "stages:\n" +
                        "  - build\n" +
                        "jobs:\n" +
                        "  - name: compile\n" +
                        "    stage: build\n" +
                        "    image: gradle:jdk17\n" +
                        "    dependencies: [123]\n" +  // Invalid
                        "    script: ./gradlew build\n"
        );

        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(yaml.toString());
        assertThrows(ClassCastException.class, () -> YamlPipelineUtils.validatePipelineConfig(config));
    }

    @Test
    void testValidateTopLevelStages_MissingNameFieldInMap() throws IOException, ValidationException {
        Path yaml = tempDir.resolve("missing-name-in-stage-map.yaml");
        Files.writeString(yaml,
                "stages:\n" +
                        "  - build\n" +
                        "  - {}\n" +  // Invalid: empty map
                        "jobs:\n" +
                        "  - name: compile\n" +
                        "    stage: build\n" +
                        "    image: gradle:jdk17\n" +
                        "    script: ./gradlew build\n"
        );

        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(yaml.toString());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });

        assertTrue(exception.getMessage().contains("is missing 'name' field"));
    }

    @Test
    void testValidateTopLevelStages_EmptyStageName() throws IOException, ValidationException {
        Path yaml = tempDir.resolve("empty-stage-name.yaml");
        Files.writeString(yaml,
                "stages:\n" +
                        "  - \"\"\n" +  // Empty name
                        "jobs:\n" +
                        "  - name: compile\n" +
                        "    stage: \"\"\n" +
                        "    image: gradle:jdk17\n" +
                        "    script: ./gradlew build\n"
        );

        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(yaml.toString());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });

        assertTrue(exception.getMessage().contains("has empty name"));
    }

    @Test
    void testValidateTopLevelStages_DuplicateStageNames() throws IOException, ValidationException {
        Path yaml = tempDir.resolve("duplicate-stage-names.yaml");
        Files.writeString(yaml,
                "stages:\n" +
                        "  - build\n" +
                        "  - build\n" +  // Duplicate
                        "jobs:\n" +
                        "  - name: compile\n" +
                        "    stage: build\n" +
                        "    image: gradle:jdk17\n" +
                        "    script: ./gradlew build\n"
        );

        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(yaml.toString());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            YamlPipelineUtils.validatePipelineConfig(config);
        });

        assertTrue(exception.getMessage().contains("Duplicate stage name"));
    }

    @Test
    void testExtractStageNames_MapWithNameField() throws IOException, ValidationException {
        Path yaml = tempDir.resolve("map-style-stages.yaml");
        Files.writeString(yaml,
                "stages:\n" +
                        "  - name: build\n" +
                        "  - name: test\n" +
                        "jobs:\n" +
                        "  - name: compile\n" +
                        "    stage: build\n" +
                        "    image: gradle:jdk17\n" +
                        "    script: ./gradlew build\n" +
                        "  - name: test-job\n" +
                        "    stage: test\n" +
                        "    image: gradle:jdk17\n" +
                        "    script: ./gradlew test\n"
        );

        Map<String, Object> config = YamlPipelineUtils.readPipelineYaml(yaml.toString());

        // The validation logic should extract names from stage maps
        assertDoesNotThrow(() -> YamlPipelineUtils.validatePipelineConfig(config));
    }







}