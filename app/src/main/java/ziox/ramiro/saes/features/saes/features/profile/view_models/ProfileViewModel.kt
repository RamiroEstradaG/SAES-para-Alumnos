package ziox.ramiro.saes.features.saes.features.profile.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.features.profile.data.models.ProfileUser
import ziox.ramiro.saes.features.saes.features.profile.data.repositories.ProfileRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    val profile = mutableStateOf<ProfileUser?>(null)
    val error = MutableStateFlow<String?>(null)

    init {
        error.dismissAfterTimeout()
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
                fetchMyData()
                error.value = "Error al obtener los datos del usuario"
            }
        }
    }
}