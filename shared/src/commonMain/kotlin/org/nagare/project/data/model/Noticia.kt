package org.nagare.project.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Noticia(
    val id: String = "",
    val titol: String = "",
    val cos: String = "",
    val tipus: String = TipusNoticia.GENERAL.name,
    val data: Long = 0L,
    val autorUid: String = ""
)
