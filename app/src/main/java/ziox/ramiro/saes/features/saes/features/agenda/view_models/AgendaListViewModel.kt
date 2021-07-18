package ziox.ramiro.saes.features.saes.features.agenda.view_models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.BaseViewModel
import ziox.ramiro.saes.features.saes.features.agenda.data.repositories.AgendaRepository

class AgendaListViewModel(
    private val agendaRepository: AgendaRepository
) : BaseViewModel<AgendaListState, AgendaListEvent>() {

    fun fetchAgendas() = viewModelScope.launch {
        emitState(AgendaListState.Loading())

        kotlin.runCatching {
            agendaRepository.getCalendars().collect {
                emitState(AgendaListState.Complete(it))
            }
        }.onFailure {
            it.printStackTrace()
            emitEvent(AgendaListEvent.Error("Error al obtener la lista de agendas"))
        }
    }

}