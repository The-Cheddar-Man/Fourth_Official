package com.example.fourthofficial.export

enum class PdfEventType(val label: String) {
    SCORE("Scores"),
    SUBSTITUTION("Substitutions"),
    DISCIPLINE("Discipline")
}

data class TeamPdfExportOptions(val includedEventTypes: Set<PdfEventType> = PdfEventType.entries.toSet())
{
    val hasSelectedEventTypes: Boolean
        get() = includedEventTypes.isNotEmpty()
}