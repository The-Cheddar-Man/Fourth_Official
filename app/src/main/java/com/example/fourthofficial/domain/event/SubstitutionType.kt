package com.example.fourthofficial.domain.event

enum class SubstitutionType(val label: String){
    TACTICAL("Tactical"),
    INJURY("Injury"), // Player cannot return
    HIA("H.I.A")
}