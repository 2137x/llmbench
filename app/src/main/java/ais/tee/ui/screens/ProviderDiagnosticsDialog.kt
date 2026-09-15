package ais.tee.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ais.tee.data.model.WebAiService
import ais.tee.data.model.WebChatActivityStatus
import ais.tee.ui.theme.AccentCyan
import ais.tee.web.ProviderDiagnosticsProbeResult
import ais.tee.web.ProviderDiagnosticsSnapshot
import ais.tee.web.providerDiagnosticsProbeSummary

private const val DIAGNOSTIC_NONE_YET = "None yet"

@Composable
internal fun ProviderDiagnosticsDialog(
    service: WebAiService,
    host: String,
    providerOwned: Boolean,
    webViewPackage: String,
    isDesktopMode: Boolean,
    activityTrackingSupported: Boolean,
    activityStatus: WebChatActivityStatus,
    fileChooserRequests: Int,
    fileChooserMode: String?,
    fileChooserHost: String?,
    fileChooserAcceptTypes: String?,
    fileChooserOutcome: String?,
    probeResult: ProviderDiagnosticsProbeResult?,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
    onCopyReport: (String) -> Unit
) {
    val probeSummary = providerDiagnosticsProbeSummary(probeResult)
    val snapshot = ProviderDiagnosticsSnapshot(
        providerName = service.shortName,
        host = host,
        providerOwned = providerOwned,
        webViewPackage = webViewPackage,
        siteMode = if (isDesktopMode) "desktop" else "mobile",
        activityTracking = if (activityTrackingSupported) "verified" else "not verified",
        activityState = activityStatus.name.lowercase(),
        fileChooserRequests = fileChooserRequests,
        fileChooserMode = fileChooserMode ?: "none",
        fileChooserHost = fileChooserHost ?: "none",
        fileChooserAcceptTypes = fileChooserAcceptTypes ?: "none",
        fileChooserOutcome = fileChooserOutcome ?: "none",
        domProbeSummary = probeSummary
    )
    val safeReport = snapshot.safeReport()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.BugReport, contentDescription = null, tint = AccentCyan)
                Text("Provider diagnostics", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Privacy-safe diagnostics only: no page text, full URLs, cookies, tokens, form values or file names are collected.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                DiagnosticsLine("Provider", service.shortName)
                DiagnosticsLine("Host", host)
                DiagnosticsLine("Provider-owned page", if (providerOwned) "Yes" else "No")
                DiagnosticsLine("WebView", webViewPackage)
                DiagnosticsLine("Site mode", if (isDesktopMode) "Desktop" else "Mobile")
                DiagnosticsLine(
                    "Activity tracking",
                    if (activityTrackingSupported) "Verified" else "Not verified"
                )
                DiagnosticsLine("Current activity", activityStatus.name.lowercase())
                HorizontalDivider()
                DiagnosticsLine("File chooser requests", fileChooserRequests.toString())
                DiagnosticsLine("Picker mode", fileChooserMode ?: DIAGNOSTIC_NONE_YET)
                DiagnosticsLine("Last picker host", fileChooserHost ?: DIAGNOSTIC_NONE_YET)
                DiagnosticsLine("Accept types", fileChooserAcceptTypes ?: DIAGNOSTIC_NONE_YET)
                DiagnosticsLine("Last picker outcome", fileChooserOutcome ?: DIAGNOSTIC_NONE_YET)
                HorizontalDivider()
                DiagnosticsLine("DOM capability probe", probeSummary)
                Text(
                    "DOM counts are capability hints for verification, not proof that sign-in, upload or generation tracking works end to end.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Refresh")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onCopyReport(safeReport) }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Copy")
                }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    )
}

@Composable
private fun DiagnosticsLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
