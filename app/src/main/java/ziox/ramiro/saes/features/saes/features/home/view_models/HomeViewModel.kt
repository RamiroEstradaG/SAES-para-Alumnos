package ziox.ramiro.saes.features.saes.features.home.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twitter.sdk.android.core.models.Tweet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.data.models.HistoryItem
import ziox.ramiro.saes.features.saes.data.repositories.HistoryRoomRepository
import ziox.ramiro.saes.features.saes.features.home.data.repositories.TwitterRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout
import ziox.ramiro.saes.utils.runOnDefaultThread

class HomeViewModel(
    private val twitterRepository: TwitterRepository,
    private val historyRoomRepository: HistoryRoomRepository
) : ViewModel() {
    val historyItems = mutableStateOf<List<HistoryItem>?>(null)
    val schoolTweets = mutableStateOf<List<Tweet>?>(null)
    val error = MutableStateFlow<String?>(null)

    init {
        fetchUserHistory()
        fetchTweets()
        error.dismissAfterTimeout()
    }

    fun fetchUserHistory() = viewModelScope.launch {
        historyItems.value = null

        kotlin.runCatching {
            runOnDefaultThread { historyRoomRepository.getLastThree() }
        }.onSuccess {
            historyItems.value = it
        }.onFailure {
            error.value = "Error al obtener el historial"
        }
    }

    fun fetchTweets() = viewModelScope.launch {
        schoolTweets.value = null

        kotlin.runCatching {
            twitterRepository.getTimelineTweets()
        }.onSuccess {
            schoolTweets.value = it
        }.onFailure {
            error.value = "Error al obtener los Tweets"
        }
    }
}