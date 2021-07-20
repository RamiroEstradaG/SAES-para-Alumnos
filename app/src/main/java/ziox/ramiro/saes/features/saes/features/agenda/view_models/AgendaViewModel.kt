package ziox.ramiro.saes.features.saes.features.agenda.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.features.agenda.data.models.AgendaItem
import ziox.ramiro.saes.features.saes.features.agenda.data.repositories.AgendaRepository

class AgendaViewModel(
    private val agendaRepository: AgendaRepository
) : ViewModel() {
    val eventList = mutableStateOf<List<AgendaItem>?>(null)
    val error = mutableStateOf<String?>(null)

    fun fetchAgendaEvents(calendarId: String) = viewModelScope.launch {
        try {
            agendaRepository.getEvents(calendarId).collect {
                eventList.value = it
            }
        }catch (e: Exception){
            e.printStackTrace()
            error.value = "Error al obtener los eventos"
        }
    }
}