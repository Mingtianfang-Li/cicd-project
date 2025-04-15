package edu.neu.cs6510.sp25.t1.backend.service.pipeline;

import edu.neu.cs6510.sp25.t1.backend.info.ClonedPipelineInfo;
import edu.neu.cs6510.sp25.t1.common.api.request.PipelineExecutionRequest;
import edu.neu.cs6510.sp25.t1.common.utils.GitCloneUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.io.File;
import java.io.IOException;
import java.util.UUID;



class GitPipelineServiceTest {

  private GitPipelineService service;

  @BeforeEach
  void setUp() {
    service = new GitPipelineService();
  }

  @Test
  void testCloneRepoAndLocatePipelineFile_success() throws Exception {
    // Arrange: Create request with full constructor
    PipelineExecutionRequest request = new PipelineExecutionRequest(
        UUID.randomUUID(),
        "https://example.com/repo.git",
        "main",         // test with branch
        "abc123",
        false,
        1,
        null
    );

    // Create fake directory for .pipelines and a fake YAML file
    File fakeCloneDir = new File("/tmp/fakeRepo-success");
    File pipelinesDir = new File(fakeCloneDir, ".pipelines");
    pipelinesDir.mkdirs();

    File fakeYaml = new File(pipelinesDir, "pipeline.yaml");
    fakeYaml.createNewFile();

    // Mock GitCloneUtil with ALL parameters using matchers
    try (MockedStatic<GitCloneUtil> mockStatic = org.mockito.Mockito.mockStatic(GitCloneUtil.class)) {
      mockStatic.when(() ->
          GitCloneUtil.cloneRepository(
              eq(request.getRepo()),
              any(File.class),
              eq("main")
          )).thenReturn(fakeCloneDir);

      // Act
      ClonedPipelineInfo result = service.cloneRepoAndLocatePipelineFile(request);

      // Assert
      assertNotNull(result);
      assertTrue(result.getYamlPath().endsWith("pipeline.yaml"));
      assertNotNull(result.getUuid());
    }

    // Cleanup
    fakeYaml.delete();
    pipelinesDir.delete();
    fakeCloneDir.delete();
  }


  @Test
  void testThrowsWhenNoPipelineDir() {
    // Arrange: use valid constructor
    PipelineExecutionRequest request = new PipelineExecutionRequest(
        UUID.randomUUID(),
        "https://example.com/repo.git",
        null,          // no branch
        "abc123",
        false,
        1,
        null
    );

    File fakeCloneDir = new File("/tmp/fakeRepo-no-pipeline");
    fakeCloneDir.mkdirs();

    try (MockedStatic<GitCloneUtil> mockStatic = org.mockito.Mockito.mockStatic(GitCloneUtil.class)) {
      mockStatic.when(() ->
              GitCloneUtil.cloneRepository(eq(request.getRepo()), any(File.class)))
          .thenReturn(fakeCloneDir);

      // Act + Assert
      Exception ex = assertThrows(Exception.class, () ->
          service.cloneRepoAndLocatePipelineFile(request));
      assertTrue(ex.getMessage().contains(".pipelines"));
    }

    // Clean up
    fakeCloneDir.delete();
  }

  @Test
  void testThrowsWhenNoYamlFile() throws Exception {
    // Construct request with required 7-arg constructor
    PipelineExecutionRequest request = new PipelineExecutionRequest(
        UUID.randomUUID(),
        "https://example.com/repo.git",
        null,          // no branch
        "abc123",
        false,
        1,
        null
    );

    // Create fake .pipelines directory without any YAML files
    File fakeCloneDir = new File("/tmp/fakeRepo-empty-yaml");
    File pipelinesDir = new File(fakeCloneDir, ".pipelines");
    pipelinesDir.mkdirs();

    try (MockedStatic<GitCloneUtil> mockStatic = org.mockito.Mockito.mockStatic(GitCloneUtil.class)) {
      mockStatic.when(() ->
              GitCloneUtil.cloneRepository(eq(request.getRepo()), any(File.class)))
          .thenReturn(fakeCloneDir);

      // Act + Assert
      Exception ex = assertThrows(Exception.class, () ->
          service.cloneRepoAndLocatePipelineFile(request));
      assertTrue(ex.getMessage().contains("No YAML"));
    }

    // Cleanup
    pipelinesDir.delete();
    fakeCloneDir.delete();
  }

