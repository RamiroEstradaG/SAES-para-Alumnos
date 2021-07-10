package ziox.ramiro.saes.data.repositories

import android.content.Context
import ziox.ramiro.saes.data.data_provider.createWebView
import ziox.ramiro.saes.data.data_provider.runThenScrap
import ziox.ramiro.saes.data.data_provider.scrap
import ziox.ramiro.saes.data.models.Auth
import ziox.ramiro.saes.data.models.Captcha

interface AuthRepository {
    suspend fun getCaptcha() : Captcha
    suspend fun login(username: String, password: String, captcha: String) : Auth
    suspend fun isNotLoggedIn() : Boolean
}


class AuthWebViewRepository(
    private val context: Context
) : AuthRepository {
    private val webView = createWebView(context)

    override suspend fun getCaptcha(): Captcha {
        return webView.scrap(
            """
            var isNotLoggedIn = byId("ctl00_leftColumn_LoginUser_CaptchaCodeTextBox") != null;
            next({
                isNotLoggedIn: isNotLoggedIn,
                url: isNotLoggedIn ? byId("c_default_ctl00_leftcolumn_loginuser_logincaptcha_CaptchaImage").src : ""
            });
            """.trimIndent(),
            loadNewUrl = true
        ){
            val data = it.result.getJSONObject("data")
            Captcha(
                data.getString("url"),
                data.getBoolean("isNotLoggedIn"),
                it.headers
            )
        }
    }

    override suspend fun login(username: String, password: String, captcha: String): Auth {
        return webView.runThenScrap(
            preRequest = """
                byId("ctl00_leftColumn_LoginUser_UserName").value = "$username";
                byId("ctl00_leftColumn_LoginUser_Password").value = "${password.replace(Regex("[\"\\\\]")) { matchResult -> "\\${matchResult.value}" }}";
                byId("ctl00_leftColumn_LoginUser_CaptchaCodeTextBox").value = "$captcha";
                byId("ctl00_leftColumn_LoginUser_LoginButton").click();
            """.trimIndent(),
            postRequest = """
                var error = byClass("failureNotification");
                next({
                    isNotLoggedIn: byId("ctl00_leftColumn_LoginUser_CaptchaCodeTextBox") != null,
                    errorMessage: error != null && error.length >= 3 ? error[2].innerText.trim() : ""
                });
            """.trimIndent(),
            loadNewUrl = false
        ){
            val data = it.result.getJSONObject("data")
            Auth(
                data.getBoolean("isNotLoggedIn"),
                data.getString("errorMessage")
            )
        }
    }

    override suspend fun isNotLoggedIn(): Boolean {
        return createWebView(context).scrap(
            """
            next({
                isNotLoggedIn: byId("ctl00_leftColumn_LoginUser_CaptchaCodeTextBox") != null
            });
            """.trimIndent()
        ){
            it.result.getJSONObject("data").getBoolean("isNotLoggedIn")
        }
    }
}