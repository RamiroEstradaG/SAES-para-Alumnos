package ziox.ramiro.saes.features.saes.features.agenda.view_models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.BaseViewModel
import ziox.ramiro.saes.features.saes.features.agenda.data.repositories.AgendaRepository

class AgendaViewModel(
    private val agendaRepository: AgendaRepository
) : BaseViewModel<AgendaState, AgendaEvent>() {
    fun fetchAgendaEvents(calendarId: String) = viewModelScope.launch {
        emitState(AgendaState.Loading())

        agendaRepository.getEvents(calendarId).collect {
            emitState(AgendaState.Complete(it))
        }
    }
}