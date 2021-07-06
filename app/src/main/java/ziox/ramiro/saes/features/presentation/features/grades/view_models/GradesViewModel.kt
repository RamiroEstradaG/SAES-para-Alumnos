package ziox.ramiro.saes.features.presentation.features.grades.view_models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.BaseViewModel
import ziox.ramiro.saes.features.presentation.features.grades.data.repositories.GradesRepository

class GradesViewModel(
    private val gradesRepository: GradesRepository
) : BaseViewModel<GradesState, GradesEvent>() {
    fun fetchGrades() = viewModelScope.launch {
        emitState(GradesState.GradesLoading())

        kotlin.runCatching {
            gradesRepository.getMyGrades()
        }.onSuccess {
            emitState(GradesState.GradesComplete(it))
        }.onFailure {
            emitEvent(GradesEvent.Error("Error al obtener las calificaciones"))
        }
    }
}