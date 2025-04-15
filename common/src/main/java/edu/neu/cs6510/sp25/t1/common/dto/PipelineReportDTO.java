package edu.neu.cs6510.sp25.t1.common.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;

public class PipelineReportDTO {
  private UUID id;
  private String name;
  private int runNumber;
  private String commitHash;
  private ExecutionStatus status;
  private Instant startTime;
  private Instant completionTime;
  private List<StageReportDTO> stages;

  private String pipelineName;

  public String getPipelineName() {
    return pipelineName;
  }

  public void setPipelineName(String pipelineName) {
    this.pipelineName = pipelineName;
    this.name = pipelineName;
  }
  // Constructors, getters, and setters

  public PipelineReportDTO() {
  }

  public PipelineReportDTO(UUID id, String name, int runNumber, String commitHash,
      ExecutionStatus status, Instant startTime, Instant completionTime,
      List<StageReportDTO> stages) {
    this.id = id;
    this.name = name;
    this.runNumber = runNumber;
    this.commitHash = commitHash;
    this.status = status;
    this.startTime = startTime;
    this.completionTime = completionTime;
    this.stages = stages;
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

  public int getRunNumber() {
    return runNumber;
  }

  public void setRunNumber(int runNumber) {
    this.runNumber = runNumber;
  }

  public String getCommitHash() {
    return commitHash;
  }

  public void setCommitHash(String commitHash) {
    this.commitHash = commitHash;
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

  public List<StageReportDTO> getStages() {
    return stages;
  }

  public void setStages(List<StageReportDTO> stages) {
    this.stages = stages;
  }
}