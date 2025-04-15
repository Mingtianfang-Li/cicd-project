package edu.neu.cs6510.sp25.t1.backend.messaging;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Component responsible for dispatching stages of jobs to RabbitMQ.
 * Each stage is represented as a queue of job UUIDs, and stages are dispatched sequentially.
 * This component waits for the RabbitMQ job queue to be empty before dispatching the next stage.
 */
@Component
@RequiredArgsConstructor
public class StageQueuePublisher {

  private final RabbitTemplate rabbitTemplate;
  private final RabbitAdmin rabbitAdmin;

  @Value("${spring.rabbitmq.job-queue}")
  private String jobQueueName;

  private static final long POLL_INTERVAL_MS = 1000; // Time between polling RabbitMQ for emptiness

  /**
   * Starts processing the stageQueue and sending job UUIDs to RabbitMQ.
   * @param stageQueue Queue of job queues (Queue<Queue<UUID>>)
   */
  @Async
  public void dispatchStageQueue(Queue<Queue<UUID>> stageQueue) {
    PipelineLogger.info("Starting StageQueuePublisher...");

    while (!stageQueue.isEmpty()) {
      Queue<UUID> currentJobQueue = stageQueue.poll();

      if (currentJobQueue == null || currentJobQueue.isEmpty()) {
        PipelineLogger.warn("Empty job queue found, skipping...");
        continue;
      }

      // Wait until RabbitMQ job queue is empty
      PipelineLogger.info("Waiting for RabbitMQ job queue to become empty...");
      waitForRabbitQueueEmpty();

      // Send each job UUID to RabbitMQ
      PipelineLogger.info("Dispatching jobs to RabbitMQ...");
      for (UUID jobId : currentJobQueue) {
        sendJobToRabbitMq(jobId);
      }

      PipelineLogger.info("Finished dispatching jobs for one stage.");
    }

    PipelineLogger.info("All stages processed and dispatched.");
  }

  /**
   * Sends a job UUID to the configured RabbitMQ job queue.
   *
   * @param jobId UUID of the job to dispatch
   */
  private void sendJobToRabbitMq(UUID jobId) {
    String message = jobId.toString();
    rabbitTemplate.convertAndSend(jobQueueName, message);
    PipelineLogger.info("Sent job UUID to RabbitMQ: " + message);
  }

  /**
   * Polls RabbitMQ until the job queue becomes empty.
   * Throws an exception if the queue is not found or the thread is interrupted.
   */
  private void waitForRabbitQueueEmpty() {
    while (true) {
      QueueInformation queueInfo = rabbitAdmin.getQueueInfo(jobQueueName);

      if (queueInfo == null) {
        PipelineLogger.error("RabbitMQ queue not found: " + jobQueueName);
        throw new IllegalStateException("RabbitMQ queue not found: " + jobQueueName);
      }

      int messageCount = queueInfo.getMessageCount();

      PipelineLogger.info("RabbitMQ job queue message count: " + messageCount);

      if (messageCount == 0) {
        PipelineLogger.info("RabbitMQ job queue is empty. Proceeding...");
        break;
      }

      try {
        TimeUnit.MILLISECONDS.sleep(POLL_INTERVAL_MS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        PipelineLogger.error("Interrupted while waiting for RabbitMQ queue to become empty.");
        throw new RuntimeException("Interrupted while waiting for RabbitMQ queue to become empty.", e);
      }
    }
  }
}
