package ziox.ramiro.saes.view_models

import ziox.ramiro.saes.data.models.Auth
import ziox.ramiro.saes.data.models.Captcha
import ziox.ramiro.saes.data.models.ViewModelEvent
import ziox.ramiro.saes.data.models.ViewModelState

sealed class AuthState : ViewModelState {
    class LoadingCaptcha : AuthState()
    class CaptchaComplete(val captcha: Captcha) : AuthState()
    class LoadingLogin : AuthState()
    class LoginComplete(val auth: Auth): AuthState()
    class LogoutSuccess: AuthState()
}

sealed class AuthEvent : ViewModelEvent {
    class Error(val message: String) : AuthEvent()
}