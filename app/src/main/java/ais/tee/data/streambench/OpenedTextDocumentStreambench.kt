package ais.tee.data.streambench

import ais.tee.data.document.OpenedTextDocument
import ais.tee.data.model.BenchToolPermission
import ais.tee.data.model.BenchToolSurface

/**
 * Android SAF adapter into the portable Streambench playlist-import action.
 *
 * Bounded file I/O remains owned by TextDocumentFileAccess. This bridge only forwards already opened
 * text into the shared action so Streambench does not grow a second document reader or permission path.
 */
internal fun OpenedTextDocument.executeStreambenchPlaylistImportAction(
    surface: BenchToolSurface,
    isEnabled: Boolean,
    grantedPermissions: Set<BenchToolPermission>,
    allowArtwork: Boolean = false
): StreambenchImportedPlaylistActionResult = StreambenchImportedPlaylistAction.execute(
    source = document.text,
    surface = surface,
    isEnabled = isEnabled,
    grantedPermissions = grantedPermissions,
    allowArtwork = allowArtwork
)
