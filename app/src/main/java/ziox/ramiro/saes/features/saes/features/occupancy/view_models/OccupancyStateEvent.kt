package ziox.ramiro.saes.features.saes.features.occupancy.view_models

import ziox.ramiro.saes.data.models.ViewModelEvent
import ziox.ramiro.saes.data.models.ViewModelState
import ziox.ramiro.saes.features.saes.features.occupancy.data.models.ClassOccupancy

sealed class OccupancyState : ViewModelState {
    class Loading: OccupancyState()
    class Complete(val occupancyList: List<ClassOccupancy>): OccupancyState()
}

sealed class OccupancyEvent : ViewModelEvent {
    class Error(val message: String): OccupancyEvent()
}