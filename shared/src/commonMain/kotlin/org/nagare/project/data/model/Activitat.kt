package org.nagare.project.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Activitat(
    val id: String = "",
    val titol: String = "",
    val descripcio: String = "",
    val tipus: String = TipusActivitat.COMPETICIO.name,
    val dataInici: Long = 0L,
    val dataFi: Long? = null,
    val lloc: String = "",
    val inscrits: List<String> = emptyList(),
    val inscripcions: Map<String, String> = emptyMap()
)
