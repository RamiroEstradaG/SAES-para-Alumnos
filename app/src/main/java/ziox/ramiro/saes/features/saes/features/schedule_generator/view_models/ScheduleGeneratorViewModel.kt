package ziox.ramiro.saes.features.saes.features.schedule_generator.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.features.schedule.data.models.GeneratorClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule_generator.models.reposotories.ScheduleGeneratorRepository
import ziox.ramiro.saes.utils.runOnDefaultThread

class ScheduleGeneratorViewModel(
    private val scheduleGeneratorRepository: ScheduleGeneratorRepository
): ViewModel() {
    val scheduleItems = mutableStateOf<List<GeneratorClassSchedule>?>(null)
    val error = mutableStateOf<String?>(null)

    init {
        fetchSchedule()
    }

    fun fetchSchedule() = viewModelScope.launch {
        scheduleItems.value = null

        kotlin.runCatching {
            runOnDefaultThread {
                scheduleGeneratorRepository.fetchSchedule()
            }
        }.onSuccess {
            scheduleItems.value = it
        }.onFailure {
            error.value = "Error al obtener los elementos del generador"
        }
    }
}