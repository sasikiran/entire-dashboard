package com.mzfuture.entire.checkpoint.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// Parses checkpoint-level metadata.json and optional session metadata for agent.
@Slf4j
@Component
public class CheckpointMetadataParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /// Parse checkpoint metadata.json into CheckpointParseResult (agent not set; set from last session separately).
    public CheckpointParseResult parseCheckpointMetadata(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String checkpointId = text(root, "checkpoint_id");
            String branch = text(root, "branch");
            Integer checkpointsCount = root.has("checkpoints_count") ? root.get("checkpoints_count").asInt() : null;
            int filesTouched = 0;
            if (root.has("files_touched") && root.get("files_touched").isArray()) {
                filesTouched = root.get("files_touched").size();
            }
            long tokenUsage = 0;
            if (root.has("token_usage") && root.get("token_usage").isObject()) {
                JsonNode tu = root.get("token_usage");
                long input = tu.has("input_tokens") ? tu.get("input_tokens").asLong() : 0;
                long output = tu.has("output_tokens") ? tu.get("output_tokens").asLong() : 0;
                tokenUsage = input + output;
            }
            return CheckpointParseResult.builder()
                    .checkpointId(checkpointId)
                    .branch(branch)
                    .checkpointsCount(checkpointsCount)
                    .filesTouched(filesTouched)
                    .tokenUsage(tokenUsage)
                    .agent(null)
                    .build();
        } catch (Exception e) {
            log.warn("Parse checkpoint metadata failed: {}", e.getMessage());
            return null;
        }
    }

    /// Parse session metadata.json and return agent field (last session).
    public String parseSessionAgent(String sessionMetadataJson) {
        try {
            JsonNode root = objectMapper.readTree(sessionMetadataJson);
            return text(root, "agent");
        } catch (Exception e) {
            log.debug("Parse session agent failed: {}", e.getMessage());
            return null;
        }
    }

    /// Get path to last session metadata from checkpoint metadata.json (sessions array, last element, "metadata" key).
    /// Returns path without leading slash for JGit, e.g. "1a/d322329de8/1/metadata.json"
    public String getLastSessionMetadataPath(String checkpointMetadataJson) {
        try {
            JsonNode root = objectMapper.readTree(checkpointMetadataJson);
            JsonNode sessions = root.get("sessions");
            if (sessions == null || !sessions.isArray() || sessions.isEmpty()) {
                return null;
            }
            String path = text(sessions.get(sessions.size() - 1), "metadata");
            if (path != null && path.startsWith("/")) {
                path = path.substring(1);
            }
            return path;
        } catch (Exception e) {
            log.debug("Get last session path failed: {}", e.getMessage());
            return null;
        }
    }

    /// Get all session metadata paths from checkpoint metadata.json (sessions array, "metadata" key each).
    /// Excludes tasks/; only numeric indices 0, 1, 2... from sessions array are included.
    /// Returns paths without leading slash for JGit, e.g. ["ae/54d67dddc9/0/metadata.json", "ae/54d67dddc9/1/metadata.json"]
    public List<String> getSessionMetadataPaths(String checkpointMetadataJson) {
        try {
            JsonNode root = objectMapper.readTree(checkpointMetadataJson);
            JsonNode sessions = root.get("sessions");
            if (sessions == null || !sessions.isArray() || sessions.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> paths = new ArrayList<>(sessions.size());
            for (int i = 0; i < sessions.size(); i++) {
                String path = text(sessions.get(i), "metadata");
                if (path != null && !path.isEmpty()) {
                    if (path.startsWith("/")) {
                        path = path.substring(1);
                    }
                    paths.add(path);
                }
            }
            return paths;
        } catch (Exception e) {
            log.debug("Get session metadata paths failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /// Get prompt path for a session by index from checkpoint metadata (sessions array, "prompt" key).
    /// Returns path without leading slash, or null if not found.
    public String getSessionPromptPath(String checkpointMetadataJson, int sessionIndex) {
        try {
            JsonNode root = objectMapper.readTree(checkpointMetadataJson);
            JsonNode sessions = root.get("sessions");
            if (sessions == null || !sessions.isArray() || sessionIndex < 0 || sessionIndex >= sessions.size()) {
                return null;
            }
            String path = text(sessions.get(sessionIndex), "prompt");
            if (path != null && path.startsWith("/")) {
                path = path.substring(1);
            }
            return path;
        } catch (Exception e) {
            log.debug("Get session prompt path failed: {}", e.getMessage());
            return null;
        }
    }

    private static String text(JsonNode node, String key) {
        if (node == null || !node.has(key)) {
            return null;
        }
        JsonNode v = node.get(key);
        return v != null && v.isTextual() ? v.asText() : null;
    }
}
