package ziox.ramiro.saes.features.saes.features.agenda.view_models

import ziox.ramiro.saes.data.models.ViewModelEvent
import ziox.ramiro.saes.data.models.ViewModelState
import ziox.ramiro.saes.features.saes.features.agenda.data.models.AgendaItem

sealed class AgendaState : ViewModelState {
    class Loading : AgendaState()
    class Complete(val events: List<AgendaItem>): AgendaState()
}

sealed class AgendaEvent : ViewModelEvent {
    class Error(val message: String): AgendaEvent()
}
