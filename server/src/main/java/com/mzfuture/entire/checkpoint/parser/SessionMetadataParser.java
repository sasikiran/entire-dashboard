package com.mzfuture.entire.checkpoint.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/// Parses session-level metadata.json (CommittedMetadata) into SessionParseResult.
@Slf4j
@Component
public class SessionMetadataParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /// Parse session metadata.json into SessionParseResult. promptPreview must be set by caller from prompt.txt.
    public SessionParseResult parse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String sessionId = text(root, "session_id");
            String strategy = text(root, "strategy");
            Long sessionCreatedAt = parseIso8601ToEpochMs(text(root, "created_at"));
            String branch = text(root, "branch");
            Integer checkpointsCount = root.has("checkpoints_count") ? root.get("checkpoints_count").asInt() : null;
            int filesTouchedCount = 0;
            String filesTouchedJson = null;
            if (root.has("files_touched") && root.get("files_touched").isArray()) {
                JsonNode arr = root.get("files_touched");
                filesTouchedCount = arr.size();
                filesTouchedJson = objectMapper.writeValueAsString(arr);
            }
            String agent = text(root, "agent");

            long inputTokens = 0, outputTokens = 0;
            int apiCallCount = 0;
            if (root.has("token_usage") && root.get("token_usage").isObject()) {
                JsonNode tu = root.get("token_usage");
                inputTokens = tu.has("input_tokens") ? tu.get("input_tokens").asLong() : 0;
                outputTokens = tu.has("output_tokens") ? tu.get("output_tokens").asLong() : 0;
                apiCallCount = tu.has("api_call_count") ? tu.get("api_call_count").asInt() : 0;
            }

            int agentLines = 0, humanAdded = 0, humanModified = 0, humanRemoved = 0, totalCommitted = 0;
            BigDecimal agentPercentage = null;
            if (root.has("initial_attribution") && root.get("initial_attribution").isObject()) {
                JsonNode att = root.get("initial_attribution");
                agentLines = att.has("agent_lines") ? att.get("agent_lines").asInt() : 0;
                humanAdded = att.has("human_added") ? att.get("human_added").asInt() : 0;
                humanModified = att.has("human_modified") ? att.get("human_modified").asInt() : 0;
                humanRemoved = att.has("human_removed") ? att.get("human_removed").asInt() : 0;
                totalCommitted = att.has("total_committed") ? att.get("total_committed").asInt() : 0;
                if (att.has("agent_percentage") && !att.get("agent_percentage").isNull()) {
                    agentPercentage = att.get("agent_percentage").decimalValue();
                }
            }

            return SessionParseResult.builder()
                    .sessionId(sessionId)
                    .sessionIndex(null)
                    .strategy(strategy)
                    .sessionCreatedAt(sessionCreatedAt)
                    .branch(branch)
                    .checkpointsCount(checkpointsCount)
                    .filesTouchedCount(filesTouchedCount)
                    .filesTouchedJson(filesTouchedJson)
                    .agent(agent)
                    .inputTokens(inputTokens)
                    .outputTokens(outputTokens)
                    .apiCallCount(apiCallCount)
                    .agentLines(agentLines)
                    .humanAdded(humanAdded)
                    .humanModified(humanModified)
                    .humanRemoved(humanRemoved)
                    .totalCommitted(totalCommitted)
                    .agentPercentage(agentPercentage)
                    .promptPreview(null)
                    .build();
        } catch (Exception e) {
            log.warn("Parse session metadata failed: {}", e.getMessage());
            return null;
        }
    }

    /// Extract first line of prompt text for list preview.
    public String getFirstLineOfPrompt(String promptText) {
        if (promptText == null || promptText.isEmpty()) {
            return null;
        }
        int idx = promptText.indexOf('\n');
        return idx < 0 ? promptText.trim() : promptText.substring(0, idx).trim();
    }

    private static String text(JsonNode node, String key) {
        if (node == null || !node.has(key)) {
            return null;
        }
        JsonNode v = node.get(key);
        return v != null && v.isTextual() ? v.asText() : null;
    }

    private static Long parseIso8601ToEpochMs(String iso) {
        if (iso == null || iso.isEmpty()) {
            return null;
        }
        try {
            return Instant.parse(iso).toEpochMilli();
        } catch (Exception e) {
            return null;
        }
    }
}
