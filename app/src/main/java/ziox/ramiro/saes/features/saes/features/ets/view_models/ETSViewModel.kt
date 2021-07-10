package ziox.ramiro.saes.features.saes.features.ets.view_models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.BaseViewModel
import ziox.ramiro.saes.features.saes.features.ets.data.repositories.ETSRepository

class ETSViewModel(
    private val etsRepository: ETSRepository
) : BaseViewModel<ETSState, ETSEvent>() {
    val availableETSStates = MutableStateFlow<ETSState?>(null)

    val scoresStates = MutableStateFlow<ETSState?>(null)

    init {
        viewModelScope.launch {
            states.filter {
                it is ETSState.ETSLoading || it is ETSState.ETSComplete
            }.collect {
                availableETSStates.emit(it)
            }
        }

        viewModelScope.launch {
            states.filter {
                it is ETSState.ScoresLoading || it is ETSState.ScoresComplete
            }.collect {
                scoresStates.emit(it)
            }
        }

        fetchAvailableETS()
        fetchETSScores()
    }

    fun fetchAvailableETS() = viewModelScope.launch {
        emitState(ETSState.ETSLoading())

        kotlin.runCatching {
            etsRepository.getAvailableETS()
        }.onSuccess {
            emitState(ETSState.ETSComplete(it))
        }.onFailure {
            emitEvent(ETSEvent.Error("Error al obtener ETS"))
        }
    }

    fun fetchETSScores() = viewModelScope.launch {
        emitState(ETSState.ScoresLoading())

        kotlin.runCatching {
            etsRepository.getETSScores()
        }.onSuccess {
            emitState(ETSState.ScoresComplete(it))
        }.onFailure {
            emitEvent(ETSEvent.Error("Error al obtener calificaciones de ETS"))
        }
    }

    fun enrollETS(etsIndex: Int) = viewModelScope.launch {
        emitState(ETSState.ETSLoading())

        kotlin.runCatching {
            etsRepository.enrollETS(etsIndex)
        }.onSuccess {
            emitState(ETSState.ETSComplete(it))
            fetchETSScores()
        }.onFailure {
            emitEvent(ETSEvent.Error("Error al inscribir ETS"))
        }
    }
}