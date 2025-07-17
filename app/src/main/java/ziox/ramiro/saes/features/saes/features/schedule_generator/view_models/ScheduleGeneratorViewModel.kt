package ziox.ramiro.saes.features.saes.features.schedule_generator.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.schedule.data.models.GeneratorClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.checkIfOccupied
import ziox.ramiro.saes.utils.dismissAfterTimeout
import ziox.ramiro.saes.utils.runOnDefaultThread
import javax.inject.Inject

@HiltViewModel
class ScheduleGeneratorViewModel @Inject constructor(
    localAppDatabase: LocalAppDatabase
): ViewModel() {
    private val scheduleGeneratorRepository = localAppDatabase.scheduleGeneratorRepository()
    val scheduleItems = mutableStateOf<List<GeneratorClassSchedule>?>(null)
    val error = MutableStateFlow<String?>(null)

    init {
        fetchSchedule()
        error.dismissAfterTimeout()
    }

    private fun fetchSchedule() = viewModelScope.launch {
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

    fun removeClass(classId: String) = viewModelScope.launch {
        kotlin.runCatching {
            runOnDefaultThread {
                scheduleGeneratorRepository.removeClass(classId)
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
                    val interference = checkIfOccupied(it.map { it.scheduleDayTime }, newItem.scheduleDayTime)
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
                        it.printStackTrace()
                        error.value = "Error al agregar la clase al generador"
                    }
                }
            }
        }
    }
}