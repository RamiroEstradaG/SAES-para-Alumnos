package ziox.ramiro.saes.view_models

import android.webkit.CookieManager
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.repositories.AuthRepository
import ziox.ramiro.saes.data.models.BaseViewModel

class AuthViewModel(
    private val authRepository: AuthRepository
) : BaseViewModel<AuthState, AuthEvent>(){
    init {
        checkSession()
    }

    fun fetchCaptcha() {
        viewModelScope.launch {
            emitState(AuthState.LoadingCaptcha())

            kotlin.runCatching {
                authRepository.getCaptcha()
            }.onSuccess {
                emitState(AuthState.CaptchaComplete(it))
            }.onFailure {
                fetchCaptcha()
                emitEvent(AuthEvent.Error("Error al obtener el captcha"))
            }
        }
    }


    fun login(username: String, password: String, captcha: String) = viewModelScope.launch {
        emitEvent(AuthEvent.LoadingLogin())

        kotlin.runCatching {
            authRepository.login(username, password, captcha)
        }.onSuccess {
            emitEvent(AuthEvent.LoginComplete(it))
            if (it.isNotLoggedIn){
                fetchCaptcha()
            }
        }.onFailure {
            emitEvent(AuthEvent.Error("Error al obtener el captcha"))
            fetchCaptcha()
        }
    }

    fun checkSession() {
        viewModelScope.launch {
            emitState(AuthState.SessionCheckLoading())

            kotlin.runCatching {
                authRepository.isNotLoggedIn()
            }.onSuccess {
                emitState(AuthState.SessionCheckComplete(it))
            }.onFailure {
                checkSession()
                emitEvent(AuthEvent.Error("Error al revisar la sesion"))
            }
        }
    }

    fun logout() = CookieManager.getInstance().removeAllCookies {
        emitEvent(AuthEvent.LogoutSuccess())
    }
}