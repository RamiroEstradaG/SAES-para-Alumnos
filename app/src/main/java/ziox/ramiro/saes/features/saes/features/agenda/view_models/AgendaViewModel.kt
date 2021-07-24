package ziox.ramiro.saes.features.saes.features.agenda.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.features.agenda.data.models.AgendaItem
import ziox.ramiro.saes.features.saes.features.agenda.data.repositories.AgendaRepository

class AgendaViewModel(
    private val agendaRepository: AgendaRepository,
    calendarId: String?
) : ViewModel() {
    val eventList = mutableStateOf<List<AgendaItem>?>(null)
    val isAddAgendaLoading = mutableStateOf(false)
    val error = MutableStateFlow<String?>(null)

    init {
        if(calendarId != null){
            fetchAgendaEvents(calendarId)
        }
    }

    fun fetchAgendaEvents(calendarId: String) = viewModelScope.launch {
        agendaRepository.getEvents(calendarId).catch {
            error.value = "Error al obtener los eventos"
        }.collect {
            eventList.value = it
        }
    }

    fun addAgendaEvent(agendaItem: AgendaItem) = viewModelScope.launch {
        isAddAgendaLoading.value = true

        kotlin.runCatching {
            agendaRepository.addEvent(agendaItem)
        }.onFailure {
            error.value = "Error al agregar el evento"
        }

        isAddAgendaLoading.value = false
    }
}