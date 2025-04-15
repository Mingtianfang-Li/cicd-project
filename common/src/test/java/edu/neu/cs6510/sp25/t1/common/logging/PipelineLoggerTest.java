package edu.neu.cs6510.sp25.t1.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ch.qos.logback.classic.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

public class PipelineLoggerTest {

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    public void setup() {
        logger = (Logger) org.slf4j.LoggerFactory.getLogger("cicd-logger");

        listAppender = new ListAppender<>();
        listAppender.start();

        logger.detachAndStopAllAppenders();
        logger.addAppender(listAppender);

        logger.setLevel(Level.DEBUG);
    }

    @Test
    public void testInfoLogging() {
        PipelineLogger.info("Test info message");
        List<ILoggingEvent> logsList = listAppender.list;

        assertFalse(logsList.isEmpty());
        assertEquals(Level.INFO, logsList.get(0).getLevel());
        assertEquals("Test info message", logsList.get(0).getFormattedMessage());
    }

    @Test
    public void testErrorLogging() {
        PipelineLogger.error("Test error message");
        List<ILoggingEvent> logsList = listAppender.list;

        assertFalse(logsList.isEmpty());
        assertEquals(Level.ERROR, logsList.get(0).getLevel());
        assertEquals("Test error message", logsList.get(0).getFormattedMessage());
    }

    @Test
    public void testSetVerboseChangesLogLevel() {
        PipelineLogger.setVerbose(false);
        assertEquals(Level.INFO, logger.getLevel());

        PipelineLogger.setVerbose(true);
        assertEquals(Level.DEBUG, logger.getLevel());
    }

}
