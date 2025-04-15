package edu.neu.cs6510.sp25.t1.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class RabbitMQConfigTest {

  private RabbitMQConfig config;

  @BeforeEach
  void setUp() {
    config = new RabbitMQConfig();

    // Manually inject jobQueueName since @Value won't work outside Spring
    try {
      var field = RabbitMQConfig.class.getDeclaredField("jobQueueName");
      field.setAccessible(true);
      field.set(config, "test.queue");
    } catch (Exception e) {
      throw new RuntimeException("Failed to set jobQueueName via reflection", e);
    }
  }

  @Test
  void testJobQueueCreation() {
    Queue queue = config.jobQueue();
    assertNotNull(queue);
    assertEquals("test.queue", queue.getName());
    assertTrue(queue.isDurable());
  }

  @Test
  void testRabbitTemplateCreation() {
    ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
    RabbitTemplate template = config.rabbitTemplate(connectionFactory);
    assertNotNull(template);
    assertSame(connectionFactory, template.getConnectionFactory());
  }

  @Test
  void testRabbitAdminCreation() {
    ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
    RabbitAdmin admin = config.rabbitAdmin(connectionFactory);
    assertNotNull(admin);
  }
}
