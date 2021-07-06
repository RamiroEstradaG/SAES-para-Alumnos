package ziox.ramiro.saes.features.saes.presentation.features.home.view_models

import ziox.ramiro.saes.data.models.ViewModelEvent
import ziox.ramiro.saes.data.models.ViewModelState
import ziox.ramiro.saes.features.saes.presentation.features.home.data.models.MappedTweet

sealed class HomeState : ViewModelState {
    class TweetsLoading : HomeState()
    class TweetsComplete(val tweets : List<MappedTweet>) : HomeState()
}

sealed class HomeEvent : ViewModelEvent {
    class Error(val message: String) : HomeEvent()
}
