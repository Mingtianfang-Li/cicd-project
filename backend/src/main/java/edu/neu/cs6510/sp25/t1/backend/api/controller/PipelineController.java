package edu.neu.cs6510.sp25.t1.backend.api.controller;

import edu.neu.cs6510.sp25.t1.backend.messaging.StageQueuePublisher;
import edu.neu.cs6510.sp25.t1.backend.service.status.StatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Lazy;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.backend.error.ApiError;
import edu.neu.cs6510.sp25.t1.backend.service.execution.PipelineExecutionService;
import edu.neu.cs6510.sp25.t1.common.api.request.PipelineExecutionRequest;
import edu.neu.cs6510.sp25.t1.common.api.response.PipelineExecutionResponse;
import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller class for handling pipeline execution related endpoints.
 * This controller has been updated to work with the queue-based execution
 * system.
 */
@RestController
@RequestMapping("/api/pipeline")
@Tag(name = "Pipeline API", description = "Endpoints for managing pipeline executions")
public class PipelineController {

  private final PipelineExecutionService pipelineExecutionService;
  @Lazy
  private final StageQueuePublisher stageQueuePublisher;
  private final StatusService statusService;

  /**
   * Constructor for PipelineController.
   *
   * @param pipelineExecutionService the service responsible for executing
   *                                 pipelines
   * @param stageQueuePublisher      the publisher responsible for managing stage
   *                                 queues
   * @param statusService            the service responsible for handling status
   *                                 updates
   */
  public PipelineController(
      PipelineExecutionService pipelineExecutionService,
      StageQueuePublisher stageQueuePublisher,
      StatusService statusService) {
    this.pipelineExecutionService = pipelineExecutionService;
    this.stageQueuePublisher = stageQueuePublisher;
    this.statusService = statusService;
  }

  /**
   * Trigger a pipeline execution.
   *
   * @param request PipelineExecutionRequest object
   * @return ResponseEntity object
   */
  @PostMapping("/run")
  @Operation(summary = "Trigger pipeline execution", description = "Starts a new pipeline execution.")
  public ResponseEntity<?> runPipeline(@RequestBody PipelineExecutionRequest request) {
    PipelineLogger.info("Received pipeline execution request for: " + request.getFilePath());

    try {
      // Validate filePath exists
      if (request.getFilePath() == null || request.getFilePath().isEmpty()) {
        PipelineLogger.error("Pipeline file path is missing in the request");
        return ResponseEntity.badRequest().body(
            new ApiError(HttpStatus.BAD_REQUEST, "Invalid Request", "Pipeline file path is required"));
      }

      Queue<Queue<UUID>> stageQueue = new LinkedList<Queue<UUID>>();
      PipelineExecutionResponse response = pipelineExecutionService.startPipelineExecution(request, stageQueue);
      stageQueuePublisher.dispatchStageQueue(stageQueue);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      PipelineLogger.error("Failed pipeline execution: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Pipeline Execution Failed", e.getMessage()));
    }
  }

  // /**
  // * Debug endpoint to check the database status for a specific pipeline
  // * execution.
  // * This is useful for debugging persistence issues.
  // *
  // * @param executionId The pipeline execution ID to check
  // * @return ResponseEntity with detailed database status
  // */
  // @GetMapping("/debug/{executionId}")
  // @Operation(summary = "Debug pipeline database state", description = "Checks
  // if all entities for a pipeline execution exist in the database.")
  // public ResponseEntity<?> debugDatabaseState(@PathVariable UUID executionId) {
  // try {
  // // Get necessary repositories
  // var pipelineExecRepo =
  // pipelineExecutionService.getClass().getDeclaredField("pipelineExecutionRepository")
  // .get(pipelineExecutionService);
  // var stageExecRepo =
  // pipelineExecutionService.getClass().getDeclaredField("stageExecutionRepository")
  // .get(pipelineExecutionService);
  // var jobExecRepo =
  // pipelineExecutionService.getClass().getDeclaredField("jobExecutionRepository")
  // .get(pipelineExecutionService);
  //
  // Map<String, Object> response = new HashMap<>();
  //
  // // Check pipeline execution
  // boolean pipelineExists = ((boolean)
  // pipelineExecRepo.getClass().getMethod("existsById", Object.class)
  // .invoke(pipelineExecRepo, executionId));
  // response.put("pipelineExecutionExists", pipelineExists);
  //
  // if (pipelineExists) {
  // // Get and check stage executions
  // List<?> stages = (List<?>)
  // stageExecRepo.getClass().getMethod("findByPipelineExecutionId", UUID.class)
  // .invoke(stageExecRepo, executionId);
  // response.put("stageExecutionCount", stages.size());
  //
  // // Check job executions for each stage
  // int totalJobs = 0;
  // for (Object stage : stages) {
  // List<?> jobs = (List<?>)
  // jobExecRepo.getClass().getMethod("findByStageExecution", stage.getClass())
  // .invoke(jobExecRepo, stage);
  // totalJobs += jobs.size();
  // }
  // response.put("jobExecutionCount", totalJobs);
  //
  // // Overall status
  // response.put("status", (stages.size() > 0 && totalJobs > 0) ? "COMPLETE" :
  // "INCOMPLETE");
  // } else {
  // response.put("status", "NOT_FOUND");
  // }
  //
  // return ResponseEntity.ok(response);
  // } catch (Exception e) {
  // PipelineLogger.error("Error checking database state: " + e.getMessage());
  // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
  // .body(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking database
  // state", e.getMessage()));
  // }
  // }

  /**
   * Gets the pipeline status using the pipeline YAML file name.
   *
   * @param pipelineFile The name of the pipeline YAML file (e.g.,
   *                     my-cicd-pipeline.yaml)
   * @return ResponseEntity with status info
   */
  @GetMapping("/{pipelineFile}")
  @Operation(summary = "Get pipeline status by YAML filename", description = "Extracts pipeline name from file and retrieves current status.")
  public ResponseEntity<?> getPipelineStatusByFile(@PathVariable String pipelineFile) {
    try {
      if (pipelineFile == null) {
        return ResponseEntity.badRequest().body("Invalid pipeline ");
      }
      String pipelineName;
      if (pipelineFile.endsWith(".yaml")) {
        pipelineName = pipelineFile.replaceFirst("\\.yaml$", "");
      }

      pipelineName = pipelineFile;
      PipelineLogger.info("Fetching status for pipeline: " + pipelineName);

      // Trigger status service (implementation to be added)
      Map<String, Object> status = statusService.getStatusForPipeline(pipelineName);

      return ResponseEntity.ok(status);
    } catch (Exception e) {
      PipelineLogger.error("Error getting status for pipeline: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Pipeline status fetch failed", e.getMessage()));
    }
  }
}
