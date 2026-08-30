package com.example.fourthofficial.domain.match

import com.example.fourthofficial.domain.event.SubstitutionType
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId

data class PreparedSubstitutionBatch(
    val teamId: TeamId,
    val substitutions: List<PreparedSubstitution> = emptyList()
)

data class PreparedSubstitution(
    val playerOffId: PlayerId,
    val playerOnId: PlayerId? = null,
    val type: SubstitutionType? = null
)