package ziox.ramiro.saes.view_models

import android.webkit.CookieManager
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.Auth
import ziox.ramiro.saes.data.models.Captcha
import ziox.ramiro.saes.data.repositories.AuthRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel(){
    val captcha = mutableStateOf<Captcha?>(null)
    val auth = mutableStateOf<Auth?>(Auth.Empty)
    val error = MutableStateFlow<String?>(null)
    val isLoggedIn = mutableStateOf<Boolean?>(null)
    private var isCaptchaLoading = false

    constructor(
        authRepository: AuthRepository,
        initCaptcha: Boolean
    ): this(authRepository){
        if (initCaptcha){
            fetchCaptcha()
        }
    }

    init {
        error.dismissAfterTimeout()
        checkSession()
    }

    fun fetchCaptcha() {
        if(isCaptchaLoading) return
        isCaptchaLoading = true
        viewModelScope.launch {
            captcha.value = null

            kotlin.runCatching {
                authRepository.getCaptcha()
            }.onSuccess {
                isCaptchaLoading = false
                captcha.value = it
            }.onFailure {
                it.printStackTrace()
                isCaptchaLoading = false
                fetchCaptcha()
                error.value = if(it is TimeoutCancellationException){
                    "Tiempo de espera excedido (10s)"
                }else{
                    "Error al obtener el captcha"
                }
            }
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
                "Tiempo de espera excedido (30s)"
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
                if (it !is TimeoutCancellationException) {
                    error.value = "Error al revisar la sesión"
                }
            }
        }
    }

    fun logout() = CookieManager.getInstance().removeAllCookies {
        isLoggedIn.value = false
    }
}