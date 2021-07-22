package ziox.ramiro.saes.features.saes.features.performance.data.repositories

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import ziox.ramiro.saes.features.saes.data.repositories.UserFirebaseRepository
import ziox.ramiro.saes.features.saes.features.kardex.data.models.KardexData
import ziox.ramiro.saes.features.saes.features.performance.data.models.PerformanceData

interface PerformanceRepository {
    suspend fun getCareerPerformance(careerName: String): PerformanceData?
    suspend fun getSchoolPerformance(schoolName: String): PerformanceData?
    suspend fun getGeneralPerformance(): PerformanceData?
    suspend fun updateMyPerformance(kardexData: KardexData)
}

class PerformanceFirebaseRepository: PerformanceRepository{
    val db = Firebase.firestore
    companion object{
        const val COLLECTION_ID_CAREER_STATISTICS = "career_statistics_v2"
        const val COLLECTION_ID_SCHOOL_STATISTICS = "school_statistics_v2"
    }

    override suspend fun getCareerPerformance(careerName: String): PerformanceData? {
        return db.collection(COLLECTION_ID_CAREER_STATISTICS)
            .document(careerName)
            .get().await().toObject(PerformanceData::class.java)
    }

    override suspend fun getSchoolPerformance(schoolName: String): PerformanceData? {
        return db.collection(COLLECTION_ID_SCHOOL_STATISTICS)
            .document(schoolName)
            .get().await().toObject(PerformanceData::class.java)
    }

    override suspend fun getGeneralPerformance(): PerformanceData? {
        return db.collection(COLLECTION_ID_SCHOOL_STATISTICS)
            .document("IPN")
            .get().await().toObject(PerformanceData::class.java)
    }

    override suspend fun updateMyPerformance(kardexData: KardexData) {
        db.collection(UserFirebaseRepository.COLLECTION_ID_USERS)
            .document()
    }
}