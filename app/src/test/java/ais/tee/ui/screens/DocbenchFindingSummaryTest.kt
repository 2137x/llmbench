package ais.tee.ui.screens

import ais.tee.data.security.TextFindingSeverity
import ais.tee.data.security.TextInspectionResult
import ais.tee.data.security.TextSafetyFinding
import org.junit.Assert.assertEquals
import org.junit.Test

class DocbenchFindingSummaryTest {
    @Test
    fun summaryLimitsVisibleFindingsWithoutHidingDetectedCount() {
        val findings = (1..3).map { index ->
            TextSafetyFinding(
                severity = TextFindingSeverity.LOW,
                kind = "test",
                label = "Finding $index",
                detail = "detail",
                offset = index - 1,
                length = 1,
                line = index,
                column = 1
            )
        }
        val inspection = TextInspectionResult(findings, detectedCount = 5, truncated = true)
        val summary = docbenchFindingSummary(inspection, limit = 2)

        assertEquals(2, summary.visibleFindings.size)
        assertEquals(3, summary.omittedCount)
        assertEquals(5, summary.detectedCount)
    }
}
