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
import ziox.ramiro.saes.features.saes.features.schedule_generator.models.reposotories.ScheduleGeneratorRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout
import ziox.ramiro.saes.utils.runOnDefaultThread

class ScheduleGeneratorViewModel(
    private val scheduleGeneratorRepository: ScheduleGeneratorRepository
): ViewModel() {
    val scheduleItems = mutableStateOf<List<GeneratorClassSchedule>?>(null)
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asSharedFlow()

    init {
        fetchSchedule()

        _error.dismissAfterTimeout(3000)
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
            _error.value = "Error al obtener los elementos del generador"
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
            _error.value = "Error al eliminar la clase al generador"
        }
    }

    private fun checkIfOccupied(list: List<GeneratorClassSchedule>, item: GeneratorClassSchedule): GeneratorClassSchedule?{
        for (classSchedule in list) {
            if(item.hourRange.start.toDouble() in classSchedule.hourRange.start.toDouble()..(classSchedule.hourRange.end.toDouble() - 0.001)
                || item.hourRange.end.toDouble() in (classSchedule.hourRange.start.toDouble() + 0.001)..classSchedule.hourRange.end.toDouble()){
                return classSchedule
            }
        }

        return null
    }

    fun addClassToGenerator(generatorClassSchedule: List<GeneratorClassSchedule>) = viewModelScope.launch {
        if(scheduleItems.value != null){
            scheduleItems.value?.let {
                val hourInterference = ArrayList<String>()

                generatorClassSchedule.forEach { newItem ->
                    val interference = checkIfOccupied(it, newItem)
                    if(interference != null && !hourInterference.contains(interference.className)){
                        hourInterference.add(interference.className)
                    }
                }

                if (hourInterference.isNotEmpty()) {
                    _error.value = hourInterference.joinToString(
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
                        _error.value = "Error al agregar la clase al generador"
                    }
                }
            }
        }
    }
}