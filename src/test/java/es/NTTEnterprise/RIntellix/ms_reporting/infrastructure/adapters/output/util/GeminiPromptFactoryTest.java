package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.genai.types.Schema;

class GeminiPromptFactoryTest {

    @Test
    @DisplayName("Should build user message with provided scoring JSON")
    void buildUserMessage_success() {
        String json = "{\"score\": 100}";
        String result = GeminiPromptFactory.buildUserMessage(json);

        assertNotNull(result);
        assertTrue(result.contains("Datos de scoring (JSON):"));
        assertTrue(result.contains(json));
    }

    @Test
    @DisplayName("Should build response schema correctly")
    void responseSchema_success() {
        Schema schema = GeminiPromptFactory.responseSchema();

        assertNotNull(schema);
    }
}
