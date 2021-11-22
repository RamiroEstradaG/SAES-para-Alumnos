package ziox.ramiro.saes.view_models

import android.webkit.CookieManager
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.data_providers.ScrapException
import ziox.ramiro.saes.data.data_providers.WebViewProvider.Companion.DEFAULT_TIMEOUT
import ziox.ramiro.saes.data.models.Auth
import ziox.ramiro.saes.data.models.Captcha
import ziox.ramiro.saes.data.repositories.AuthRepository
import ziox.ramiro.saes.features.saes.data.repositories.StorageRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository,
    initCaptcha: Boolean = false
) : ViewModel() {
    val captcha = mutableStateOf<Captcha?>(null)
    val auth = mutableStateOf<Auth?>(Auth.Empty)
    val error = MutableStateFlow<String?>(null)
    val scrapError = MutableStateFlow<ScrapException?>(null)
    val captchaScrapError = MutableStateFlow<ScrapException?>(null)
    val isLoggedIn = mutableStateOf<Boolean?>(null)
    private var isCaptchaLoading = false

    init {
        error.dismissAfterTimeout()
        scrapError.dismissAfterTimeout(10000)
        captchaScrapError.dismissAfterTimeout(10000)
        checkSession()
        if (initCaptcha) {
            fetchCaptcha()
        }
    }

    init {
        error.dismissAfterTimeout()
        checkSession()
    }

    fun fetchCaptcha() {
        if (isCaptchaLoading) return
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

                if (it is ScrapException) {
                    captchaScrapError.value = it
                } else {
                    error.value = if (it is TimeoutCancellationException) {
                        "Tiempo de espera excedido (10s)"
                    } else {
                        "Error al obtener el captcha"
                    }
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
            if (!it.isLoggedIn) {
                fetchCaptcha()
            }
        }.onFailure {
            it.printStackTrace()
            auth.value = Auth.Empty
            if (it is ScrapException) {
                scrapError.value = it
            } else {
                error.value = if (it is TimeoutCancellationException) {
                    "Tiempo de espera excedido (30s)"
                } else {
                    "Error al iniciar sesión"
                }
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
                error.value = if (it is TimeoutCancellationException) {
                    "Tiempo de espera excedido (${DEFAULT_TIMEOUT.div(1000)}s)"
                } else {
                    "Error al revisar la sesión"
                }
            }
        }
    }

    fun logout() = CookieManager.getInstance().removeAllCookies {
        isLoggedIn.value = false
    }

    fun uploadSourceCode(
        isCaptcha: Boolean = false
    ) = viewModelScope.launch {
        val error = if (isCaptcha) captchaScrapError.value else scrapError.value
        scrapError.value = null
        captchaScrapError.value = null
        if (error == null) return@launch

        val sourceCode = error.sourceCode ?: return@launch

        runCatching {
            storageRepository.uploadFile(
                content = sourceCode,
                filePath = if(isCaptcha) "captcha_errors" else "login_errors",
                fileName = "${Date().time}.html"
            )
        }
    }
}