package ais.tee.ui.viewmodel

import ais.tee.data.model.AiProvider

internal data class StreamingTextTarget(
    val generationId: Long,
    val messageId: String,
    val provider: AiProvider,
    val model: String
)

internal data class StreamingTextBatch(
    val target: StreamingTextTarget,
    val text: String
)

internal class StreamingTextBatcher {
    private val lock = Any()
    private val pending = linkedMapOf<StreamingTextTarget, StringBuilder>()

    fun append(target: StreamingTextTarget, delta: String) {
        if (delta.isEmpty()) return
        synchronized(lock) {
            pending.getOrPut(target) { StringBuilder() }.append(delta)
        }
    }

    fun hasPendingGeneration(generationId: Long): Boolean = synchronized(lock) {
        pending.keys.any { it.generationId == generationId }
    }

    fun drainGeneration(generationId: Long): List<StreamingTextBatch> = synchronized(lock) {
        buildList {
            val iterator = pending.entries.iterator()
            while (iterator.hasNext()) {
                val (target, text) = iterator.next()
                if (target.generationId == generationId) {
                    add(StreamingTextBatch(target, text.toString()))
                    iterator.remove()
                }
            }
        }
    }

    fun discard(generationId: Long, messageId: String) {
        synchronized(lock) {
            val iterator = pending.keys.iterator()
            while (iterator.hasNext()) {
                val target = iterator.next()
                if (target.generationId == generationId && target.messageId == messageId) {
                    iterator.remove()
                }
            }
        }
    }
}
