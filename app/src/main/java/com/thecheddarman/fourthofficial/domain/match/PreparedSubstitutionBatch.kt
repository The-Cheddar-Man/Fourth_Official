package com.thecheddarman.fourthofficial.domain.match

import com.thecheddarman.fourthofficial.domain.event.SubstitutionType
import com.thecheddarman.fourthofficial.domain.id.PlayerId
import com.thecheddarman.fourthofficial.domain.id.TeamId

data class PreparedSubstitutionBatch(
    val teamId: TeamId,
    val substitutions: List<PreparedSubstitution> = emptyList()
)

data class PreparedSubstitution(
    val playerOffId: PlayerId,
    val playerOnId: PlayerId? = null,
    val type: SubstitutionType? = null
)