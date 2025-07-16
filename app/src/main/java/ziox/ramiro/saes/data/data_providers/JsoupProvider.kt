package ziox.ramiro.saes.data.data_providers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import okhttp3.Headers
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.runOnDefaultThread
import java.security.KeyManagementException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private fun socketFactory(): SSLSocketFactory {
    val trustAllCerts = arrayOf<TrustManager>(@SuppressLint("CustomX509TrustManager")
    object : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    })

    try {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        return sslContext.socketFactory
    } catch (e: Exception) {
        when (e) {
            is RuntimeException, is KeyManagementException -> {
                throw RuntimeException("Failed to create a SSL socket factory", e)
            }

            else -> throw e
        }
    }
}

suspend fun <T> Context.jsoup(path: String = "", lambda: Connection.Response.(Document) -> T): T {
    val userPreferences = UserPreferences.invoke(this)

    return runOnDefaultThread {
        val response = Jsoup
            .connect("${userPreferences.getPreference(PreferenceKeys.SchoolUrl, null)}${path}")
            .sslSocketFactory(socketFactory())
            .execute()

        val document = response.parse()

        lambda(response, document)
    }
}

suspend fun <T> Context.jsoupForm(
    path: String = "",
    data: Map<String, String>,
    lambda: Connection.Response.(Document) -> T
): T {
    val userPreferences = UserPreferences.invoke(this)

    return runOnDefaultThread {
        val request = Jsoup
            .connect("${userPreferences.getPreference(PreferenceKeys.SchoolUrl, null)}${path}")
            .sslSocketFactory(socketFactory())
            .execute()

        val document = request.parse()

        val hiddenElements = document.select("input[type=hidden]")
        val nameValue = HashMap<String, String>()

        for (elem in hiddenElements) {
            nameValue[elem.attr("name")] = elem.attr("value")
        }

        val response = Jsoup
            .connect("${userPreferences.getPreference(PreferenceKeys.SchoolUrl, null)}${path}")
            .sslSocketFactory(socketFactory())
            .data(data)
            .data(nameValue)
            .execute()

        lambda(response, document)
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun rememberJsoupPainter(imageUrl: String, headers: Headers? = null): ImagePainter {
    val context = LocalContext.current
    val userPreferences = remember {
        UserPreferences.invoke(context)
    }
    val body = remember {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(key1 = body) {
        runOnDefaultThread {
            kotlin.runCatching {
                val cookies = if (headers == null) {
                    Jsoup
                        .connect(
                            userPreferences.getPreference(PreferenceKeys.SchoolUrl, null) ?: ""
                        )
                        .sslSocketFactory(socketFactory())
                        .execute().cookies()
                } else null

                Jsoup
                    .connect(imageUrl)
                    .sslSocketFactory(socketFactory())
                    .ignoreContentType(true)
                    .headers(
                        headers?.toMap() ?: mapOf(
                        "Cookie" to (cookies?.entries?.joinToString("") { "${it.key}=${it.value}; " } ?: "")
                    ))
                    .execute()
            }.onSuccess {
                body.value = BitmapFactory.decodeStream(it.bodyStream())
            }
        }
    }

    return rememberImagePainter(
        data = body.value,
    )
}