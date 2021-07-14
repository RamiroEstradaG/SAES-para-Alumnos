package ziox.ramiro.saes.features.saes.features.ets_calendar.view_models

import ziox.ramiro.saes.data.models.ViewModelEvent
import ziox.ramiro.saes.data.models.ViewModelState
import ziox.ramiro.saes.features.saes.features.ets_calendar.data.models.ETSCalendarItem

sealed class ETSCalendarState : ViewModelState{
    class EventsLoading : ETSCalendarState()
    class EventsComplete(val events: List<ETSCalendarItem>) : ETSCalendarState()
}


sealed class ETSCalendarEvent : ViewModelEvent{
    class Error(val message: String): ETSCalendarEvent()
}
