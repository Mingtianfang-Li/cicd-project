package edu.neu.cs6510.sp25.t1.common.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data transfer object for Job entity.
 * Using lombok for getter, setter, constructor, and builder.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDTO {
  // Getter & Setter with lombok
  // Constructor with lombok
  // Builder with lombok
  private UUID id;
  private UUID stageId;
  private String name;
  private String dockerImage;
  private List<String> script;
  // adding workingDir to jobDto
  private String workingDir;
  private List<UUID> dependencies;
  private boolean allowFailure;
  private List<String> artifacts;
  private Instant createdAt;
  private Instant updatedAt;
}
