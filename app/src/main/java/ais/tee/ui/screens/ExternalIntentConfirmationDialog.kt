package ais.tee.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/** Requires explicit user consent before an intent URI leaves Aistee. */
@Composable
internal fun ExternalIntentConfirmationDialog(
    uri: Uri?,
    onDismiss: () -> Unit,
    onConfirm: (Uri) -> Unit
) {
    val pendingUri = uri ?: return
    val targetPackage = runCatching {
        Intent.parseUri(pendingUri.toString(), Intent.URI_INTENT_SCHEME).`package`
    }.getOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Open another app?") },
        text = {
            Text(
                targetPackage?.let { "This login wants to open $it." }
                    ?: "This login wants to leave Aistee and open another app."
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(pendingUri) }) { Text("Open") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Stay here") }
        }
    )
}
