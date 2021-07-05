package ziox.ramiro.saes.view_models

import android.webkit.CookieManager
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.AuthRepository
import ziox.ramiro.saes.data.models.BaseViewModel

class AuthViewModel(
    private val authRepository: AuthRepository
) : BaseViewModel<AuthState, AuthEvent>(){
    fun fetchCaptcha() = viewModelScope.launch {
        emitState(AuthState.LoadingCaptcha())

        kotlin.runCatching {
            authRepository.getCaptcha()
        }.onSuccess {
            emitState(AuthState.CaptchaComplete(it))
        }.onFailure {
            emitEvent(AuthEvent.Error("Error al obtener el captcha"))
        }
    }


    fun login(username: String, password: String, captcha: String) = viewModelScope.launch {
        emitState(AuthState.LoadingLogin())

        kotlin.runCatching {
            authRepository.login(username, password, captcha)
        }.onSuccess {
            emitState(AuthState.LoginComplete(it))
            if (!it.isLoggedIn){
                fetchCaptcha()
            }
        }.onFailure {
            emitEvent(AuthEvent.Error("Error al obtener el captcha"))
            fetchCaptcha()
        }
    }

    fun logout(){
        CookieManager.getInstance().removeAllCookies {
            emitState(AuthState.LogoutSuccess())
        }
    }
}