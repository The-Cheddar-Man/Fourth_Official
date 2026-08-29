package com.example.fourthofficial.domain.event

enum class DisciplineType(val label: String){
    YELLOW("Yellow Card"),
    RED("Red Card")
}


sealed interface DisciplineReason {
    val label: String
}

enum class DisciplineReasonYellow(override val label: String) : DisciplineReason{
    TECHNICAL("Technical"),
    FOUL_PLAY("Foul Play"),
}

enum class DisciplineReasonRed(override val label: String) : DisciplineReason{
    DANGEROUS_PLAY("Dangerous Play"),
    SERIOUS_FOUL_PLAY("Foul Play"),
    VIOLENT_CONDUCT("Violent Conduct")
}