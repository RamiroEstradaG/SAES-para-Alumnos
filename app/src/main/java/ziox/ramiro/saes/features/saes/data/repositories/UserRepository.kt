package ziox.ramiro.saes.features.saes.data.repositories

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import ziox.ramiro.saes.features.saes.data.models.UserData

interface UserRepository {
    suspend fun getUserData(): UserData
    suspend fun updateUserField(field: String, value: Any): UserData
}

class UserFirebaseRepository(
    private val userId: String
) : UserRepository{
    private val db = Firebase.firestore

    companion object{
        const val COLLECTION_ID_USERS = "users_v2"
    }

    override suspend fun getUserData(): UserData {
        return db.collection(COLLECTION_ID_USERS)
            .document(userId)
            .get()
            .await()
            .toObject(UserData::class.java)!!
    }

    override suspend fun updateUserField(field: String, value: Any): UserData {
        val ref = db.collection(COLLECTION_ID_USERS)
            .document(userId)

        ref.update(mapOf(
            field to value
        )).await()

        return ref.get().await().toObject(UserData::class.java)!!
    }

}