package ziox.ramiro.saes.view_models

import android.webkit.CookieManager
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.data_providers.WebViewProvider.Companion.DEFAULT_TIMEOUT
import ziox.ramiro.saes.data.models.Auth
import ziox.ramiro.saes.data.models.Captcha
import ziox.ramiro.saes.data.repositories.AuthRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout

class AuthViewModel(
    private val authRepository: AuthRepository,
    initCaptcha: Boolean = false
) : ViewModel(){
    val captcha = mutableStateOf<Captcha?>(null)
    val auth = mutableStateOf<Auth?>(Auth.Empty)
    val error = MutableStateFlow<String?>(null)
    val isLoggedIn = mutableStateOf<Boolean?>(null)
    val isCaptchaLoading = mutableStateOf(false)

    init {
        error.dismissAfterTimeout()
        checkSession()
        if (initCaptcha){
            fetchCaptcha()
        }
    }

    fun fetchCaptcha() {
        if(isCaptchaLoading.value) return
        isCaptchaLoading.value = true
        viewModelScope.launch {
            captcha.value = null

            kotlin.runCatching {
                authRepository.getCaptcha()
            }.onSuccess {
                captcha.value = it
            }.onFailure {
                it.printStackTrace()
                fetchCaptcha()
                error.value = if(it is TimeoutCancellationException){
                    "Tiempo de espera excedido (10s)"
                }else{
                    "Error al obtener el captcha"
                }
            }

            isCaptchaLoading.value = false
        }
    }


    fun login(username: String, password: String, captcha: String) = viewModelScope.launch {
        auth.value = null

        kotlin.runCatching {
            authRepository.login(username, password, captcha)
        }.onSuccess {
            auth.value = it
            if (!it.isLoggedIn){
                fetchCaptcha()
            }
        }.onFailure {
            auth.value = Auth.Empty
            error.value = if(it is TimeoutCancellationException){
                "Tiempo de espera excedido (${DEFAULT_TIMEOUT.div(1000)}s)"
            }else{
                "Error al iniciar sesión"
            }
            fetchCaptcha()
        }
    }

    fun checkSession() {
        viewModelScope.launch {
            isLoggedIn.value = null

            kotlin.runCatching {
                authRepository.isLoggedIn()
            }.onSuccess {
                isLoggedIn.value = it
            }.onFailure {
                checkSession()
                error.value = if(it is TimeoutCancellationException){
                    "Tiempo de espera excedido (${DEFAULT_TIMEOUT.div(1000)}s)"
                }else{
                    "Error al revisar la sesión"
                }
            }
        }
    }

    fun logout() = CookieManager.getInstance().removeAllCookies {
        isLoggedIn.value = false
    }
}