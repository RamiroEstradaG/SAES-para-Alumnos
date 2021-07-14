package ziox.ramiro.saes.data.repositories

import android.content.Context
import okhttp3.Headers
import ziox.ramiro.saes.data.data_providers.WebViewProvider
import ziox.ramiro.saes.data.models.Auth
import ziox.ramiro.saes.data.models.Captcha
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.isNetworkAvailable

interface AuthRepository {
    suspend fun getCaptcha() : Captcha
    suspend fun login(username: String, password: String, captcha: String) : Auth
    suspend fun isLoggedIn() : Boolean
}


class AuthWebViewRepository(
    private val context: Context
) : AuthRepository {
    private val webViewProvider = WebViewProvider(context)

    override suspend fun getCaptcha(): Captcha {
        return if(context.isNetworkAvailable()){
            webViewProvider.scrap(
                """
                var isLoggedIn = !(byId("ctl00_leftColumn_LoginUser_CaptchaCodeTextBox") != null);
                next({
                    isLoggedIn: isLoggedIn,
                    url: isLoggedIn ? "" : byId("c_default_ctl00_leftcolumn_loginuser_logincaptcha_CaptchaImage").src
                });
                """.trimIndent(),
                reloadPage = true
            ){
                val data = it.result.getJSONObject("data")
                Captcha(
                    data.getString("url"),
                    data.getBoolean("isLoggedIn"),
                    it.headers
                )
            }.also {
                println(it)
            }
        }else{
            Captcha("", true, Headers.of(mapOf()))
        }
    }

    override suspend fun login(username: String, password: String, captcha: String): Auth {
        return webViewProvider.runThenScrap(
            preRequest = """
                byId("ctl00_leftColumn_LoginUser_UserName").value = "$username";
                byId("ctl00_leftColumn_LoginUser_Password").value = "${password.replace(Regex("[\"\\\\]")) { matchResult -> "\\${matchResult.value}" }}";
                byId("ctl00_leftColumn_LoginUser_CaptchaCodeTextBox").value = "$captcha";
                byId("ctl00_leftColumn_LoginUser_LoginButton").click();
            """.trimIndent(),
            postRequest = """
                var error = byClass("failureNotification");
                next({
                    isLoggedIn: !(byId("ctl00_leftColumn_LoginUser_CaptchaCodeTextBox") != null),
                    errorMessage: error != null && error.length >= 3 ? error[2].innerText.trim() : ""
                });
            """.trimIndent(),
            reloadPage = false
        ){
            val data = it.result.getJSONObject("data")
            Auth(
                data.getBoolean("isLoggedIn"),
                data.getString("errorMessage")
            )
        }
    }

    override suspend fun isLoggedIn() = UserPreferences.invoke(context).run {
        if (getPreference(PreferenceKeys.SchoolUrl, null) == null) return@run false

        when {
            getPreference(PreferenceKeys.OfflineMode, false) -> true
            context.isNetworkAvailable() -> WebViewProvider(context).scrap(
                """
                next({
                    isLoggedIn: !(byId("ctl00_leftColumn_LoginUser_CaptchaCodeTextBox") != null)
                });
                """.trimIndent(),
                timeout = 3000
            ){
                it.result.getJSONObject("data").getBoolean("isLoggedIn")
            }
            else -> isAuthDataSaved()
        }
    }
}