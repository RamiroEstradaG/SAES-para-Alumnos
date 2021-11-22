package ziox.ramiro.saes.features.saes.features.home.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.data.models.HistoryItem
import ziox.ramiro.saes.features.saes.features.home.data.models.Tweet
import ziox.ramiro.saes.features.saes.features.home.data.repositories.TwitterRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout
import ziox.ramiro.saes.utils.runOnDefaultThread
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    localAppDatabase: LocalAppDatabase,
    private val twitterRepository: TwitterRepository
) : ViewModel() {
    private val historyRoomRepository = localAppDatabase.historyRepository()
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