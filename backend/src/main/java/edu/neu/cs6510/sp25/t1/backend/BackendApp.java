package edu.neu.cs6510.sp25.t1.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main class to start the Spring Boot application.
 * keep this file in current folder under src/main/java/edu/neu/cs6510/sp25/t1/backend for the application to run
 */
@SpringBootApplication

@ComponentScan("edu.neu.cs6510.sp25.t1")

@EntityScan(basePackages = "edu.neu.cs6510.sp25.t1.backend.database.entity")

@EnableJpaRepositories("edu.neu.cs6510.sp25.t1.backend.database.repository")
/*
 * Main application class for the backend service.
 *
 */
public class BackendApp {
  public static void main(String[] args) {
    SpringApplication.run(BackendApp.class, args);
  }
}
