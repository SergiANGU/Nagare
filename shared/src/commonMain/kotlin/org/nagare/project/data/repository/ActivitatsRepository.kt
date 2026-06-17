package org.nagare.project.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.nagare.project.data.model.Activitat

class ActivitatsRepository {

    private val db = Firebase.firestore

    private fun col() = db.collection("activitats")

    fun getActivitats(): Flow<List<Activitat>> =
        col().orderBy("dataInici", Direction.ASCENDING).snapshots().map { snap ->
            snap.documents.map { doc ->
                doc.data<Activitat>().copy(id = doc.id)
            }
        }

    suspend fun getActivitat(id: String): Activitat? {
        val snap = col().document(id).get()
        return if (snap.exists) snap.data<Activitat>().copy(id = snap.id) else null
    }

    suspend fun createActivitat(activitat: Activitat) {
        col().add(activitat)
    }

    suspend fun apuntar(uid: String, activitatId: String, categoria: String? = null) {
        if (categoria != null) {
            col().document(activitatId).update(
                "inscrits" to FieldValue.arrayUnion(uid),
                "inscripcions.$uid" to categoria
            )
        } else {
            col().document(activitatId).update("inscrits" to FieldValue.arrayUnion(uid))
        }
    }

    suspend fun desapuntar(uid: String, activitatId: String) {
        col().document(activitatId).update("inscrits" to FieldValue.arrayRemove(uid))
    }
}
