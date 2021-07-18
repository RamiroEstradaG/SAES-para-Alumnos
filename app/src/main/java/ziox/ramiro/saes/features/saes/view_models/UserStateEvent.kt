package ziox.ramiro.saes.features.saes.view_models

import ziox.ramiro.saes.data.models.ViewModelEvent
import ziox.ramiro.saes.data.models.ViewModelState

sealed class UserState: ViewModelState{
    class UserDoesNotExist: UserState()
}

sealed class UserEvent: ViewModelEvent{

}
