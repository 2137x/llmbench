package ais.tee.ui.screens

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ais.tee.data.model.WebAiService

@Composable
internal fun rememberWebViewLifecycleStarted(
    webViewMap: Map<WebAiService, WebView>,
    selectedService: WebAiService
): Boolean {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentSelectedService by rememberUpdatedState(selectedService)
    var lifecycleStarted by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    lifecycleStarted = true
                    webViewMap[currentSelectedService]?.onResume()
                }
                Lifecycle.Event.ON_STOP -> {
                    lifecycleStarted = false
                    webViewMap.values.forEach(WebView::onPause)
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return lifecycleStarted
}
