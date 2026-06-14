package org.nagare.project.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Entreno(
    val id: String = "",
    val titol: String = "",
    val data: Long = 0L,
    val lloc: String = "",
    val notes: String = "",
    val assistents: List<String> = emptyList()
)
