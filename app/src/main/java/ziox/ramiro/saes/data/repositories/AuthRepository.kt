package ziox.ramiro.saes.data.repositories

import android.content.Context
import androidx.core.net.toUri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import ziox.ramiro.saes.data.data_providers.WebViewProvider
import ziox.ramiro.saes.data.models.Auth
import ziox.ramiro.saes.data.models.Captcha
import ziox.ramiro.saes.features.saes.data.repositories.UserFirebaseRepository
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.isNetworkAvailable

interface AuthRepository {
    suspend fun getCaptcha() : Captcha
    suspend fun login(username: String, password: String, captcha: String) : Auth
    suspend fun isLoggedIn() : Boolean
}


class AuthWebViewRepository(
    private val context: Context,
    private val withTestFile: String? = null
) : AuthRepository {
    private val webViewProvider = WebViewProvider(context, withTestFile = withTestFile)
    private val userPreferences = UserPreferences.invoke(context)

    override suspend fun getCaptcha(): Captcha {
        return if(context.isNetworkAvailable()){
            webViewProvider.scrap(
                """
                try{
                    var isLoggedIn = !(byId("ctl00_leftColumn_LoginUser_CaptchaCodeTextBox") != null);
                    next({
                        isLoggedIn: isLoggedIn,
                        url: byId("c_default_ctl00_leftcolumn_loginuser_logincaptcha_CaptchaImage").src
                    });
                }catch(e){
                    throwError(e);
                }
                """.trimIndent(),
                timeout = 10000,
                reloadPage = true
            ){
                val data = it.result.getJSONObject("data")
                Captcha(
                    data.getString("url"),
                    data.getBoolean("isLoggedIn"),
                    it.headers
                )
            }
        }else{
            throw Exception("Sin conexion a internet")
        }
    }

    override suspend fun login(username: String, password: String, captcha: String): Auth {
        return webViewProvider.runThenScrap(
            preRequest = """
                try{
                    byId("ctl00_leftColumn_LoginUser_UserName").value = "$username";
                    byId("ctl00_leftColumn_LoginUser_Password").value = "${password.replace(Regex("[\"\\\\]")) { matchResult -> "\\${matchResult.value}" }}";
                    byId("ctl00_leftColumn_LoginUser_CaptchaCodeTextBox").value = "$captcha";
                    byId("ctl00_leftColumn_LoginUser_LoginButton").click();
                }catch(e){
                    throwError(e);
                }
            """.trimIndent(),
            postRequest = """
                try{
                    var error = byClass("failureNotification");
                    next({
                        isLoggedIn: !(byId("ctl00_leftColumn_LoginUser_CaptchaCodeTextBox") != null),
                        errorMessage: error != null && error.length >= 3 ? error[2].innerText.trim() : ""
                    });
                }catch(e){
                    throwError(e);
                }
            """.trimIndent(),
            reloadPage = withTestFile != null,
            timeout = 30000
        ){
            val data = it.result.getJSONObject("data")
            Auth(
                data.getBoolean("isLoggedIn"),
                data.getString("errorMessage")
            )
        }.also {
            if(userPreferences.getPreference(PreferenceKeys.IsFirebaseEnabled, false)){
                tryRegisterUser(it, username, password)
            }
        }
    }

    private suspend fun tryRegisterUser(auth: Auth, username: String, password: String){
        val userFirebaseRepository = UserFirebaseRepository()

        if (auth.isLoggedIn){
            val schoolDomain = userPreferences.getPreference(PreferenceKeys.SchoolUrl, null)?.toUri()?.host?.replace("www.", "")

            kotlin.runCatching {
                if(userFirebaseRepository.isUserRegistered("${username}@${schoolDomain}")){
                    Firebase.auth.signInWithEmailAndPassword("${username}@${schoolDomain}", password).await()
                }else{
                    Firebase.auth.createUserWithEmailAndPassword("${username}@${schoolDomain}", password).await()
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    override suspend fun isLoggedIn() = userPreferences.run {
        when {
            getPreference(PreferenceKeys.SchoolUrl, null) == null -> false
            getPreference(PreferenceKeys.OfflineMode, false) -> true
            context.isNetworkAvailable() -> WebViewProvider(context).scrap(
                """
                next({
                    isLoggedIn: !(byId("ctl00_leftColumn_LoginUser_CaptchaCodeTextBox") != null)
                });
                """.trimIndent()
            ){
                it.result.getJSONObject("data").getBoolean("isLoggedIn")
            }
            else -> authData.value.isAuthDataSaved()
        }.also {
            if(!it){
                Firebase.auth.signOut()
            }
        }
    }
}