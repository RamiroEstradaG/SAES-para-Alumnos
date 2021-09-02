package ziox.ramiro.saes.features.saes.features.performance.data.repositories

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import ziox.ramiro.saes.features.saes.features.performance.data.models.PerformanceData

interface PerformanceRepository {
    suspend fun getCareerPerformance(careerName: String): Flow<PerformanceData?>
    suspend fun getSchoolPerformance(schoolName: String): Flow<PerformanceData?>
    suspend fun getGeneralPerformance(): Flow<PerformanceData?>
}

class PerformanceFirebaseRepository: PerformanceRepository{
    val db = Firebase.firestore

    companion object{
        const val COLLECTION_ID_CAREER_STATISTICS = "career_statistics_v2"
        const val COLLECTION_ID_SCHOOL_STATISTICS = "school_statistics_v2"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getCareerPerformance(careerName: String): Flow<PerformanceData?> = callbackFlow {
        val subs = db.collection(COLLECTION_ID_CAREER_STATISTICS)
            .document(careerName)
            .addSnapshotListener { value, _ ->
                if(value != null){
                    trySend(value.toObject(PerformanceData::class.java))
                }
            }

        awaitClose { subs.remove() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getSchoolPerformance(schoolName: String): Flow<PerformanceData?> = callbackFlow {
        val subs = db.collection(COLLECTION_ID_SCHOOL_STATISTICS)
            .document(schoolName)
            .addSnapshotListener { value, _ ->
                if(value != null){
                    trySend(value.toObject(PerformanceData::class.java))
                }
            }

        awaitClose { subs.remove() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getGeneralPerformance(): Flow<PerformanceData?> = callbackFlow {
        val subs = db.collection(COLLECTION_ID_SCHOOL_STATISTICS)
            .document("IPN")
            .addSnapshotListener { value, _ ->
                if(value != null){
                    trySend(value.toObject(PerformanceData::class.java))
                }
            }

        awaitClose { subs.remove() }
    }
}