package ziox.ramiro.saes.view_models

import ziox.ramiro.saes.data.models.Auth
import ziox.ramiro.saes.data.models.Captcha
import ziox.ramiro.saes.data.models.ViewModelEvent
import ziox.ramiro.saes.data.models.ViewModelState

sealed class AuthState : ViewModelState {
    class LoadingCaptcha : AuthState()
    class CaptchaComplete(val captcha: Captcha) : AuthState()

    class SessionCheckLoading : AuthState()
    class SessionCheckComplete(val isNotLoggedIn: Boolean) : AuthState()
}

sealed class AuthEvent : ViewModelEvent {
    class Error(val message: String) : AuthEvent()
    class LoadingLogin : AuthEvent()
    class LoginComplete(val auth: Auth): AuthEvent()
    class LogoutSuccess: AuthEvent()
}