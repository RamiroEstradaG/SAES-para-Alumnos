package ziox.ramiro.saes.features.saes.features.schedule.view_models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.BaseViewModel
import ziox.ramiro.saes.features.saes.features.schedule.data.repositories.ScheduleRepository

class ScheduleViewModel(
    private val scheduleRepository: ScheduleRepository
) : BaseViewModel<ScheduleState, ScheduleEvent>() {
    fun fetchMySchedule() = viewModelScope.launch {
        emitState(ScheduleState.ScheduleLoading())

        kotlin.runCatching {
            scheduleRepository.getMySchedule()
        }.onSuccess {
            emitState(ScheduleState.ScheduleComplete(it))
        }.onFailure {
            emitEvent(ScheduleEvent.Error("Error al cargar el horario"))
        }
    }
}