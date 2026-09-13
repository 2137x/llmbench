package com.twojstar.llmbench.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CreateMessageAppActionTest {
    @Test
    fun mapsVoiceMessageTextIntoIncomingSharePayload() {
        val payload = CreateMessageAppAction.payload(
            action = CreateMessageAppAction.ACTION,
            text = "Ask Claude about this",
        )

        assertEquals("Ask Claude about this", payload?.text)
        assertEquals(emptyList<String>(), payload?.uriStrings)
    }

    @Test
    fun rejectsUnrelatedActionsAndBlankMessages() {
        assertNull(CreateMessageAppAction.payload("other", "hello"))
        assertNull(CreateMessageAppAction.payload(CreateMessageAppAction.ACTION, "   "))
    }
}
