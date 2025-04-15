package edu.neu.cs6510.sp25.t1.worker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ArtifactServiceTest {

    private ArtifactService artifactService;

    @BeforeEach
    void setUp() {
        artifactService = new ArtifactService();
    }

    @Test
    void testProcessArtifacts_fileCopiedSuccessfully(@TempDir Path tempDir) throws IOException {
        // Create a mock source file
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectories(sourceDir);
        Path artifactFile = sourceDir.resolve("example.txt");
        Files.writeString(artifactFile, "Hello World");

        UUID jobId = UUID.randomUUID();

        // Call the method under test
        artifactService.processArtifacts(jobId, sourceDir.toString(), List.of("example.txt"));

        // Check if the file was copied
        Path expectedDestFile = tempDir.resolve("artifacts").resolve(jobId.toString()).resolve("example.txt");
        Path actualDestFile = Path.of("./artifacts", jobId.toString(), "example.txt");

        assertTrue(Files.exists(actualDestFile));
    }

    @Test
    void testProcessArtifacts_patternNotFound(@TempDir Path tempDir) throws IOException {
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectories(sourceDir);

        UUID jobId = UUID.randomUUID();
        artifactService.processArtifacts(jobId, sourceDir.toString(), List.of("nonexistent.txt"));

        Path expectedDestFile = Path.of("./artifacts", jobId.toString(), "nonexistent.txt");
        assertFalse(Files.exists(expectedDestFile));
    }

    @Test
    void testProcessArtifacts_emptyPatternsDoesNothing(@TempDir Path tempDir) {
        UUID jobId = UUID.randomUUID();
        artifactService.processArtifacts(jobId, tempDir.toString(), List.of());

        Path expectedDir = Path.of("./artifacts", jobId.toString());
        assertFalse(Files.exists(expectedDir));
    }

    @Test
    void testProcessArtifacts_directoryCopiedSuccessfully(@TempDir Path tempDir) throws IOException {
        // Create a mock source directory with nested files
        Path sourceDir = tempDir.resolve("source");
        Path nestedDir = sourceDir.resolve("nested");
        Files.createDirectories(nestedDir);

        Path fileInNestedDir = nestedDir.resolve("file.txt");
        Files.writeString(fileInNestedDir, "Nested content");

        UUID jobId = UUID.randomUUID();

        artifactService.processArtifacts(jobId, sourceDir.toString(), List.of("nested"));

        // Check that the directory and file were copied
        Path copiedFile = Path.of("./artifacts", jobId.toString(), "nested", "file.txt");
        assertTrue(Files.exists(copiedFile));
        assertTrue(Files.readString(copiedFile).contains("Nested content"));
    }

    @Test
    void testProcessArtifacts_ioExceptionLogged(@TempDir Path tempDir) throws IOException {
        // Create source dir with a file that will throw IOException
        Path sourceDir = tempDir.resolve("source");
        Files.createDirectories(sourceDir);
        Path badFile = sourceDir.resolve("badfile.txt");
        Files.writeString(badFile, "This will fail");

        // Make file non-readable to simulate IOException
        badFile.toFile().setReadable(false);

        UUID jobId = UUID.randomUUID();
        artifactService.processArtifacts(jobId, sourceDir.toString(), List.of("badfile.txt"));

        // Check that the file wasn't copied
        Path expectedDestFile = Path.of("./artifacts", jobId.toString(), "badfile.txt");
        assertFalse(Files.exists(expectedDestFile));
    }
}
