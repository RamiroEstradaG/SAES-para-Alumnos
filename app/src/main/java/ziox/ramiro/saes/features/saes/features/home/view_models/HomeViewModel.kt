package ziox.ramiro.saes.features.saes.features.home.view_models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.BaseViewModel
import ziox.ramiro.saes.features.saes.data.models.HistoryItem
import ziox.ramiro.saes.features.saes.data.repositories.HistoryRoomRepository
import ziox.ramiro.saes.features.saes.features.home.data.repositories.TwitterRepository
import ziox.ramiro.saes.utils.runOnDefaultThread

class HomeViewModel(
    private val twitterRepository: TwitterRepository,
    private val historyRoomRepository: HistoryRoomRepository
) : BaseViewModel<HomeState, HomeEvent>() {
    private val _historyItems = MutableStateFlow<List<HistoryItem>?>(null)
    val historyItem = _historyItems.asSharedFlow()

    fun fetchUserHistory() = viewModelScope.launch {
        kotlin.runCatching {
            runOnDefaultThread { historyRoomRepository.getLastThree() }
        }.onSuccess {
            _historyItems.emit(it)
        }.onFailure {
            emitEvent(HomeEvent.Error("Error al obtener el historial"))
        }
    }

    fun fetchTweets() = viewModelScope.launch {
        emitState(HomeState.TweetsLoading())

        kotlin.runCatching {
            twitterRepository.getTimelineTweets()
        }.onSuccess {
            emitState(HomeState.TweetsComplete(it))
        }.onFailure {
            emitEvent(HomeEvent.Error("Error al obtener los Tweets"))
        }
    }
}