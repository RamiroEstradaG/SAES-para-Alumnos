package ziox.ramiro.saes.features.saes.features.schedule.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.repositories.ScheduleRepository

class ScheduleViewModel(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {
    val scheduleList = mutableStateOf<List<ClassSchedule>?>(null)
    val error = mutableStateOf<String?>(null)

    init {
        fetchMySchedule()
    }

    fun fetchMySchedule() = viewModelScope.launch {
        scheduleList.value = null

        kotlin.runCatching {
            scheduleRepository.getMySchedule()
        }.onSuccess {
            scheduleList.value = it
        }.onFailure {
            error.value = "Error al cargar el horario"
        }
    }
}