package ais.tee.ui.screens

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class ChatMessageLayoutTest {
    @Test
    fun compactWidthKeepsPhoneBubbleCap() {
        assertEquals(340.dp, chatBubbleMaxWidth(360.dp))
    }

    @Test
    fun mediumWidthUsesAvailableSpaceWithGutter() {
        assertEquals(568.dp, chatBubbleMaxWidth(600.dp))
    }

    @Test
    fun expandedWidthStopsAtReadableCap() {
        assertEquals(640.dp, chatBubbleMaxWidth(900.dp))
    }
}
