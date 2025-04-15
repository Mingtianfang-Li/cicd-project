package edu.neu.cs6510.sp25.t1.common.model.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.common.model.Stage;



class StageDeserializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Fix LocalDateTime issue
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Stage.class, new StageDeserializer());
        objectMapper.registerModule(module);
    }

    @Test
    void testDeserializeSimpleStageName() throws JsonProcessingException {
        String json = "\"Build\"";

        Stage stage = objectMapper.readValue(json, Stage.class);

        assertNotNull(stage);
        assertEquals("Build", stage.getName());
        assertNull(stage.getId());
        assertTrue(stage.getJobs().isEmpty());
    }

    @Test
    void testDeserializeNumberAsStageName() throws JsonProcessingException {
        String json = "123";

        Stage stage = objectMapper.readValue(json, Stage.class);

        assertNotNull(stage);
        assertEquals("123", stage.getName());
        assertNull(stage.getId());
        assertTrue(stage.getJobs().isEmpty());
    }

    @Test
    void testDeserializeFullStageObject() throws JsonProcessingException {
        UUID stageId = UUID.randomUUID();
        UUID pipelineId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        String json = String.format("""
            {
                "id": "%s",
                "name": "Deploy",
                "pipelineId": "%s",
                "executionOrder": 2,
                "jobs": [
                    {
                        "id": "%s",
                        "name": "Deploy Job"
                    }
                ],
                "createdAt": "2024-12-01T10:15:30",
                "updatedAt": "2025-01-01T12:00:00"
            }
            """, stageId, pipelineId, jobId);

        Stage stage = objectMapper.readValue(json, Stage.class);

        assertNotNull(stage);
        assertEquals("Deploy", stage.getName());
        assertEquals(stageId, stage.getId());
        assertEquals(pipelineId, stage.getPipelineId());
        assertEquals(2, stage.getExecutionOrder());
        assertEquals(1, stage.getJobs().size());
        assertEquals("Deploy Job", stage.getJobs().get(0).getName());
        assertEquals(LocalDateTime.of(2024, 12, 1, 10, 15, 30), stage.getCreatedAt());
        assertEquals(LocalDateTime.of(2025, 1, 1, 12, 0), stage.getUpdatedAt());
    }

    @Test
    void testDeserializeUnsupportedNodeTypeThrowsException() {
        String json = "[\"NotAStage\"]";

        Exception exception = assertThrows(IOException.class, () -> {
            objectMapper.readValue(json, Stage.class);
        });

        assertTrue(exception.getMessage().contains("unexpected input format"));
    }

    @Test
    void testDeserializeWithMissingOptionalFields() throws JsonProcessingException {
        String json = """
        {
            "name": "Test Stage"
        }
        """;

        Stage stage = objectMapper.readValue(json, Stage.class);

        assertNotNull(stage);
        assertEquals("Test Stage", stage.getName());
        assertNull(stage.getId());
        assertNull(stage.getPipelineId());
        assertEquals(0, stage.getExecutionOrder()); // default value
        assertTrue(stage.getJobs().isEmpty()); // default value
        assertNull(stage.getCreatedAt());
        assertNull(stage.getUpdatedAt());
    }
}
