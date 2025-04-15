package edu.neu.cs6510.sp25.t1.common.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.common.enums.ExecutionStatus;

public class JobReportDTO {
  private String name;
  private List<ExecutionRecord> executions;
  private String pipelineName;
  private int runNumber;
  private String commitHash;
  private String stageName;

  public JobReportDTO(String name, List<ExecutionRecord> executions) {
    this.name = name;
    this.executions = executions;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ExecutionRecord> getExecutions() {
    return executions;
  }

  public void setExecutions(List<ExecutionRecord> executions) {
    this.executions = executions;
  }

  public String getPipelineName() {
    return pipelineName;
  }

  public void setPipelineName(String pipelineName) {
    this.pipelineName = pipelineName;
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

  public String getStageName() {
    return stageName;
  }

  public void setStageName(String stageName) {
    this.stageName = stageName;
  }

  public static class ExecutionRecord {
    private UUID id;
    private ExecutionStatus status;
    private Instant startTime;
    private Instant completionTime;
    private boolean allowFailure;

    public ExecutionRecord() {
    }

    public ExecutionRecord(UUID id, ExecutionStatus status, Instant startTime,
        Instant completionTime, boolean allowFailure) {
      this.id = id;
      this.status = status;
      this.startTime = startTime;
      this.completionTime = completionTime;
      this.allowFailure = allowFailure;
    }

    public UUID getId() {
      return id;
    }

    public void setId(UUID id) {
      this.id = id;
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

    public boolean isAllowFailure() {
      return allowFailure;
    }

    public void setAllowFailure(boolean allowFailure) {
      this.allowFailure = allowFailure;
    }
  }
}