package edu.neu.cs6510.sp25.t1.common.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;

public class StageReportDTO {
  private UUID id;
  private String name;
  private ExecutionStatus status;
  private Instant startTime;
  private Instant completionTime;
  private List<JobReportDTO> jobs;

  // Constructors, getters, and setters

  public StageReportDTO() {
  }

  public StageReportDTO(UUID id, String name, ExecutionStatus status,
      Instant startTime, Instant completionTime, List<JobReportDTO> jobs) {
    this.id = id;
    this.name = name;
    this.status = status;
    this.startTime = startTime;
    this.completionTime = completionTime;
    this.jobs = jobs;
  }

  // Getters and setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ExecutionStatus getStatus() {
    return status;
  }

  public void setStatus(ExecutionStatus status) {
    this.status = status;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public Instant getCompletionTime() {
    return completionTime;
  }

  public void setCompletionTime(Instant completionTime) {
    this.completionTime = completionTime;
  }

  public List<JobReportDTO> getJobs() {
    return jobs;
  }

  public void setJobs(List<JobReportDTO> jobs) {
    this.jobs = jobs;
  }
}