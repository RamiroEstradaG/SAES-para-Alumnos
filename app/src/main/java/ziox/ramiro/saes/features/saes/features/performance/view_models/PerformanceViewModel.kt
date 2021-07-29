package ziox.ramiro.saes.features.saes.features.performance.view_models

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.School
import ziox.ramiro.saes.features.saes.data.repositories.UserRepository
import ziox.ramiro.saes.features.saes.features.kardex.data.models.KardexData
import ziox.ramiro.saes.features.saes.features.kardex.data.repositories.KardexRepository
import ziox.ramiro.saes.features.saes.features.performance.data.models.PerformanceData
import ziox.ramiro.saes.features.saes.features.performance.data.models.TriStateBoolean
import ziox.ramiro.saes.features.saes.features.performance.data.repositories.PerformanceRepository
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.dismissAfterTimeout

class PerformanceViewModel(
    private val performanceRepository: PerformanceRepository,
    private val kardexRepository: KardexRepository,
    private val userRepository: UserRepository
): ViewModel() {
    val schoolPerformance = mutableStateOf<PerformanceData?>(null)
    val generalPerformance = mutableStateOf<PerformanceData?>(null)
    val careerPerformance = mutableStateOf<PerformanceData?>(null)
    val permissionToSaveData = mutableStateOf<TriStateBoolean?>(null)
    val error = MutableStateFlow<String?>(null)

    init {
        error.dismissAfterTimeout()
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

            kotlin.runCatching {
                kardexRepository.getMyKardexData()
            }.onSuccess {
                if(schoolPerformance.value == null
                    && careerPerformance.value == null
                    && generalPerformance.value == null){

                    updateMyPerformance(it, schoolName)
                    fetchCareerPerformance(it.careerName)
                    fetchGeneralPerformance()
                    fetchSchoolPerformance(schoolName)
                }
            }
        }
    }

    fun fetchCareerPerformance(careerName: String) = viewModelScope.launch {
        kotlin.runCatching {
            performanceRepository.getCareerPerformance(careerName).collect {
                careerPerformance.value = it
            }
        }.onFailure {
            error.value = "Error al obtener los datos de la carrera"
        }
    }

    fun fetchSchoolPerformance(schoolName: String) = viewModelScope.launch {
        kotlin.runCatching {
            performanceRepository.getSchoolPerformance(schoolName).collect {
                schoolPerformance.value = it
            }
        }.onFailure {
            error.value = "Error al obtener los datos de la escuela"
        }
    }

    fun fetchGeneralPerformance() = viewModelScope.launch {
        kotlin.runCatching {
            performanceRepository.getGeneralPerformance().collect {
                generalPerformance.value = it
            }
        }.onFailure {
            error.value = "Error al obtener los datos del IPN"
        }
    }

    fun updateMyPerformance(kardexData: KardexData, schoolName: String) = viewModelScope.launch {
        kotlin.runCatching {
            userRepository.update(mapOf(
                "school" to schoolName,
                "career" to kardexData.careerName,
                "kardexData" to kardexData,
                "generalScore" to kardexData.generalScore
            ))
        }.onFailure {
            it.printStackTrace()
            error.value = "Error al enviar tu rendimiento"
        }
    }
}