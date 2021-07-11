package ziox.ramiro.saes.features.saes.features.home.view_models

import com.twitter.sdk.android.core.models.Tweet
import ziox.ramiro.saes.data.models.ViewModelEvent
import ziox.ramiro.saes.data.models.ViewModelState

sealed class HomeState : ViewModelState {
    class TweetsLoading : HomeState()
    class TweetsComplete(val tweets : List<Tweet>) : HomeState()
}

sealed class HomeEvent : ViewModelEvent {
    class Error(val message: String) : HomeEvent()
}
