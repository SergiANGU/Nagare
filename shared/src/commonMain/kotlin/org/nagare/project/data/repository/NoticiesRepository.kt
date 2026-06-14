package org.nagare.project.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.nagare.project.data.model.Noticia

class NoticiesRepository {

    private val db = Firebase.firestore

    private fun col() = db.collection("noticies")

    fun getNoticies(): Flow<List<Noticia>> =
        col().orderBy("data", Direction.DESCENDING).snapshots().map { snap ->
            snap.documents.map { doc ->
                doc.data<Noticia>().copy(id = doc.id)
            }
        }

    suspend fun createNoticia(noticia: Noticia) {
        col().add(noticia)
    }
}
