package ziox.ramiro.saes.features.saes.features.agenda.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.features.agenda.data.models.AgendaCalendar
import ziox.ramiro.saes.features.saes.features.agenda.data.repositories.AgendaRepository

class AgendaListViewModel(
    private val agendaRepository: AgendaRepository
) : ViewModel() {
    val agendaList = mutableStateOf<List<AgendaCalendar>?>(null)
    val error = mutableStateOf<String?>(null)

    fun fetchAgendas() = viewModelScope.launch {
        kotlin.runCatching {
            agendaRepository.getCalendars().collect {
                agendaList.value = it
            }
        }.onFailure {
            it.printStackTrace()
            error.value = "Error al obtener la lista de agendas"
        }
    }

}