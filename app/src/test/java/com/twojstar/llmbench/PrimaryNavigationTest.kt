package com.twojstar.llmbench

import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import com.twojstar.llmbench.navigation.LlmBenchQuickActionNavigation
import com.twojstar.llmbench.ui.viewmodel.NavigationTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrimaryNavigationTest {
    @Test
    fun studioToolsStayUnderOnePrimaryDestination() {
        listOf(
            NavigationTab.STUDIO,
            NavigationTab.INSTRUCTIONS,
            NavigationTab.YAML,
            NavigationTab.PLAYGROUND,
            NavigationTab.SKILLS
        ).forEach { tab -> assertTrue(tab.belongsToStudioSection()) }
    }

    @Test
    fun dailyChatDestinationsRemainIndependent() {
        assertFalse(NavigationTab.WEB_CHATS.belongsToStudioSection())
        assertFalse(NavigationTab.COMPARE_HUB.belongsToStudioSection())
    }

    @Test
    fun webChatUsesImmersiveNavigationShell() {
        assertFalse(showPrimaryNavigation(NavigationTab.WEB_CHATS))
        assertTrue(showPrimaryNavigation(NavigationTab.COMPARE_HUB))
        assertTrue(showPrimaryNavigation(NavigationTab.STUDIO))
    }

    @Test
    fun bottomNavigationTypesDoNotNeedExtraSystemInset() {
        assertTrue(navigationSuiteUsesBottomBar(NavigationSuiteType.NavigationBar))
        assertTrue(navigationSuiteUsesBottomBar(NavigationSuiteType.ShortNavigationBarCompact))
        assertTrue(navigationSuiteUsesBottomBar(NavigationSuiteType.ShortNavigationBarMedium))
        assertFalse(navigationSuiteUsesBottomBar(NavigationSuiteType.NavigationRail))
        assertFalse(navigationSuiteUsesBottomBar(NavigationSuiteType.WideNavigationRailCollapsed))
        assertFalse(navigationSuiteUsesBottomBar(NavigationSuiteType.WideNavigationRailExpanded))
    }

    @Test
    fun renderedPromptTestChatOpensProfilePlayground() {
        assertEquals(NavigationTab.PLAYGROUND, profilePlaygroundDestination())
    }

    @Test
    fun quickActionRoutingAcceptsCurrentAndLegacyActions() {
        assertTrue(
            LlmBenchQuickActionNavigation.isOpenDestinationAction(
                LlmBenchQuickActionNavigation.ACTION_OPEN_DESTINATION
            )
        )
        assertTrue(
            LlmBenchQuickActionNavigation.isOpenDestinationAction(
                LlmBenchQuickActionNavigation.LEGACY_WIDGET_ACTION_OPEN_DESTINATION
            )
        )
        assertFalse(LlmBenchQuickActionNavigation.isOpenDestinationAction("not-a-quick-action"))
    }

    @Test
    fun quickActionDestinationIdFallsBackToIntentData() {
        assertEquals(
            LlmBenchQuickActionNavigation.DESTINATION_COMPARE,
            LlmBenchQuickActionNavigation.destinationId(
                currentExtra = null,
                legacyExtra = null,
                dataLastPathSegment = LlmBenchQuickActionNavigation.DESTINATION_COMPARE,
            )
        )
    }

    @Test
    fun quickActionDestinationsMapToPrimaryTabs() {
        assertEquals(
            NavigationTab.WEB_CHATS,
            LlmBenchQuickActionNavigation.destination(LlmBenchQuickActionNavigation.DESTINATION_WEB_AI)
        )
        assertEquals(
            NavigationTab.COMPARE_HUB,
            LlmBenchQuickActionNavigation.destination(LlmBenchQuickActionNavigation.DESTINATION_COMPARE)
        )
        assertEquals(
            NavigationTab.STUDIO,
            LlmBenchQuickActionNavigation.destination(LlmBenchQuickActionNavigation.DESTINATION_STUDIO)
        )
        assertNull(LlmBenchQuickActionNavigation.destination("unknown"))
    }
}
