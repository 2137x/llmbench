package ais.tee.data.document

import ais.tee.data.model.BenchToolDataKind
import ais.tee.data.model.BenchToolInvocationMode
import ais.tee.data.model.BenchToolPermission
import ais.tee.data.model.BenchToolSurface
import ais.tee.data.model.BuiltInBenchTool
import ais.tee.data.model.BuiltInBenchToolAvailability
import ais.tee.data.model.availability

/** Result of one explicit, policy-gated Docbench line-ending normalization action. */
sealed interface DocbenchLineEndingNormalizeActionResult {
    data class Completed(
        val text: String,
        val changed: Boolean
    ) : DocbenchLineEndingNormalizeActionResult {
        override fun toString(): String =
            "DocbenchLineEndingNormalizeActionResult.Completed(text=<redacted>, changed=$changed)"
    }

    data class Blocked(
        val availability: BuiltInBenchToolAvailability
    ) : DocbenchLineEndingNormalizeActionResult
}

/** Explicit local EOL normalizer backed by the existing syntax-independent repair core. */
object DocbenchLineEndingNormalizeAction {
    fun availability(
        surface: BenchToolSurface,
        isEnabled: Boolean,
        grantedPermissions: Set<BenchToolPermission>
    ): BuiltInBenchToolAvailability = BuiltInBenchTool.DOCBENCH_DOCUMENT.availability(
        surface = surface,
        invocationMode = BenchToolInvocationMode.EXPLICIT_USER_ACTION,
        inputKind = BenchToolDataKind.TEXT,
        isEnabled = isEnabled,
        grantedPermissions = grantedPermissions,
        networkAvailable = false
    )

    fun execute(
        text: String,
        target: LineEnding,
        surface: BenchToolSurface,
        isEnabled: Boolean,
        grantedPermissions: Set<BenchToolPermission>
    ): DocbenchLineEndingNormalizeActionResult {
        val availability = availability(
            surface = surface,
            isEnabled = isEnabled,
            grantedPermissions = grantedPermissions
        )
        if (!availability.canOffer) {
            return DocbenchLineEndingNormalizeActionResult.Blocked(availability)
        }

        val document = TextDocument(
            text = text,
            hadUtf8Bom = false,
            lineEndings = TextDocumentCodec.detectLineEndings(text)
        )
        val repaired = DocumentDiagnostics.repair(
            document = document,
            normalizeTo = target
        )
        return DocbenchLineEndingNormalizeActionResult.Completed(
            text = repaired.document.text,
            changed = repaired.document.text != text
        )
    }
}
