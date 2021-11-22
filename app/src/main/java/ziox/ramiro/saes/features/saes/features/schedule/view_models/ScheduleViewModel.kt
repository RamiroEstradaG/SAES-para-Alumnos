package ziox.ramiro.saes.features.saes.features.schedule.view_models

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.CustomClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.repositories.ScheduleRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout
import ziox.ramiro.saes.utils.runOnDefaultThread
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    localAppDatabase: LocalAppDatabase
) : ViewModel() {
    private val customScheduleRoomRepository = localAppDatabase.customScheduleGeneratorRepository()
    val scheduleList = mutableStateListOf<ClassSchedule>()
    val isLoading = mutableStateOf(false)
    val error = MutableStateFlow<String?>(null)

    init {
        fetchMySchedule()
        error.dismissAfterTimeout()
    }

    private fun fetchMySchedule() = viewModelScope.launch {
        scheduleList.clear()
        isLoading.value = true

        kotlin.runCatching {
            scheduleRepository.getMySchedule()
        }.onSuccess {
            scheduleList.addAll(it)
        }.onFailure {
            error.value = "Error al cargar el horario"
        }

        isLoading.value = false
    }

    fun editClass(classSchedule: CustomClassSchedule) = viewModelScope.launch {
        val index = scheduleList.indexOfFirst {
            it.id == classSchedule.id
        }

        if(index in scheduleList.indices){
            runOnDefaultThread {
                customScheduleRoomRepository.removeClass(classSchedule.id)
                customScheduleRoomRepository.addClass(classSchedule)
            }

            scheduleList[index] = classSchedule.toClassSchedule()
        }
    }
}