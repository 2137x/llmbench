package ais.tee.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import ais.tee.MainActivity
import ais.tee.ui.viewmodel.NavigationTab

internal object AisteeQuickActionNavigation {
    const val ACTION_OPEN_DESTINATION = "ais.tee.action.OPEN_DESTINATION"
    const val EXTRA_DESTINATION = "ais.tee.extra.DESTINATION"
    const val LEGACY_WIDGET_ACTION_OPEN_DESTINATION = "ais.tee.action.OPEN_WIDGET_DESTINATION"
    const val LEGACY_WIDGET_EXTRA_DESTINATION = "ais.tee.extra.WIDGET_DESTINATION"

    const val DESTINATION_WEB_AI = "web_ai"
    const val DESTINATION_COMPARE = "compare"
    const val DESTINATION_STUDIO = "studio"

    fun launchIntent(context: Context, destination: String): Intent =
        Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_DESTINATION
            data = Uri.parse("aistee://quick-action/$destination")
            putExtra(EXTRA_DESTINATION, destination)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

    fun isOpenDestinationAction(value: String?): Boolean =
        value == ACTION_OPEN_DESTINATION || value == LEGACY_WIDGET_ACTION_OPEN_DESTINATION

    fun destinationId(currentExtra: String?, legacyExtra: String?, dataLastPathSegment: String?): String? =
        currentExtra ?: legacyExtra ?: dataLastPathSegment

    fun destination(value: String?): NavigationTab? = when (value) {
        DESTINATION_WEB_AI -> NavigationTab.WEB_CHATS
        DESTINATION_COMPARE -> NavigationTab.COMPARE_HUB
        DESTINATION_STUDIO -> NavigationTab.STUDIO
        else -> null
    }
}
