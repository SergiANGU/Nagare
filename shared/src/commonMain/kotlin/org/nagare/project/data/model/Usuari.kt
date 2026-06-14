package org.nagare.project.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Usuari(
    val uid: String = "",
    val nom: String = "",
    val cognoms: String = "",
    val dni: String = "",
    val dataNaixement: String = "",
    val email: String = "",
    val rol: String = Rol.MEMBRE.name,
    val creatEl: Long = 0L
)
