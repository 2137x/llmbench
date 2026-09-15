package ais.tee.data.engine

import ais.tee.data.model.AiProvider
import ais.tee.data.model.CHAT_ROLE_ASSISTANT
import ais.tee.data.model.CHAT_ROLE_USER
import ais.tee.data.model.ModelChatMessage
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenAiCompatibleProtocolTest {
    @Test
    fun exposesProviderSpecificEndpointsAndHeaders() {
        val deepSeek = requireNotNull(OpenAiCompatibleProtocol.configFor(AiProvider.DEEPSEEK))
        val openRouter = requireNotNull(OpenAiCompatibleProtocol.configFor(AiProvider.OPENROUTER))

        assertEquals("https://api.deepseek.com/chat/completions", deepSeek.endpointUrl)
        assertEquals("https://openrouter.ai/api/v1/models?output_modalities=text", openRouter.modelCatalogUrl)
        assertEquals("https://github.com/travnie/aistee", openRouter.extraHeaders["HTTP-Referer"])
        assertNull(OpenAiCompatibleProtocol.configFor(AiProvider.GEMINI))
    }

    @Test
    fun buildsProviderScopedConversationMessages() {
        val history = listOf(
            ModelChatMessage(id = "u1", sender = CHAT_ROLE_USER, text = "first"),
            ModelChatMessage(
                id = "or1",
                sender = CHAT_ROLE_ASSISTANT,
                provider = AiProvider.OPENROUTER,
                text = "router answer"
            ),
            ModelChatMessage(
                id = "ds1",
                sender = CHAT_ROLE_ASSISTANT,
                provider = AiProvider.DEEPSEEK,
                text = "deepseek answer"
            )
        )

        val messages = OpenAiCompatibleProtocol.buildMessages(
            prompt = "next",
            systemInstruction = "system",
            conversationHistory = history,
            provider = AiProvider.OPENROUTER
        )

        assertEquals(listOf("system", "first", "router answer", "next"), messages.map {
            it.jsonObject.getValue("content").jsonPrimitive.content
        })
    }

    @Test
    fun addsUsageRequestOnlyForOpenRouter() {
        val messages = buildJsonArray { }
        val openRouter = OpenAiCompatibleProtocol.buildRequestPayload(
            AiProvider.OPENROUTER, "openrouter/free", messages, stream = true
        )
        val deepSeek = OpenAiCompatibleProtocol.buildRequestPayload(
            AiProvider.DEEPSEEK, "deepseek-chat", messages, stream = false
        )

        assertTrue(openRouter.getValue("stream").jsonPrimitive.content.toBoolean())
        assertTrue("usage" in openRouter)
        assertFalse("usage" in deepSeek)
    }

    @Test
    fun decodesCompatibleStreamingMetadata() {
        val event = buildJsonObject {
            put("model", "anthropic/claude-sonnet-5")
            put("choices", buildJsonArray {
                add(buildJsonObject {
                    put("delta", buildJsonObject { put("content", "hello") })
                    put("finish_reason", "stop")
                })
            })
        }

        assertEquals("hello", OpenAiCompatibleProtocol.extractStreamText(event))
        assertEquals("anthropic/claude-sonnet-5", OpenAiCompatibleProtocol.extractModel(event))
        assertTrue(OpenAiCompatibleProtocol.isStreamComplete(event))
    }
}
