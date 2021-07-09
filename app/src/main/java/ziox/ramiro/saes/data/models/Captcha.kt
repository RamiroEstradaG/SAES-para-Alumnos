package ziox.ramiro.saes.data.models

import okhttp3.Headers

data class Captcha(
    val url: String,
    val isNotLoggedIn: Boolean,
    val headers: Headers
)
