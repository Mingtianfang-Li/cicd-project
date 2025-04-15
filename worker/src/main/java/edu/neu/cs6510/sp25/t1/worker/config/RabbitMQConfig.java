package edu.neu.cs6510.sp25.t1.worker.config;


import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${cicd.rabbitmq.job-queue}")
    private String jobQueueName;

    @Bean
    public Queue jobQueue() {
        return new Queue(jobQueueName, true); // durable=true
    }
}