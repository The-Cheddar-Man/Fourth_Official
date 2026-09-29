package com.example.fourthofficial.export

enum class PdfEventType(val label: String) {
    SCORE("Scores"),
    SUBSTITUTION("Substitutions"),
    DISCIPLINE("Discipline")
}

enum class PdfMatchPeriod(val label: String, private val halfIndex: Int?) {
    FULL_MATCH(label = "Full match", halfIndex = null),
    FIRST_HALF(label = "First half", halfIndex = 1),
    SECOND_HALF(label = "Second half", halfIndex = 2);

    fun includesHalf(eventHalfIndex: Int): Boolean {
        return halfIndex == null || halfIndex == eventHalfIndex
    }
}

data class TeamPdfExportOptions(
    val includedEventTypes: Set<PdfEventType> = PdfEventType.entries.toSet(),
    val period: PdfMatchPeriod = PdfMatchPeriod.FULL_MATCH)
{
    val hasSelectedEventTypes: Boolean
        get() = includedEventTypes.isNotEmpty()
}