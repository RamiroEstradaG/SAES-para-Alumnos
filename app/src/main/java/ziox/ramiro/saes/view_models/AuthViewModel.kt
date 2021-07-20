package ziox.ramiro.saes.view_models

import android.webkit.CookieManager
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.Auth
import ziox.ramiro.saes.data.models.Captcha
import ziox.ramiro.saes.data.repositories.AuthRepository

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel(){
    val captcha = mutableStateOf<Captcha?>(null)
    val auth = mutableStateOf<Auth?>(Auth.Empty)
    val error = mutableStateOf<String?>(null)
    val isLoggedIn = mutableStateOf<Boolean?>(null)

    init {
        checkSession()
        fetchCaptcha()
    }

    fun fetchCaptcha() {
        viewModelScope.launch {
            captcha.value = null

            kotlin.runCatching {
                authRepository.getCaptcha()
            }.onSuccess {
                if(it.url.isBlank() && !it.isLoggedIn){
                    fetchCaptcha()
                }else{
                    captcha.value = it
                }
            }.onFailure {
                fetchCaptcha()
                error.value = "Error al obtener el captcha"
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
            error.value = "Error al obtener el captcha"
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
                error.value = "Error al revisar la sesion"
            }
        }
    }

    fun logout() = CookieManager.getInstance().removeAllCookies {
        isLoggedIn.value = false
    }
}