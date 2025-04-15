package edu.neu.cs6510.sp25.t1.backend.database.entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "job_dependencies")
public class JobDependencyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "job_id")
  private JobExecutionEntity job;  // The job that has dependencies

  @ManyToOne
  @JoinColumn(name = "depends_on_job_id")
  private JobExecutionEntity dependency;  // The job that must run first
}
