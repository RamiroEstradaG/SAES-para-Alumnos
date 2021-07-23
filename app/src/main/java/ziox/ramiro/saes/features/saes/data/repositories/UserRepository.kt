package ziox.ramiro.saes.features.saes.data.repositories

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import ziox.ramiro.saes.features.saes.data.models.UserData

interface UserRepository {
    suspend fun getUserData(): UserData
    suspend fun updateUserField(field: String, value: Any?): UserData
    suspend fun update(data: Map<String, Any?>): UserData
}

class UserFirebaseRepository(
    val userId: String
) : UserRepository{
    private val db = Firebase.firestore

    companion object{
        const val COLLECTION_ID_USERS = "users_v2"
    }

    override suspend fun getUserData(): UserData {
        return safeUserDocument()
            .get()
            .await()
            .toObject(UserData::class.java)!!
    }

    private suspend fun safeUserDocument(): DocumentReference{
        val ref = db.collection(COLLECTION_ID_USERS)
            .document(userId)

        if (!ref.get().await().exists()){
            ref.set(UserData(
                id = userId
            )).await()
        }

        return ref
    }

    override suspend fun updateUserField(field: String, value: Any?): UserData {
        val ref = safeUserDocument()

        ref.update(mapOf(
            field to value
        )).await()

        return ref.get().await().toObject(UserData::class.java)!!
    }

    override suspend fun update(data: Map<String, Any?>): UserData {
        val ref = safeUserDocument()

        ref.update(data).await()

        return ref.get().await().toObject(UserData::class.java)!!
    }


}