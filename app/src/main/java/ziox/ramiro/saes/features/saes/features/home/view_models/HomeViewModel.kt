package ziox.ramiro.saes.features.saes.features.home.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.data.models.HistoryItem
import ziox.ramiro.saes.features.saes.data.repositories.HistoryRoomRepository
import ziox.ramiro.saes.features.saes.features.home.data.models.Tweet
import ziox.ramiro.saes.features.saes.features.home.data.repositories.TwitterRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout
import ziox.ramiro.saes.utils.runOnDefaultThread

class HomeViewModel(
    private val historyRoomRepository: HistoryRoomRepository,
    private val twitterRepository: TwitterRepository
) : ViewModel() {
    val historyItems = mutableStateOf<List<HistoryItem>?>(null)
    val tweets = mutableStateOf<List<Tweet>?>(null)
    val error = MutableStateFlow<String?>(null)

    init {
        fetchUserHistory()
//        fetchTweets()
        error.dismissAfterTimeout()
    }

    private fun fetchUserHistory() = viewModelScope.launch {
        historyItems.value = null

        kotlin.runCatching {
            runOnDefaultThread { historyRoomRepository.getLastThree() }
        }.onSuccess {
            historyItems.value = it
        }.onFailure {
            error.value = "Error al obtener el historial"
        }
    }

    private fun fetchTweets() = viewModelScope.launch {
        tweets.value = null

        kotlin.runCatching {
            twitterRepository.getTimelineTweets()
        }.onSuccess {
            tweets.value = it
        }.onFailure {
            it.printStackTrace()
            error.value = "Error al obtener las noticias"
        }
    }
}