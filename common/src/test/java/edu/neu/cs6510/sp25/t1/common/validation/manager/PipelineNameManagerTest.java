package edu.neu.cs6510.sp25.t1.common.validation.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.nio.file.Path;
import java.lang.reflect.Field;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;
import edu.neu.cs6510.sp25.t1.common.validation.error.ValidationException;

class PipelineNameManagerTest {

    @TempDir
    private Path tempDir;

    private Path pipelinesDir;
    private String originalUserDir;

    @BeforeEach
    void setUp() {
        // Save original user.dir
        originalUserDir = System.getProperty("user.dir");
        // Set user.dir to our temp directory
        System.setProperty("user.dir", tempDir.toString());

        // Create .pipelines directory for testing
        pipelinesDir = tempDir.resolve(".pipelines");
        pipelinesDir.toFile().mkdir();
    }

    @AfterEach
    void tearDown() {
        // Restore original user.dir
        System.setProperty("user.dir", originalUserDir);
    }

    @Test
    void constructorShouldLoadExistingYamlFiles() throws Exception {
        // Create some sample YAML files in the .pipelines directory
        File pipeline1 = pipelinesDir.resolve("pipeline1.yaml").toFile();
        File pipeline2 = pipelinesDir.resolve("pipeline2.yml").toFile();
        pipeline1.createNewFile();
        pipeline2.createNewFile();

        // Create non-YAML file to ensure it's not picked up
        File textFile = pipelinesDir.resolve("not-a-pipeline.txt").toFile();
        textFile.createNewFile();

        // Create the manager
        PipelineNameManager manager = new PipelineNameManager();

        // Access private existingFileNames field using reflection
        Field existingFileNamesField = PipelineNameManager.class.getDeclaredField("existingFileNames");
        existingFileNamesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> existingFileNames = (Set<String>) existingFileNamesField.get(manager);

        // Verify the correct files were loaded
        assertEquals(2, existingFileNames.size());
        assertTrue(existingFileNames.contains("pipeline1.yaml"));
        assertTrue(existingFileNames.contains("pipeline2.yml"));
        assertFalse(existingFileNames.contains("not-a-pipeline.txt"));
    }

    @Test
    void constructorShouldThrowExceptionWhenDirectoryDoesNotExist() {
        // Delete the .pipelines directory to simulate missing directory
        pipelinesDir.toFile().delete();

        // Create a mocked PipelineLogger to verify logging
        try (MockedStatic<PipelineLogger> mockedLogger = mockStatic(PipelineLogger.class)) {
            // Verify exception is thrown
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> new PipelineNameManager());

            // Verify exception message contains directory path
            assertTrue(exception.getMessage().contains(pipelinesDir.toString()));
        }
    }

    @Test
    void isPipelineNameUniqueShouldReturnTrueForUniqueNames() throws Exception {
        // Create a sample YAML file
        File existingPipeline = pipelinesDir.resolve("existing-pipeline.yaml").toFile();
        existingPipeline.createNewFile();

        PipelineNameManager manager = new PipelineNameManager();

        // Test with a unique name
        assertTrue(manager.isPipelineNameUnique("new-pipeline.yaml"));
    }

    @Test
    void isPipelineNameUniqueShouldReturnFalseForDuplicateNames() throws Exception {
        // Create a sample YAML file
        File existingPipeline = pipelinesDir.resolve("existing-pipeline.yaml").toFile();
        existingPipeline.createNewFile();

        PipelineNameManager manager = new PipelineNameManager();

        // Test with duplicate name (case insensitive)
        assertFalse(manager.isPipelineNameUnique("existing-pipeline.yaml"));
        assertFalse(manager.isPipelineNameUnique("EXISTING-pipeline.yaml"));
    }

    @Test
    void loadingEmptyDirectoryShouldSucceed() throws Exception {
        // Directory exists but has no files
        PipelineNameManager manager = new PipelineNameManager();

        // Should initialize successfully with empty set
        Field existingFileNamesField = PipelineNameManager.class.getDeclaredField("existingFileNames");
        existingFileNamesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> existingFileNames = (Set<String>) existingFileNamesField.get(manager);

        assertTrue(existingFileNames.isEmpty());
    }
}