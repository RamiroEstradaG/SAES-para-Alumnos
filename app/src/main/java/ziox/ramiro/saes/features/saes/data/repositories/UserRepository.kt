package ziox.ramiro.saes.features.saes.data.repositories

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ziox.ramiro.saes.features.saes.data.models.UserData

interface UserRepository {
    suspend fun getUserData(): UserData
    suspend fun getUserDataFlow(): Flow<UserData>
    suspend fun updateUserField(field: String, value: Any?): UserData
    suspend fun update(data: Map<String, Any?>): UserData
}

class UserFirebaseRepository: UserRepository{
    private val db = Firebase.firestore
    private val currentUser = Firebase.auth.currentUser
    private val functions = FirebaseFunctions.getInstance()

    companion object{
        const val COLLECTION_ID_USERS = "users_v3"
    }

    override suspend fun getUserData(): UserData {
        return db.collection(COLLECTION_ID_USERS)
            .document(currentUser?.uid ?: "")
            .get()
            .await()
            .toObject(UserData::class.java)!!
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getUserDataFlow() = callbackFlow {
        val subs = db.collection(COLLECTION_ID_USERS)
            .document(currentUser?.uid ?: "")
            .addSnapshotListener { value, _ ->
                val userData = value?.toObject(UserData::class.java)
                if(userData != null){
                    CoroutineScope(Dispatchers.Default).launch {
                        send(userData)
                    }
                }
            }

        awaitClose{ subs.remove() }
    }

    suspend fun isUserRegistered(userId: String) : Boolean {
        return functions.getHttpsCallable("isUserRegistered").call(userId).await().data as? Boolean ?: false
    }

    suspend fun deleteUser() = functions.getHttpsCallable("removeUser").call().await().data as? Boolean ?: false

    fun signOut() = Firebase.auth.signOut()

    suspend fun userExist(userId: String)
        = db.collection(COLLECTION_ID_USERS)
            .document(userId).get().await().exists()

    override suspend fun updateUserField(field: String, value: Any?): UserData {
        val ref = db.collection(COLLECTION_ID_USERS)
            .document(currentUser?.uid ?: "")

        ref.update(mapOf(
            field to value
        )).await()

        return ref.get().await().toObject(UserData::class.java)!!
    }

    override suspend fun update(data: Map<String, Any?>): UserData {
        val ref = db.collection(COLLECTION_ID_USERS)
            .document(currentUser?.uid ?: "")
        println("UID ${currentUser?.uid}")
        ref.update(data).await()
        return ref.get().await().toObject(UserData::class.java)!!
    }


}