package ziox.ramiro.saes.features.saes.features.ets.view_models

import ziox.ramiro.saes.data.models.ViewModelEvent
import ziox.ramiro.saes.data.models.ViewModelState
import ziox.ramiro.saes.features.saes.features.ets.data.models.ETS
import ziox.ramiro.saes.features.saes.features.ets.data.models.ETSScore

sealed class ETSState : ViewModelState{
    class ScoresLoading: ETSState()
    class ScoresComplete(val scores: List<ETSScore>): ETSState()

    class ETSLoading: ETSState()
    class ETSComplete(val etsList: List<ETS>): ETSState()
}

sealed class ETSEvent : ViewModelEvent{
    class Error(val message: String) : ETSEvent()
}
