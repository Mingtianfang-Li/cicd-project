package edu.neu.cs6510.sp25.t1.backend.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;



class StageQueuePublisherTest {

  private RabbitTemplate rabbitTemplate;
  private RabbitAdmin rabbitAdmin;
  private StageQueuePublisher publisher;

  @BeforeEach
  void setUp() throws Exception {
    rabbitTemplate = mock(RabbitTemplate.class);
    rabbitAdmin = mock(RabbitAdmin.class);

    publisher = new StageQueuePublisher(rabbitTemplate, rabbitAdmin);

    // Use reflection to inject private field
    Field field = StageQueuePublisher.class.getDeclaredField("jobQueueName");
    field.setAccessible(true);
    field.set(publisher, "job.queue.test");
  }

  @Test
  void testDispatchStageQueue_sendsJobsAndWaitsUntilQueueEmpty() {
    // Arrange
    UUID job1 = UUID.randomUUID();
    UUID job2 = UUID.randomUUID();

    Queue<UUID> jobQueue = new LinkedList<>();
    jobQueue.add(job1);
    jobQueue.add(job2);

    Queue<Queue<UUID>> stageQueue = new LinkedList<>();
    stageQueue.add(jobQueue);

    QueueInformation queueInfo = mock(QueueInformation.class);
    when(queueInfo.getMessageCount()).thenReturn(1, 1, 0); // simulate queue draining
    when(rabbitAdmin.getQueueInfo("job.queue.test")).thenReturn(queueInfo);

    // Act
    publisher.dispatchStageQueue(stageQueue);

    // Assert
    verify(rabbitTemplate).convertAndSend("job.queue.test", job1.toString());
    verify(rabbitTemplate).convertAndSend("job.queue.test", job2.toString());
    verify(rabbitAdmin, atLeastOnce()).getQueueInfo("job.queue.test");
  }

  @Test
  void testDispatchStageQueue_skipsEmptyJobQueue() {
    Queue<Queue<UUID>> stageQueue = new LinkedList<>();
    stageQueue.add(new LinkedList<>()); // empty stage

    publisher.dispatchStageQueue(stageQueue);

    verifyNoInteractions(rabbitTemplate);
  }

  @Test
  void testDispatchStageQueue_throwsIfQueueNotFound() {
    Queue<UUID> jobQueue = new LinkedList<>();
    jobQueue.add(UUID.randomUUID());

    Queue<Queue<UUID>> stageQueue = new LinkedList<>();
    stageQueue.add(jobQueue);

    when(rabbitAdmin.getQueueInfo("job.queue.test")).thenReturn(null);

    try {
      publisher.dispatchStageQueue(stageQueue);
      fail("Expected IllegalStateException to be thrown");
    } catch (IllegalStateException e) {
      assertTrue(e.getMessage().contains("RabbitMQ queue not found"));
    }
  }

  @Test
  void testDispatchStageQueue_triggersInterruption() throws Exception {
    UUID jobId = UUID.randomUUID();

    Queue<UUID> jobQueue = new LinkedList<>();
    jobQueue.add(jobId);

    Queue<Queue<UUID>> stageQueue = new LinkedList<>();
    stageQueue.add(jobQueue);

    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    RabbitAdmin rabbitAdmin = mock(RabbitAdmin.class);

    QueueInformation queueInfo = mock(QueueInformation.class);
    when(queueInfo.getMessageCount()).thenReturn(1); // force sleep
    when(rabbitAdmin.getQueueInfo("job.queue.test")).thenReturn(queueInfo);

    StageQueuePublisher publisher = new StageQueuePublisher(rabbitTemplate, rabbitAdmin);
    setPrivateField(publisher, "jobQueueName", "job.queue.test");

    // Simulate thread interruption
    Thread.currentThread().interrupt();

    RuntimeException ex = assertThrows(RuntimeException.class, () ->
        publisher.dispatchStageQueue(stageQueue)
    );

    assertTrue(ex.getMessage().contains("Interrupted"));
  }

  private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }



}
