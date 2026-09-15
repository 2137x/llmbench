package ais.tee.ui.screens

import ais.tee.data.model.WebChatActivityStatus
import ais.tee.data.model.WebChatGenerationObservation
import ais.tee.data.model.nextWebChatActivityStatus
import ais.tee.data.model.webChatActivityStatusAfterEviction

internal const val INACTIVE_WEB_ACTIVITY_POLL_EVERY = 3

internal fun shouldApplyWebChatObservation(
    observation: WebChatGenerationObservation,
    isLiveService: Boolean,
    isSameWebView: Boolean,
    hasCurrentWebView: Boolean
): Boolean {
    if (isLiveService && isSameWebView) return true
    val canSurviveEviction = observation == WebChatGenerationObservation.GENERATING ||
        observation == WebChatGenerationObservation.COMPLETED ||
        observation == WebChatGenerationObservation.COMPLETED_WHILE_SELECTED
    return canSurviveEviction && (isSameWebView || !hasCurrentWebView)
}

internal fun nextObservedWebChatActivityStatus(
    previous: WebChatActivityStatus,
    observation: WebChatGenerationObservation,
    isSelected: Boolean,
    isLiveService: Boolean
): WebChatActivityStatus {
    val nextStatus = nextWebChatActivityStatus(previous, observation, isSelected)
    return if (isLiveService) nextStatus else webChatActivityStatusAfterEviction(nextStatus)
}

// The page observer latches completion, so inactive tracked tabs do not need every native poll.
internal fun shouldProbeWebChatActivity(
    trackingSupported: Boolean,
    isSelected: Boolean,
    pollTick: Int
): Boolean = trackingSupported &&
    (isSelected || pollTick % INACTIVE_WEB_ACTIVITY_POLL_EVERY == 0)

internal fun shouldApplyPendingDesktopMode(
    observation: WebChatGenerationObservation,
    trackingSupported: Boolean,
    isStableOffProviderPage: Boolean
): Boolean = when {
    observation == WebChatGenerationObservation.GENERATING -> false
    isStableOffProviderPage -> true
    !trackingSupported -> false
    observation != WebChatGenerationObservation.UNKNOWN -> true
    else -> false
}
