package ais.tee.data.document

import ais.tee.data.model.BenchToolDataKind
import ais.tee.data.model.BenchToolInvocationMode
import ais.tee.data.model.BenchToolPermission
import ais.tee.data.model.BenchToolSurface
import ais.tee.data.model.BuiltInBenchTool
import ais.tee.data.model.BuiltInBenchToolAvailability
import ais.tee.data.model.availability

/** Result of one explicit, policy-gated Docbench JSON formatting action. */
sealed interface DocbenchJsonFormatActionResult {
    data class Completed(
        val text: String,
        val changed: Boolean
    ) : DocbenchJsonFormatActionResult {
        override fun toString(): String =
            "DocbenchJsonFormatActionResult.Completed(text=<redacted>, changed=$changed)"
    }

    data class Rejected(val message: String) : DocbenchJsonFormatActionResult

    data class Blocked(
        val availability: BuiltInBenchToolAvailability
    ) : DocbenchJsonFormatActionResult
}

/** Explicit local JSON formatter backed by the existing fidelity-preserving Docbench core. */
object DocbenchJsonFormatAction {
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
        surface: BenchToolSurface,
        isEnabled: Boolean,
        grantedPermissions: Set<BenchToolPermission>
    ): DocbenchJsonFormatActionResult {
        val availability = availability(
            surface = surface,
            isEnabled = isEnabled,
            grantedPermissions = grantedPermissions
        )
        if (!availability.canOffer) {
            return DocbenchJsonFormatActionResult.Blocked(availability)
        }

        val formatted = StructuredTextDiagnostics.formatJson(text)
        return if (formatted.isSuccess) {
            DocbenchJsonFormatActionResult.Completed(
                text = formatted.text,
                changed = formatted.changed
            )
        } else {
            DocbenchJsonFormatActionResult.Rejected(
                message = formatted.errorMessage ?: "Could not format JSON."
            )
        }
    }
}
