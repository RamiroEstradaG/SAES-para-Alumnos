package ziox.ramiro.saes.features.saes.features.performance.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.School
import ziox.ramiro.saes.features.saes.data.repositories.UserFirebaseRepository
import ziox.ramiro.saes.features.saes.features.kardex.data.models.KardexData
import ziox.ramiro.saes.features.saes.features.kardex.data.repositories.KardexRepository
import ziox.ramiro.saes.features.saes.features.performance.data.models.PerformanceData
import ziox.ramiro.saes.features.saes.features.performance.data.repositories.PerformanceRepository
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.dismissAfterTimeout
import javax.inject.Inject

@HiltViewModel
class PerformanceViewModel @Inject constructor(
    private val performanceRepository: PerformanceRepository,
    private val kardexRepository: KardexRepository,
    private val userFirebaseRepository: UserFirebaseRepository,
    private val userPreferences: UserPreferences
): ViewModel() {
    val schoolPerformance = mutableStateOf<PerformanceData?>(null)
    val generalPerformance = mutableStateOf<PerformanceData?>(null)
    val careerPerformance = mutableStateOf<PerformanceData?>(null)
    val error = MutableStateFlow<String?>(null)

    init {
        error.dismissAfterTimeout()
        if(userPreferences.getPreference(PreferenceKeys.IsFirebaseEnabled, false)){
            uploadUserData()
        }
    }

    private fun uploadUserData() = viewModelScope.launch {
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

    private fun fetchCareerPerformance(careerName: String) = viewModelScope.launch {
        kotlin.runCatching {
            performanceRepository.getCareerPerformance(careerName).collect {
                careerPerformance.value = it
            }
        }.onFailure {
            error.value = "Error al obtener los datos de la carrera"
        }
    }

    private fun fetchSchoolPerformance(schoolName: String) = viewModelScope.launch {
        kotlin.runCatching {
            performanceRepository.getSchoolPerformance(schoolName).collect {
                schoolPerformance.value = it
            }
        }.onFailure {
            error.value = "Error al obtener los datos de la escuela"
        }
    }

    private fun fetchGeneralPerformance() = viewModelScope.launch {
        kotlin.runCatching {
            performanceRepository.getGeneralPerformance().collect {
                generalPerformance.value = it
            }
        }.onFailure {
            error.value = "Error al obtener los datos del IPN"
        }
    }

    private fun updateMyPerformance(kardexData: KardexData, schoolName: String) = viewModelScope.launch {
        kotlin.runCatching {
            userFirebaseRepository.update(mapOf(
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