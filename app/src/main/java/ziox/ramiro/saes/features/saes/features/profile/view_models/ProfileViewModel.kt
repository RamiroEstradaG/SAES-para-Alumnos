package ziox.ramiro.saes.features.saes.features.profile.view_models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.BaseViewModel
import ziox.ramiro.saes.features.saes.features.profile.data.repositories.UserRepository

class ProfileViewModel(
    private val userRepository: UserRepository
) : BaseViewModel<ProfileState, ProfileEvent>() {
    init {
        fetchMyData()
    }

    fun fetchMyData() = viewModelScope.launch {
        emitState(ProfileState.UserLoading())

        kotlin.runCatching { 
            userRepository.getMyUserData()
        }.onSuccess {
            emitState(ProfileState.UserComplete(it))
        }.onFailure {
            emitEvent(ProfileEvent.Error("Error al obtener los datos del usuario"))
        }
    }
}