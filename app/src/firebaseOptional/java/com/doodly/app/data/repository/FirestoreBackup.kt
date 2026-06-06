package com.doodly.app.data.repository

import android.net.Uri
import com.doodly.app.data.local.DiaryEntry
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Optional Firebase implementation. This source set is excluded from the core build.
 * See README.md for activation steps.
 */
class FirestoreBackup(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : BackupRepository {
    override suspend fun backup(entry: DiaryEntry): Result<Unit> = runCatching {
        val imageUrl = entry.imagePath?.let { path ->
            val imageRef = storage.reference.child("diary/${entry.date}_${File(path).name}")
            imageRef.putFile(Uri.fromFile(File(path))).awaitTask()
            imageRef.downloadUrl.awaitTask().toString()
        }
        val payload = mapOf(
            "date" to entry.date,
            "content" to entry.content,
            "mood" to entry.mood,
            "tags" to entry.tags,
            "imageUrl" to imageUrl,
            "createdAt" to entry.createdAt
        )
        firestore.collection("diary")
            .document(entry.date.toString())
            .set(payload)
            .awaitTask()
        Unit
    }
}

private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
}
