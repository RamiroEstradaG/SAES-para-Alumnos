package ziox.ramiro.saes.data.data_provider

import android.annotation.SuppressLint
import android.content.Context
import android.net.http.SslError
import android.webkit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Headers
import org.json.JSONArray
import org.json.JSONObject
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.UtilsJavascriptInterface
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread
import kotlin.coroutines.suspendCoroutine


fun WebView.getCookies() : Headers = runBlocking(Dispatchers.Main){
    Headers.Builder()
        .add("Cookie", CookieManager.getInstance()?.getCookie(this@getCookies.url) ?: "")
        .build()
}


data class ScrapResult(
    val result: JSONObject,
    val headers: Headers
)


suspend fun <T>WebView.scrap(
    script: String,
    path: String = "/",
    loadNewUrl : Boolean = true,
    resultAdapter: (ScrapResult) -> T
): T {
    val url = UserPreferences.invoke(context).getPreference(PreferenceKeys.SchoolUrl, "")+path
    var isResumed = false

    return suspendCoroutine {
        val scriptBase = """
            javascript:
            function next(obj){
                window.JSI.result(JSON.stringify(obj));
            }
            function byId(id){
                return document.getElementById(id);
            }
            function byClass(className){
                return document.getElementsByClassName(className);
            }
            function byTag(tag){
                return document.getElementsByTagName(tag);
            }
            $script
        """.trimIndent()

        addJavascriptInterface(object {
            @JavascriptInterface
            fun result(resultJson: String){
                runBlocking {
                    if(!isResumed){
                        it.resumeWith(Result.success(resultAdapter(ScrapResult(
                            result = JSONObject(mapOf(
                                "data" to try{
                                    JSONObject(resultJson)
                                }catch (e: Exception){
                                    JSONArray(resultJson)
                                }
                            )),
                            headers = this@scrap.getCookies()
                        ))))
                        isResumed = true
                    }
                }
            }
        }, "JSI")

        webViewClient = object : WebViewClient(){
            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                if(!isResumed) {
                    it.resumeWith(Result.failure(Exception("Error HTTP ${errorResponse?.statusCode}")))
                    isResumed = true
                }
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
                if(!isResumed) {
                    it.resumeWith(Result.failure(Exception("Error SSL ${error?.primaryError}")))
                    isResumed = true
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                if (loadNewUrl){
                    view?.loadUrl(scriptBase)
                }
            }
        }

        if(loadNewUrl){
            loadUrl(url)
        }else{
            loadUrl(scriptBase)
        }

        thread(start = true) {
            Thread.sleep(10 * 1000)

            if(!isResumed){
                it.resumeWith(Result.failure(TimeoutException()))
                isResumed = true
            }
        }
    }
}


suspend fun <T>WebView.runThenScrap(
    preRequest: String,
    postRequest: String,
    path: String = "/",
    loadNewUrl: Boolean = true,
    resultAdapter: (ScrapResult) -> T
): T {
    val url = UserPreferences.invoke(context).getPreference(PreferenceKeys.SchoolUrl, "")+path
    var isFirstLoad = true
    var isResumed = false

    return suspendCoroutine {
        val preRequestScript = """
            javascript:
            function byId(id){
                return document.getElementById(id);
            }
            function byClass(className){
                return document.getElementsByClassName(className);
            }
            function byTag(tag){
                return document.getElementsByTagName(tag);
            }
            $preRequest
        """.trimIndent()

        val postRequestScript = """
            javascript:
            function next(obj){
                window.JSI.result(JSON.stringify(obj));
            }
            function byId(id){
                return document.getElementById(id);
            }
            function byClass(className){
                return document.getElementsByClassName(className);
            }
            function byTag(tag){
                return document.getElementsByTagName(tag);
            }
            $postRequest
        """.trimIndent()

        addJavascriptInterface(object {
            @JavascriptInterface
            fun result(resultJson: String){
                runBlocking {
                    if(!isResumed){
                        it.resumeWith(Result.success(resultAdapter(ScrapResult(
                            result = JSONObject(mapOf(
                                "data" to try{
                                    JSONObject(resultJson)
                                }catch (e: Exception){
                                    JSONArray(resultJson)
                                }
                            )),
                            headers = this@runThenScrap.getCookies()
                        ))))
                        isResumed = true
                    }
                }
            }
        }, "JSI")

        webViewClient = object : WebViewClient(){

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                if(!isResumed) {
                    it.resumeWith(Result.failure(Exception("Error HTTP ${errorResponse?.statusCode}")))
                    isResumed = true
                }
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
                if(!isResumed) {
                    it.resumeWith(Result.failure(Exception("Error SSL ${error?.primaryError}")))
                    isResumed = true
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                if(loadNewUrl && isFirstLoad){
                    view?.loadUrl(preRequestScript)
                    isFirstLoad = false
                }else{
                    view?.loadUrl(postRequestScript)
                }
            }
        }

        if(loadNewUrl){
            loadUrl(url)
        }else{
            loadUrl(preRequestScript)
        }

        thread(start = true) {
            Thread.sleep(10 * 1000)

            if(!isResumed){
                it.resumeWith(Result.failure(TimeoutException()))
                isResumed = true
            }
        }
    }
}


@SuppressLint("SetJavaScriptEnabled")
fun createWebView(context: Context) : WebView {
    val webView = WebView(context)

    webView.settings.javaScriptEnabled = true
    webView.settings.domStorageEnabled = true

    webView.webChromeClient = object : WebChromeClient(){
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            if(newProgress > 95) view?.stopLoading()
        }
    }

    webView.addJavascriptInterface(UtilsJavascriptInterface(), "Utils")

    return webView
}