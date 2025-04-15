package edu.neu.cs6510.sp25.t1.worker.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitMQConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestRabbitMQConfig.class)
            .withPropertyValues("cicd.rabbitmq.job-queue=test-queue");

    @Test
    void testQueueBeanIsCreatedWithCorrectNameAndDurability() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(Queue.class);
            Queue queue = context.getBean(Queue.class);
            assertThat(queue.getName()).isEqualTo("test-queue");
            assertThat(queue.isDurable()).isTrue();
        });
    }

    @Configuration
    static class TestRabbitMQConfig extends RabbitMQConfig {
        // Inherits @Bean method and @Value field from RabbitMQConfig
    }
}
