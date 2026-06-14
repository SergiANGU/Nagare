package org.nagare.project.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import org.nagare.project.data.model.Usuari

class UsuariRepository {

    private val db = Firebase.firestore

    private fun col() = db.collection("usuaris")

    suspend fun getUsuari(uid: String): Usuari? {
        val snap = col().document(uid).get()
        return if (snap.exists) snap.data<Usuari>() else null
    }

    suspend fun createUsuari(usuari: Usuari) {
        col().document(usuari.uid).set(usuari)
    }

    suspend fun updateUsuari(usuari: Usuari) {
        col().document(usuari.uid).set(usuari, merge = true)
    }
}