  @Test
  void testCloneRepoWithoutBranch_usesTwoArgClone() throws Exception {
    PipelineExecutionRequest request = new PipelineExecutionRequest(
        UUID.randomUUID(),
        "https://example.com/repo.git",
        "",              // empty branch
        "commit123",
        false,
        1,
        null
    );

    File fakeCloneDir = new File("/tmp/fakeRepo-nobranch");
    File pipelinesDir = new File(fakeCloneDir, ".pipelines");
    pipelinesDir.mkdirs();

    File fakeYaml = new File(pipelinesDir, "build.yaml");
    fakeYaml.createNewFile();

    try (MockedStatic<GitCloneUtil> mockStatic = Mockito.mockStatic(GitCloneUtil.class)) {
      mockStatic.when(() ->
              GitCloneUtil.cloneRepository(eq(request.getRepo()), any(File.class)))
          .thenReturn(fakeCloneDir);

      ClonedPipelineInfo info = service.cloneRepoAndLocatePipelineFile(request);

      assertTrue(info.getYamlPath().endsWith("build.yaml"));
      assertNotNull(info.getUuid());
    }

    // Cleanup
    fakeYaml.delete();
    pipelinesDir.delete();
    fakeCloneDir.delete();
  }

  @Test
  void testThrowsWhenPipelineDirIsFile() throws Exception {
    PipelineExecutionRequest request = new PipelineExecutionRequest(
        UUID.randomUUID(),
        "https://example.com/repo.git",
        null,
        "abc123",
        false,
        1,
        null
    );

    File fakeCloneDir = new File("/tmp/fakeRepo-pipelines-not-dir");
    fakeCloneDir.mkdirs();

    // Create a file named `.pipelines` instead of a directory
    File pipelinesFile = new File(fakeCloneDir, ".pipelines");
    pipelinesFile.createNewFile();

    try (MockedStatic<GitCloneUtil> mockStatic = Mockito.mockStatic(GitCloneUtil.class)) {
      mockStatic.when(() ->
              GitCloneUtil.cloneRepository(eq(request.getRepo()), any(File.class)))
          .thenReturn(fakeCloneDir);

      Exception ex = assertThrows(IOException.class, () ->
          service.cloneRepoAndLocatePipelineFile(request));
      assertTrue(ex.getMessage().contains(".pipelines"));
    }

    // Cleanup
    pipelinesFile.delete();
    fakeCloneDir.delete();
  }

  @Test
  void testThrowsWhenYamlFilesIsNull() throws Exception {
    // Arrange: construct request with all required fields
    PipelineExecutionRequest request = new PipelineExecutionRequest(
        UUID.randomUUID(),
        "https://example.com/repo.git",
        null,          // no branch
        "abc123",
        false,
        1,
        null
    );

    // Create fake cloned directory and a .pipelines directory (as a file to simulate failure)
    File cloneDir = new File("/tmp/fakeRepo-yaml-null");
    cloneDir.mkdirs();

    // Simulate a non-directory .pipelines entry to trigger the YAML search error
    File pipelinesDir = new File(cloneDir, ".pipelines");
    pipelinesDir.createNewFile(); // NOT a directory, will cause yamlFiles == null

    try (MockedStatic<GitCloneUtil> mockStatic = Mockito.mockStatic(GitCloneUtil.class)) {
      mockStatic.when(() ->
              GitCloneUtil.cloneRepository(eq(request.getRepo()), any(File.class)))
          .thenReturn(cloneDir);

      // Act + Assert with message inspection
      Exception ex = assertThrows(IOException.class, () ->
          service.cloneRepoAndLocatePipelineFile(request));

      // 👀 Print the actual exception message
      System.out.println("❗ Exception message: " + ex.getMessage());

      // Adjust this assertion once you see actual output
      assertTrue(
          ex.getMessage().toLowerCase().contains("yaml"),
          "Expected message to mention missing YAML file"
      );
    }

    // Cleanup
    pipelinesDir.delete();
    cloneDir.delete();
  }

  @Test
  void testYamlFilesEmptyButNotNull() throws Exception {
    PipelineExecutionRequest request = new PipelineExecutionRequest(
        UUID.randomUUID(),
        "https://example.com/repo.git",
        null,
        "abc123",
        false,
        1,
        null
    );

    File cloneDir = new File("/tmp/fakeRepo-empty-nonyaml");
    File pipelinesDir = new File(cloneDir, ".pipelines");
    pipelinesDir.mkdirs();

    // Create a file that does NOT match *.yaml or *.yml
    File nonYaml = new File(pipelinesDir, "not-a-pipeline.txt");
    nonYaml.createNewFile(); // will be filtered out

    try (MockedStatic<GitCloneUtil> mockStatic = Mockito.mockStatic(GitCloneUtil.class)) {
      mockStatic.when(() ->
              GitCloneUtil.cloneRepository(eq(request.getRepo()), any(File.class)))
          .thenReturn(cloneDir);

      Exception ex = assertThrows(IOException.class, () ->
          service.cloneRepoAndLocatePipelineFile(request));

      assertTrue(
          ex.getMessage().contains("No YAML file found in"),
          "Expected message to mention missing YAML file"
      );
    }

    // Cleanup
    nonYaml.delete();
    pipelinesDir.delete();
    cloneDir.delete();
  }


}
