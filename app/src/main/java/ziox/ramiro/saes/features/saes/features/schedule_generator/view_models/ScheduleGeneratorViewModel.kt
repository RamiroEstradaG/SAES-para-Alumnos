package ziox.ramiro.saes.features.saes.features.schedule_generator.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.features.schedule.data.models.GeneratorClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.checkIfOccupied
import ziox.ramiro.saes.features.saes.features.schedule_generator.models.reposotories.ScheduleGeneratorRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout
import ziox.ramiro.saes.utils.runOnDefaultThread

class ScheduleGeneratorViewModel(
    private val scheduleGeneratorRepository: ScheduleGeneratorRepository
): ViewModel() {
    val scheduleItems = mutableStateOf<List<GeneratorClassSchedule>?>(null)
    val error = MutableStateFlow<String?>(null)

    init {
        fetchSchedule()
        error.dismissAfterTimeout()
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

    fun removeClass(className: String, group: String) = viewModelScope.launch {
        kotlin.runCatching {
            runOnDefaultThread {
                scheduleGeneratorRepository.removeClass(className, group)
            }
        }.onSuccess {
            fetchSchedule()
        }.onFailure {
            error.value = "Error al eliminar la clase al generador"
        }
    }

    fun addClassToGenerator(generatorClassSchedule: List<GeneratorClassSchedule>) = viewModelScope.launch {
        if(scheduleItems.value != null){
            scheduleItems.value?.let {
                val hourInterference = ArrayList<String>()

                generatorClassSchedule.forEach { newItem ->
                    val interference = checkIfOccupied(it.map { it.hourRange }, newItem.hourRange)
                    if(interference != null && !hourInterference.contains(it[interference].className)){
                        hourInterference.add(it[interference].className)
                    }
                }

                if (hourInterference.isNotEmpty()) {
                    error.value = hourInterference.joinToString(
                        prefix = "Esta materia interfiere con: ",
                        postfix = "."
                    )
                }else{
                    kotlin.runCatching {
                        runOnDefaultThread {
                            generatorClassSchedule.forEach {
                                scheduleGeneratorRepository.addClass(it)
                            }
                        }
                    }.onSuccess {
                        fetchSchedule()
                    }.onFailure {
                        error.value = "Error al agregar la clase al generador"
                    }
                }
            }
        }
    }
}