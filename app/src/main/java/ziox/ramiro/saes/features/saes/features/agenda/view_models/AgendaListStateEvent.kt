package ziox.ramiro.saes.features.saes.features.agenda.view_models

import ziox.ramiro.saes.data.models.ViewModelEvent
import ziox.ramiro.saes.data.models.ViewModelState
import ziox.ramiro.saes.features.saes.features.agenda.data.models.AgendaCalendar

sealed class AgendaListState: ViewModelState{
    class Loading : AgendaListState()
    class Complete(val events: List<AgendaCalendar>): AgendaListState()
}

sealed class AgendaListEvent: ViewModelEvent{
    class Error(val message: String): AgendaListEvent()
}
