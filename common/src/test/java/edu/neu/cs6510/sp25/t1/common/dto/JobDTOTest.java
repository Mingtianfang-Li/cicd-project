package edu.neu.cs6510.sp25.t1.common.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JobDTOTest {

    @Test
    public void testEmptyConstructor() {
        // Using the no-args constructor
        JobDTO jobDTO = new JobDTO();

        // Verify all fields are initialized to null or default values
        assertNull(jobDTO.getId());
        assertNull(jobDTO.getStageId());
        assertNull(jobDTO.getName());
        assertNull(jobDTO.getDockerImage());
        assertNull(jobDTO.getScript());
        assertNull(jobDTO.getWorkingDir());
        assertNull(jobDTO.getDependencies());
        assertFalse(jobDTO.isAllowFailure());
        assertNull(jobDTO.getArtifacts());
        assertNull(jobDTO.getCreatedAt());
        assertNull(jobDTO.getUpdatedAt());
    }

    @Test
    public void testAllArgsConstructor() {
        // Create test data
        UUID id = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        String name = "test-job";
        String dockerImage = "gradle:8.12-jdk21";
        List<String> script = Arrays.asList("./gradlew test", "./gradlew check");
        String workingDir = "/app/project";
        List<UUID> dependencies = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        boolean allowFailure = true;
        List<String> artifacts = Arrays.asList("build/", "*.log");
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();

        // Use all-args constructor
        JobDTO jobDTO = new JobDTO(
                id, stageId, name, dockerImage, script, workingDir,
                dependencies, allowFailure, artifacts, createdAt, updatedAt
        );

        // Verify all fields are set correctly
        assertEquals(id, jobDTO.getId());
        assertEquals(stageId, jobDTO.getStageId());
        assertEquals(name, jobDTO.getName());
        assertEquals(dockerImage, jobDTO.getDockerImage());
        assertEquals(script, jobDTO.getScript());
        assertEquals(workingDir, jobDTO.getWorkingDir());
        assertEquals(dependencies, jobDTO.getDependencies());
        assertTrue(jobDTO.isAllowFailure());
        assertEquals(artifacts, jobDTO.getArtifacts());
        assertEquals(createdAt, jobDTO.getCreatedAt());
        assertEquals(updatedAt, jobDTO.getUpdatedAt());
    }

    @Test
    public void testBuilder() {
        // Create test data
        UUID id = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        String name = "builder-job";
        String dockerImage = "node:18";
        List<String> script = Arrays.asList("npm install", "npm test");
        String workingDir = "/workspace";
        List<UUID> dependencies = Arrays.asList(UUID.randomUUID());
        boolean allowFailure = false;
        List<String> artifacts = Arrays.asList("dist/");
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();

        // Use builder pattern
        JobDTO jobDTO = JobDTO.builder()
                .id(id)
                .stageId(stageId)
                .name(name)
                .dockerImage(dockerImage)
                .script(script)
                .workingDir(workingDir)
                .dependencies(dependencies)
                .allowFailure(allowFailure)
                .artifacts(artifacts)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // Verify all fields are set correctly
        assertEquals(id, jobDTO.getId());
        assertEquals(stageId, jobDTO.getStageId());
        assertEquals(name, jobDTO.getName());
        assertEquals(dockerImage, jobDTO.getDockerImage());
        assertEquals(script, jobDTO.getScript());
        assertEquals(workingDir, jobDTO.getWorkingDir());
        assertEquals(dependencies, jobDTO.getDependencies());
        assertFalse(jobDTO.isAllowFailure());
        assertEquals(artifacts, jobDTO.getArtifacts());
        assertEquals(createdAt, jobDTO.getCreatedAt());
        assertEquals(updatedAt, jobDTO.getUpdatedAt());
    }

    @Test
    public void testSettersAndGetters() {
        // Create an empty DTO
        JobDTO jobDTO = new JobDTO();

        // Create test data
        UUID id = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        String name = "setter-job";
        String dockerImage = "python:3.9";
        List<String> script = Arrays.asList("pip install -r requirements.txt", "pytest");
        String workingDir = "/app/python";
        List<UUID> dependencies = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        boolean allowFailure = true;
        List<String> artifacts = Arrays.asList("reports/");
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();

        // Use setters
        jobDTO.setId(id);
        jobDTO.setStageId(stageId);
        jobDTO.setName(name);
        jobDTO.setDockerImage(dockerImage);
        jobDTO.setScript(script);
        jobDTO.setWorkingDir(workingDir);
        jobDTO.setDependencies(dependencies);
        jobDTO.setAllowFailure(allowFailure);
        jobDTO.setArtifacts(artifacts);
        jobDTO.setCreatedAt(createdAt);
        jobDTO.setUpdatedAt(updatedAt);

        // Verify getters return correct values
        assertEquals(id, jobDTO.getId());
        assertEquals(stageId, jobDTO.getStageId());
        assertEquals(name, jobDTO.getName());
        assertEquals(dockerImage, jobDTO.getDockerImage());
        assertEquals(script, jobDTO.getScript());
        assertEquals(workingDir, jobDTO.getWorkingDir());
        assertEquals(dependencies, jobDTO.getDependencies());
        assertTrue(jobDTO.isAllowFailure());
        assertEquals(artifacts, jobDTO.getArtifacts());
        assertEquals(createdAt, jobDTO.getCreatedAt());
        assertEquals(updatedAt, jobDTO.getUpdatedAt());
    }
}