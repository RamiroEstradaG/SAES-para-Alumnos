package ziox.ramiro.saes.features.saes.features.profile.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.data_providers.ScrapException
import ziox.ramiro.saes.features.saes.data.repositories.StorageRepository
import ziox.ramiro.saes.features.saes.features.profile.data.models.ProfileUser
import ziox.ramiro.saes.features.saes.features.profile.data.repositories.ProfileRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor (
    private val profileRepository: ProfileRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {
    val profile = mutableStateOf<ProfileUser?>(null)
    val error = MutableStateFlow<String?>(null)
    val scrapError = MutableStateFlow<ScrapException?>(null)

    init {
        error.dismissAfterTimeout()
        scrapError.dismissAfterTimeout(10000)
        fetchMyData()
    }

    private fun fetchMyData() {
        viewModelScope.launch {
            profile.value = null

            kotlin.runCatching {
                profileRepository.getMyUserData()
            }.onSuccess {
                profile.value = it
            }.onFailure {
                it.printStackTrace()
                if(it is ScrapException) {
                    scrapError.value = it
                } else {
                    it.printStackTrace()
                    fetchMyData()
                    error.value = "Error al obtener los datos del usuario"
                }
            }
        }
    }

    fun uploadSourceCode() = viewModelScope.launch {
        val error = scrapError.value
        scrapError.value = null
        if(error == null) return@launch

        val sourceCode = error.sourceCode ?: return@launch

        runCatching {
            storageRepository.uploadFile(
                content = sourceCode,
                filePath = "profile_errors",
                fileName = "${Date().time}.html"
            )
        }
    }
}