package edu.neu.cs6510.sp25.t1.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import edu.neu.cs6510.sp25.t1.worker.config.ArtifactProperties;

import edu.neu.cs6510.sp25.t1.worker.config.WorkerApiProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main entry point for the CI/CD Worker application.
 * Responsible for executing jobs and updating their status in the database.
 */
@SpringBootApplication
@EnableConfigurationProperties({ArtifactProperties.class, WorkerApiProperties.class})
@ComponentScan(basePackages = {
        "edu.neu.cs6510.sp25.t1.worker",
        "edu.neu.cs6510.sp25.t1.backend.database.repository",
        "edu.neu.cs6510.sp25.t1.backend.mapper"
})
@EntityScan(basePackages = "edu.neu.cs6510.sp25.t1.backend.database.entity")

@EnableJpaRepositories(basePackages = "edu.neu.cs6510.sp25.t1.backend.database.repository")
public class WorkerApp {
  public static void main(String[] args) {
    SpringApplication.run(WorkerApp.class, args);
  }
}
