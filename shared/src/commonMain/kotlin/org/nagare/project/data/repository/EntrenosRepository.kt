package org.nagare.project.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.nagare.project.data.model.Entreno

class EntrenosRepository {

    private val db = Firebase.firestore

    private fun col() = db.collection("entrenos")

    fun getEntrenos(): Flow<List<Entreno>> =
        col().orderBy("data", Direction.ASCENDING).snapshots().map { snap ->
            snap.documents.map { doc ->
                doc.data<Entreno>().copy(id = doc.id)
            }
        }

    suspend fun createEntreno(entreno: Entreno) {
        col().add(entreno)
    }

    suspend fun apuntarAssistent(uid: String, entrenoId: String) {
        col().document(entrenoId).update("assistents" to FieldValue.arrayUnion(uid))
    }

    suspend fun desapuntarAssistent(uid: String, entrenoId: String) {
        col().document(entrenoId).update("assistents" to FieldValue.arrayRemove(uid))
    }
}
