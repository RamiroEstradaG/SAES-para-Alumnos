package ziox.ramiro.saes.features.saes.data.repositories

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.nio.file.Paths

interface StorageRepository {
    suspend fun uploadFile(
        content: String,
        filePath: String = "",
        fileName: String,
        onProgress: ((Float) -> Unit)? = null
    )
}

class StorageFirebaseRepository : StorageRepository {
    private val storage = Firebase.storage

    override suspend fun uploadFile(
        content: String,
        filePath: String,
        fileName: String,
        onProgress: ((Float) -> Unit)?
    ) {
        val storageRef = storage.reference.child(Paths.get("files", filePath, fileName).toString())
        val byteArray = content.toByteArray()

        val uploadTask = storageRef.putBytes(byteArray)

        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat()
            onProgress?.invoke(progress)
        }.await()
    }
}