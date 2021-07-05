package ziox.ramiro.saes.data

import android.content.Context
import ziox.ramiro.saes.data.data_provider.createWebView
import ziox.ramiro.saes.data.data_provider.runThenScrap
import ziox.ramiro.saes.data.data_provider.scrap
import ziox.ramiro.saes.data.models.Auth
import ziox.ramiro.saes.data.models.Captcha

interface AuthRepository {
    suspend fun getCaptcha() : Captcha
    suspend fun login(username: String, password: String, captcha: String) : Auth
}


class AuthWebViewRepository(
    private val context: Context
) : AuthRepository {
    override suspend fun getCaptcha(): Captcha {
        return createWebView(context).scrap(
            """
            next({
                url: byId("c_default_ctl00_leftcolumn_loginuser_logincaptcha_CaptchaImage").src
            });
            """.trimIndent()
        ){
            val data = it.result.getJSONObject("data")
            Captcha(
                data.getString("url"),
                it.headers
            )
        }
    }

    override suspend fun login(username: String, password: String, captcha: String): Auth {
        return createWebView(context).runThenScrap(
            preRequest = """
                byId("ctl00_leftColumn_LoginUser_UserName").value = "$username";
                byId("ctl00_leftColumn_LoginUser_Password").value = "${password.replace(Regex("[\"\\\\]")) { matchResult -> "\\${matchResult.value}" }}";
                byId("ctl00_leftColumn_LoginUser_CaptchaCodeTextBox").value = "$captcha";
                byId("ctl00_leftColumn_LoginUser_LoginButton").click();
            """.trimIndent(),
            postRequest = """
                next({
                    isLoggedIn: byId("ctl00_leftColumn_LoginUser_CaptchaCodeTextBox") == null,
                    errorMessage: byClass("failureNotification")[2].innerText.trim()
                });
            """.trimIndent(),
        ){
            val data = it.result.getJSONObject("data")
            Auth(
                data.getBoolean("isLoggedIn"),
                data.getString("errorMessage")
            )
        }
    }
}