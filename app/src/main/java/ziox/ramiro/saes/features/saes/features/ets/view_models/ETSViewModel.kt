package ziox.ramiro.saes.features.saes.features.ets.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.features.ets.data.models.ETS
import ziox.ramiro.saes.features.saes.features.ets.data.models.ETSScore
import ziox.ramiro.saes.features.saes.features.ets.data.repositories.ETSRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout

class ETSViewModel(
    private val etsRepository: ETSRepository
) : ViewModel() {
    val availableETS = mutableStateOf<List<ETS>?>(null)
    val scores = mutableStateOf<List<ETSScore>?>(null)
    val error = MutableStateFlow<String?>(null)

    init {
        fetchAvailableETS()
        fetchETSScores()
        error.dismissAfterTimeout()
    }

    fun fetchAvailableETS() {
        viewModelScope.launch {
            availableETS.value = null

            kotlin.runCatching {
                etsRepository.getAvailableETS()
            }.onSuccess {
                availableETS.value = it
            }.onFailure {
                fetchAvailableETS()
                error.value = "Error al obtener ETS"
            }
        }
    }

    fun fetchETSScores() {
        viewModelScope.launch {
            scores.value = null

            kotlin.runCatching {
                etsRepository.getETSScores()
            }.onSuccess {
                scores.value = it
            }.onFailure {
                fetchETSScores()
                error.value = "Error al obtener calificaciones de ETS"
            }
        }
    }

    fun enrollETS(etsIndex: Int) = viewModelScope.launch {
        availableETS.value = null

        kotlin.runCatching {
            etsRepository.enrollETS(etsIndex)
        }.onSuccess {
            availableETS.value = it
            fetchETSScores()
        }.onFailure {
            error.value = "Error al inscribir ETS"
        }
    }
}