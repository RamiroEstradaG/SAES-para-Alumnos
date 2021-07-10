package ziox.ramiro.saes.features.saes.features.kardex.view_models

import ziox.ramiro.saes.data.models.ViewModelEvent
import ziox.ramiro.saes.data.models.ViewModelState
import ziox.ramiro.saes.features.saes.features.kardex.data.models.KardexData

sealed class KardexState : ViewModelState {
    class Loading : KardexState()
    class Complete(val data: KardexData): KardexState()
}


sealed class KardexEvent : ViewModelEvent {
    class Error(val message: String) : KardexEvent()
}
