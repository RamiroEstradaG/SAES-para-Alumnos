package ziox.ramiro.saes.features.saes.features.schedule.view_models

import ziox.ramiro.saes.data.models.ViewModelEvent
import ziox.ramiro.saes.data.models.ViewModelState
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule

sealed class ScheduleState : ViewModelState{
    class ScheduleLoading : ScheduleState()
    class ScheduleComplete(val schedules: List<ClassSchedule>) : ScheduleState()
}

sealed class ScheduleEvent : ViewModelEvent{
    class Error(val message: String) : ScheduleEvent()
}