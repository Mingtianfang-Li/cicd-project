package edu.neu.cs6510.sp25.t1.backend.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up RabbitMQ-related beans.
 * Registers the job queue, RabbitTemplate, and RabbitAdmin to enable messaging and queue management.
 */
@Configuration
public class RabbitMQConfig {

  /** Name of the RabbitMQ job queue, injected from application properties. */
  @Value("${spring.rabbitmq.job-queue}")
  private String jobQueueName;

  /**
   * Creates and configures a {@link RabbitAdmin} instance.
   *
   * @param connectionFactory the RabbitMQ connection factory
   * @return a new RabbitAdmin instance
   */
  @Bean
  public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
  }

  /**
   * Creates and configures a {@link RabbitTemplate} for publishing messages.
   *
   * @param connectionFactory the RabbitMQ connection factory
   * @return a configured RabbitTemplate
   */
  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    return new RabbitTemplate(connectionFactory);
  }

  /**
   * Declares the job queue with durability enabled.
   *
   * @return the configured job queue
   */
  @Bean
  public Queue jobQueue() {
    return new Queue(jobQueueName, true);
  }
}
