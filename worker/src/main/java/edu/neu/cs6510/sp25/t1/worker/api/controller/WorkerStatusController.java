package edu.neu.cs6510.sp25.t1.worker.api.controller;

import edu.neu.cs6510.sp25.t1.common.dto.JobExecutionDTO;
import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;
import edu.neu.cs6510.sp25.t1.worker.service.JobDataService;
import edu.neu.cs6510.sp25.t1.worker.service.WorkerJobQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for worker status, monitoring, and control endpoints.
 * Provides functionality to check worker status, view active jobs,
 * and control job execution.
 */
@RestController
@RequestMapping("/api/worker")
@RequiredArgsConstructor
@Slf4j
public class WorkerStatusController {
    private final WorkerJobQueue jobQueue;
    private final JobDataService jobDataService;

    /**
     * Gets the current status of the worker including information about
     * active jobs and system health.
     *
     * @return Response with worker status information
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getWorkerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "running");
        status.put("active_jobs", jobQueue.getActiveJobCount());
        return ResponseEntity.ok(status);
    }

    /**
     * Gets detailed information about all jobs currently being processed.
     * Fetches job details from the database based on the active job IDs.
     *
     * @return Response with active job details
     */
    @GetMapping("/jobs/active")
    public ResponseEntity<Map<String, Object>> getActiveJobs() {
        Map<String, Object> response = new HashMap<>();
        List<UUID> activeJobIds = jobQueue.getActiveJobIds();

        // Fetch job details from database for all active job IDs
        List<JobExecutionDTO> activeJobs = activeJobIds.stream()
                .map(id -> jobDataService.getJobExecutionById(id).orElse(null))
                .filter(job -> job != null)
                .collect(Collectors.toList());

        response.put("active_job_count", activeJobIds.size());
        response.put("jobs", activeJobs);

        return ResponseEntity.ok(response);
    }

    /**
     * Cancels a running job if possible.
     *
     * @param jobExecutionId The job execution ID to cancel
     * @return Response indicating whether the cancellation was successful
     */
    @PostMapping("/jobs/{jobExecutionId}/cancel")
    public ResponseEntity<Map<String, String>> cancelJob(@PathVariable UUID jobExecutionId) {
        Map<String, String> response = new HashMap<>();

        boolean cancelled = jobQueue.cancelJob(jobExecutionId);
        if (cancelled) {
            // Update job status in database
            jobDataService.updateJobStatus(jobExecutionId, ExecutionStatus.CANCELED,
                    "Job cancelled by user request");

            response.put("status", "cancelled");
            response.put("message", "Job cancelled successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "failed");
            response.put("message", "Could not cancel job. It might be completed already or not found.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Gets worker configuration and capability information.
     *
     * @return Response with worker configuration details
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getWorkerInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("max_concurrent_jobs", 5);
        info.put("executor_type", "docker");
        info.put("worker_id", UUID.randomUUID());

        return ResponseEntity.ok(info);
    }
}