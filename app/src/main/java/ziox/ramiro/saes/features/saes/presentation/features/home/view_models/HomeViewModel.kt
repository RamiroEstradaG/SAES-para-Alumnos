package ziox.ramiro.saes.features.saes.presentation.features.home.view_models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.BaseViewModel
import ziox.ramiro.saes.features.saes.presentation.features.home.data.repositories.TwitterRepository

class HomeViewModel(
    private val twitterRepository: TwitterRepository
) : BaseViewModel<HomeState, HomeEvent>() {
    fun fetchTweets() = viewModelScope.launch {
        emitState(HomeState.TweetsLoading())

        kotlin.runCatching {
            twitterRepository.getTimelineTweets()
        }.onSuccess {
            emitState(HomeState.TweetsComplete(it))
        }.onFailure {
            it.printStackTrace()
            emitEvent(HomeEvent.Error("Error al obtener los Tweets"))
        }
    }
}