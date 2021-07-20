package ziox.ramiro.saes.features.saes.features.profile.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.features.profile.data.models.ProfileUser
import ziox.ramiro.saes.features.saes.features.profile.data.repositories.ProfileRepository

class ProfileViewModel(
    private val userRepository: ProfileRepository
) : ViewModel() {
    val profile = mutableStateOf<ProfileUser?>(null)
    val error = mutableStateOf<String?>(null)

    init {
        fetchMyData()
    }

    fun fetchMyData() {
        viewModelScope.launch {
            profile.value = null

            kotlin.runCatching {
                userRepository.getMyUserData()
            }.onSuccess {
                profile.value = it
            }.onFailure {
                fetchMyData()
                error.value = "Error al obtener los datos del usuario"
            }
        }
    }
}