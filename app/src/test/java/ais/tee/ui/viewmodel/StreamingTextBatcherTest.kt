package ais.tee.ui.viewmodel

import ais.tee.data.model.AiProvider
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreamingTextBatcherTest {
    private val geminiTarget = StreamingTextTarget(
        generationId = 7L,
        messageId = "stream-user-gemini",
        provider = AiProvider.GEMINI,
        model = "gemini-test"
    )

    @Test
    fun drainGenerationCoalescesFragmentsPerTarget() {
        val batcher = StreamingTextBatcher()
        val openAiTarget = StreamingTextTarget(
            generationId = 7L,
            messageId = "stream-user-openai",
            provider = AiProvider.CHATGPT,
            model = "openai-test"
        )

        batcher.append(geminiTarget, "Hel")
        batcher.append(openAiTarget, "O")
        batcher.append(geminiTarget, "lo")

        assertEquals(
            listOf(
                StreamingTextBatch(geminiTarget, "Hello"),
                StreamingTextBatch(openAiTarget, "O")
            ),
            batcher.drainGeneration(7L)
        )
        assertFalse(batcher.hasPendingGeneration(7L))
    }

    @Test
    fun drainGenerationKeepsOtherGenerationPending() {
        val batcher = StreamingTextBatcher()
        val nextGeneration = geminiTarget.copy(generationId = 8L, messageId = "next")

        batcher.append(geminiTarget, "old")
        batcher.append(nextGeneration, "new")

        assertEquals(listOf(StreamingTextBatch(geminiTarget, "old")), batcher.drainGeneration(7L))
        assertTrue(batcher.hasPendingGeneration(8L))
        assertEquals(listOf(StreamingTextBatch(nextGeneration, "new")), batcher.drainGeneration(8L))
    }

    @Test
    fun discardRemovesOnlyFinishedStream() {
        val batcher = StreamingTextBatcher()
        val sibling = geminiTarget.copy(messageId = "sibling", provider = AiProvider.CLAUDE)

        batcher.append(geminiTarget, "finalized")
        batcher.append(sibling, "still pending")
        batcher.discard(generationId = 7L, messageId = geminiTarget.messageId)

        assertEquals(listOf(StreamingTextBatch(sibling, "still pending")), batcher.drainGeneration(7L))
    }

    @Test
    fun emptyDeltaDoesNotCreateBatch() {
        val batcher = StreamingTextBatcher()

        batcher.append(geminiTarget, "")

        assertFalse(batcher.hasPendingGeneration(7L))
        assertTrue(batcher.drainGeneration(7L).isEmpty())
    }

    @Test
    fun exclusiveAccessSerializesStreamingStateTransitions() {
        val batcher = StreamingTextBatcher()
        val firstEntered = CountDownLatch(1)
        val releaseFirst = CountDownLatch(1)
        val secondEntered = CountDownLatch(1)

        val first = thread(start = true) {
            batcher.withExclusiveAccess {
                firstEntered.countDown()
                releaseFirst.await(2, TimeUnit.SECONDS)
            }
        }
        assertTrue(firstEntered.await(2, TimeUnit.SECONDS))

        val second = thread(start = true) {
            batcher.withExclusiveAccess {
                secondEntered.countDown()
            }
        }
        assertFalse(secondEntered.await(100, TimeUnit.MILLISECONDS))

        releaseFirst.countDown()
        assertTrue(secondEntered.await(2, TimeUnit.SECONDS))
        first.join(2_000)
        second.join(2_000)
        assertFalse(first.isAlive)
        assertFalse(second.isAlive)
    }
}
