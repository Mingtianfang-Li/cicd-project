package edu.neu.cs6510.sp25.t1.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "worker.api")
public class WorkerApiProperties {
  private String url;

}
