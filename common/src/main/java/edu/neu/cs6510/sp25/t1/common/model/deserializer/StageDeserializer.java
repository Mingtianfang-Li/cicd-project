package edu.neu.cs6510.sp25.t1.common.model.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.neu.cs6510.sp25.t1.common.model.Job;
import edu.neu.cs6510.sp25.t1.common.model.Stage;
import edu.neu.cs6510.sp25.t1.common.logging.PipelineLogger;

/**
 * Custom deserializer for Stage objects.
 * <p>
 * This deserializer handles two cases:
 * 1. When a stage is provided as a simple string (just a name)
 * 2. When a stage is provided as a complete object with all attributes
 */
public class StageDeserializer extends JsonDeserializer<Stage> {

    @Override
    public Stage deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            JsonNode node = mapper.readTree(p);
            
            PipelineLogger.info("Deserializing Stage node: " + node.toString());
            
            // Case 1: If the node is a text node, treat it as a stage name
            if (node instanceof TextNode || (node.isValueNode() && !node.isObject())) {
                String stageName = node.asText();
                PipelineLogger.info("Creating stage from string value: " + stageName);
                return new Stage(stageName);
            }
            
            // Case 2: Otherwise, deserialize as a full stage object
            if (node instanceof ObjectNode objNode) {
                UUID id = null;
                if (objNode.has("id") && !objNode.get("id").isNull()) {
                    id = UUID.fromString(objNode.get("id").asText());
                }
                
                String name = null;
                if (objNode.has("name")) {
                    name = objNode.get("name").asText();
                    PipelineLogger.info("Found stage name: " + name);
                }
                
                UUID pipelineId = null;
                if (objNode.has("pipelineId") && !objNode.get("pipelineId").isNull()) {
                    pipelineId = UUID.fromString(objNode.get("pipelineId").asText());
                }
                
                int executionOrder = 0;
                if (objNode.has("executionOrder")) {
                    executionOrder = objNode.get("executionOrder").asInt();
                }
                
                List<Job> jobs = new ArrayList<>();
                if (objNode.has("jobs") && objNode.get("jobs").isArray()) {
                    ArrayNode jobNodes = (ArrayNode) objNode.get("jobs");
                    for (JsonNode jobNode : jobNodes) {
                        Job job = mapper.treeToValue(jobNode, Job.class);
                        jobs.add(job);
                    }
                    PipelineLogger.info("Found " + jobs.size() + " jobs for stage: " + name);
                }
                
                LocalDateTime createdAt = null;
                if (objNode.has("createdAt") && !objNode.get("createdAt").isNull()) {
                    createdAt = mapper.treeToValue(objNode.get("createdAt"), LocalDateTime.class);
                }
                
                LocalDateTime updatedAt = null;
                if (objNode.has("updatedAt") && !objNode.get("updatedAt").isNull()) {
                    updatedAt = mapper.treeToValue(objNode.get("updatedAt"), LocalDateTime.class);
                }
                
                return new Stage(id, name, pipelineId, executionOrder, jobs, createdAt, updatedAt);
            }
            
            PipelineLogger.error("Cannot deserialize Stage, unexpected node type: " + node.getNodeType());
            throw new IOException("Cannot deserialize Stage: unexpected input format");
        } catch (Exception e) {
            PipelineLogger.error("Error deserializing Stage: " + e.getMessage());
            throw e;
        }
    }
}