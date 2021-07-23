package ziox.ramiro.saes.features.saes.features.performance.view_models

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.School
import ziox.ramiro.saes.features.saes.data.repositories.UserFirebaseRepository
import ziox.ramiro.saes.features.saes.features.kardex.data.models.KardexDataRoom
import ziox.ramiro.saes.features.saes.features.kardex.data.repositories.KardexRoomRepository
import ziox.ramiro.saes.features.saes.features.performance.data.models.PerformanceData
import ziox.ramiro.saes.features.saes.features.performance.data.models.TriStateBoolean
import ziox.ramiro.saes.features.saes.features.performance.data.repositories.PerformanceRepository
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.dismissAfterTimeout
import ziox.ramiro.saes.utils.runOnDefaultThread

class PerformanceViewModel(
    private val performanceRepository: PerformanceRepository,
    private val userFirebaseRepository: UserFirebaseRepository,
    private val kardexRoomRepository: KardexRoomRepository
): ViewModel() {
    val schoolPerformance = mutableStateOf<PerformanceData?>(null)
    val generalPerformance = mutableStateOf<PerformanceData?>(null)
    val careerPerformance = mutableStateOf<PerformanceData?>(null)
    val permissionToSaveData = mutableStateOf<TriStateBoolean?>(null)
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asSharedFlow()

    init {
        _error.dismissAfterTimeout(3000)
    }

    fun checkPerformancePermissions(
        context: Context
    ) = viewModelScope.launch {
        val userPreferences = UserPreferences.invoke(context)
        val permission = userPreferences.getPreference(PreferenceKeys.PerformanceSaveDataPermission, null)

        permissionToSaveData.value = TriStateBoolean.fromBoolean(permission)

        if(permission == true){
            val schoolName = School
                .findSchoolByUrl(userPreferences.getPreference(PreferenceKeys.SchoolUrl, null) ?: "")
                ?.schoolName ?: "Unknown"

            val kardexData = runOnDefaultThread {
                kardexRoomRepository.getMyKardexData(userFirebaseRepository.userId)
            }

            if(kardexData != null
                && schoolPerformance.value == null
                && careerPerformance.value == null
                && generalPerformance.value == null){
                val kardex = kardexData.toKardexData()

                updateMyPerformance(kardexData, schoolName)
                fetchCareerPerformance(kardex.careerName)
                fetchGeneralPerformance()
                fetchSchoolPerformance(schoolName)
            }
        }
    }

    fun fetchCareerPerformance(careerName: String) = viewModelScope.launch {
        kotlin.runCatching {
            performanceRepository.getCareerPerformance(careerName).collect {
                careerPerformance.value = it
            }
        }.onFailure {
            _error.value = "Error al obtener los datos de la carrera"
        }
    }

    fun fetchSchoolPerformance(schoolName: String) = viewModelScope.launch {
        kotlin.runCatching {
            performanceRepository.getSchoolPerformance(schoolName).collect {
                schoolPerformance.value = it
            }
        }.onFailure {
            _error.value = "Error al obtener los datos de la escuela"
        }
    }

    fun fetchGeneralPerformance() = viewModelScope.launch {
        kotlin.runCatching {
            performanceRepository.getGeneralPerformance().collect {
                generalPerformance.value = it
            }
        }.onFailure {
            _error.value = "Error al obtener los datos del IPN"
        }
    }

    fun updateMyPerformance(kardexDataRoom: KardexDataRoom, schoolName: String) = viewModelScope.launch {
        val kardexJson = kardexDataRoom.data.toString()

        val kardex = kardexDataRoom.toKardexData()

        kotlin.runCatching {
            userFirebaseRepository.update(mapOf(
                "school" to schoolName,
                "career" to kardex.careerName,
                "kardex" to kardexJson,
                "generalScore" to kardex.generalScore
            ))
        }.onFailure {
            _error.value = "Error al enviar tu rendimiento"
        }
    }
}