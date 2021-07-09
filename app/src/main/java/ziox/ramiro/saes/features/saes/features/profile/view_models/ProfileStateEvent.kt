package ziox.ramiro.saes.features.saes.features.profile.view_models

import ziox.ramiro.saes.data.models.ViewModelEvent
import ziox.ramiro.saes.data.models.ViewModelState
import ziox.ramiro.saes.features.saes.features.profile.data.models.User

sealed class ProfileState : ViewModelState {
    class UserLoading: ProfileState()
    class UserComplete(val userData: User): ProfileState()
}

sealed class ProfileEvent : ViewModelEvent {
    class Error(val message: String): ProfileEvent()
}