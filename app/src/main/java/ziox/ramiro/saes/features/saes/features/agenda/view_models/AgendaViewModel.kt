package ziox.ramiro.saes.features.saes.features.agenda.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.data_providers.WebViewProvider
import ziox.ramiro.saes.features.saes.features.agenda.data.models.AgendaItem
import ziox.ramiro.saes.features.saes.features.agenda.data.repositories.AgendaRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout

class AgendaViewModel(
    private val agendaRepository: AgendaRepository,
    private val calendarId: String?
) : ViewModel() {
    val eventList = mutableStateOf<List<AgendaItem>?>(null)
    val isAddAgendaLoading = mutableStateOf(false)
    val isRemovingEvent = mutableStateOf<String?>(null)
    val error = MutableStateFlow<String?>(null)

    init {
        if(calendarId != null){
            fetchAgendaEvents(calendarId)
        }
        error.dismissAfterTimeout()
    }

    fun fetchAgendaEvents(calendarId: String) = viewModelScope.launch {
        kotlin.runCatching {
            agendaRepository.getEvents(calendarId).catch {
                error.value = "Error al obtener los eventos"
            }.collect {
                eventList.value = it
            }
        }.onFailure {
            error.value = if(it is TimeoutCancellationException){
                "Tiempo de espera excedido (${WebViewProvider.DEFAULT_TIMEOUT.div(1000)} s)"
            }else {
                "Error al obtener los eventos"
            }
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

    fun removeEvent(eventId: String) = viewModelScope.launch {
        if(calendarId != null){
            isRemovingEvent.value = eventId
            kotlin.runCatching {
                agendaRepository.removeEvent(calendarId, eventId)
            }.onFailure {
                error.value = "Error al eliminar la agenda"

            }
            isRemovingEvent.value = null
        }
    }
}