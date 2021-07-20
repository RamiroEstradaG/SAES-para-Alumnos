package ziox.ramiro.saes.data.data_providers

import android.annotation.SuppressLint
import android.content.Context
import android.net.http.SslError
import android.util.Log
import android.webkit.*
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import okhttp3.Headers
import org.json.JSONArray
import org.json.JSONObject
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.UtilsJavascriptInterface
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random


class WebViewProvider(
    context: Context,
    path: String = "/"
) {
    val webView = createWebView(context).also {
        it.addJavascriptInterface(ResultJavascriptInterface(), "JSI")
        Log.d("WebViewProvider", "JavascriptInterface Attached")
    }
    val javascriptInterfaceJobs = mutableMapOf<String, JavascriptInterfaceJob>()
    val url = UserPreferences.invoke(context).getPreference(PreferenceKeys.SchoolUrl, "") + path

    @Composable
    fun WebViewProviderDebugView() = AndroidView(
        modifier = Modifier.height(400.dp),
        factory = {
            webView
        }
    )

    companion object {
        fun scriptTemplate(jobId: String) = """
            javascript:
            function next(obj){
                window.JSI.result("$jobId", JSON.stringify(obj));
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
            function getSelectOptions(select, offset){
                var options = select.options;
                var textOptions = [];
                
                for(let i = offset ; i < options.length ; i++){
                    textOptions.push(options[i].innerText.trim());
                }
                
                return textOptions;
            }
            function radioGroupToFilterField(elementIds, labels, fieldName){
                var selectedIndex = 0;
                
                elementIds.forEach((id, index) => {
                    var element = byId(id);
                    
                    if(element.checked){
                        selectedIndex = index;
                    }
                });
                
                return {
                    ids: elementIds,
                    options: labels,
                    name: fieldName,
                    selectedIndex: selectedIndex,
                    type: "${FilterType.RADIO_GROUP.name}"
                };
            }
            function selectToFilterField(elementId, fieldName, offset){
                var element = byId(elementId);
                var options = element.options;
                
                var selectedIndex = options.selectedIndex >= offset ? options.selectedIndex - offset : "";
                
                return {
                    id: elementId,
                    name: fieldName,
                    selectedIndex: selectedIndex.toString(),
                    offset: offset,
                    options: getSelectOptions(element, offset),
                    type: "${FilterType.SELECT.name}"
                };
            }
        """.trimIndent()

        @SuppressLint("SetJavaScriptEnabled")
        private fun createWebView(context: Context) : WebView {
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


        fun generateJobId() : String{
            val charPool = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"

            return (1..12)
                .map { Random.nextInt(0, charPool.length) }
                .map(charPool::get)
                .joinToString("")
        }
    }

    suspend inline fun <reified T>scrap(
        script: String,
        reloadPage: Boolean = true,
        timeout: Long = 10000L,
        noinline resultAdapter: (ScrapResult) -> T
    ): T {
        val jobId = generateJobId()
        val scriptBase = "${scriptTemplate(jobId)}\n$script"

        return withTimeout(timeout){
            suspendCancellableCoroutine<ScrapResultAdapter<Any?>> {
                javascriptInterfaceJobs[jobId] = JavascriptInterfaceJob(jobId, false, resultAdapter, it)

                attachWebViewClient(jobId, it) {
                    if (reloadPage) {
                        webView.loadUrl(scriptBase)
                    }
                }

                if (reloadPage) {
                    webView.loadUrl(url)
                } else {
                    webView.loadUrl(scriptBase)
                }
            }.toPrimitiveType()
        }
    }

    fun handleResume(jobId: String, block: () -> Unit) {
        if(javascriptInterfaceJobs[jobId]?.isResumed == false){
            block()
            javascriptInterfaceJobs[jobId]?.isResumed = true
        }
    }

    suspend inline fun <reified T>runMultipleThenScrap(
        preRequests: List<String>,
        postRequest: String,
        reloadPage: Boolean = true,
        timeout: Long = 10000L,
        noinline resultAdapter: (ScrapResult) -> T
    ): T {
        val jobId = generateJobId()
        var currentScriptIndex = 0
        val postRequestBase = "${scriptTemplate(jobId)}\n$postRequest"

        return withTimeout(timeout){
            suspendCancellableCoroutine<ScrapResultAdapter<Any?>> {
                javascriptInterfaceJobs[jobId] = JavascriptInterfaceJob(jobId, false, resultAdapter, it)

                attachWebViewClient(jobId, it) {
                    if (currentScriptIndex <= preRequests.lastIndex) {
                        Log.d("WebViewProvider", "Running script at $currentScriptIndex")
                        webView.loadUrl("${scriptTemplate(jobId)}\n${preRequests[currentScriptIndex++]}")
                    } else {
                        webView.loadUrl(postRequestBase)
                    }
                }

                if (reloadPage) {
                    webView.loadUrl(url)
                } else {
                    Log.d("WebViewProvider", "Running script at $currentScriptIndex")
                    webView.loadUrl("${scriptTemplate(jobId)}\n${preRequests[currentScriptIndex++]}")
                }
            }.toPrimitiveType()
        }
    }

    suspend inline fun <reified T>runThenScrap(
        preRequest: String,
        postRequest: String,
        reloadPage: Boolean = true,
        timeout: Long = 10000L,
        noinline resultAdapter: (ScrapResult) -> T
    ): T {
        var isFirstLoad = true
        val jobId = generateJobId()
        val preRequestBase = "${scriptTemplate(jobId)}\n$preRequest"
        val postRequestBase = "${scriptTemplate(jobId)}\n$postRequest"

        return withTimeout(timeout){
            suspendCancellableCoroutine<ScrapResultAdapter<Any?>> {
                javascriptInterfaceJobs[jobId] = JavascriptInterfaceJob(jobId, false, resultAdapter, it)

                attachWebViewClient(jobId, it) {
                    if (reloadPage && isFirstLoad) {
                        webView.loadUrl(preRequestBase)
                        isFirstLoad = false
                    } else {
                        webView.loadUrl(postRequestBase)
                    }
                }

                if (reloadPage) {
                    webView.loadUrl(url)
                } else {
                    webView.loadUrl(preRequestBase)
                }
            }.toPrimitiveType()
        }
    }

    fun attachWebViewClient(
        jobId: String,
        continuation: Continuation<*>,
        onPageFinished: () -> Unit
    ){
        webView.webViewClient = object : WebViewClient(){
            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                handleResume(jobId){
                    continuation.resumeWithException(Exception("Error HTTP ${errorResponse?.statusCode}"))
                }
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
                handleResume(jobId){
                    continuation.resumeWithException(Exception("Error SSL ${error?.primaryError}"))
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                onPageFinished()
            }
        }
    }

    private fun getCookies() : Headers = runBlocking(Dispatchers.Main){
        Headers.Builder()
            .add("Cookie", CookieManager.getInstance()?.getCookie(url) ?: "")
            .build()
    }

    private inner class ResultJavascriptInterface {
        @JavascriptInterface
        fun result(jobId: String, resultJson: String){
            Log.d("WebViewProvider", "Result received for $jobId")
            runBlocking {
                handleResume(jobId){
                    javascriptInterfaceJobs[jobId]?.let { job ->
                        job.continuation.resume(
                            ScrapResultAdapter(
                                job.resultAdapter(ScrapResult(
                                    result = JSONObject(mapOf(
                                        "data" to try{
                                            JSONObject(resultJson)
                                        }catch (e: Exception){
                                            JSONArray(resultJson)
                                        }catch(e: Exception){
                                            e.printStackTrace()
                                            null
                                        }
                                    )),
                                    headers = getCookies()
                                ))
                            )
                        )
                    }

                }
            }
        }
    }
}

enum class FilterType{
    SELECT, RADIO_GROUP
}

data class JavascriptInterfaceJob(
    val jobId: String,
    var isResumed: Boolean,
    val resultAdapter: (ScrapResult) -> Any?,
    val continuation: Continuation<ScrapResultAdapter<Any?>>
)

data class ScrapResultAdapter<T>(
    val value: T
) {
    inline fun <reified T2> toPrimitiveType(): T2{
        return if(this.value is T2){
            this.value
        }else{
            throw TypeCastException()
        }
    }
}

data class ScrapResult(
    val result: JSONObject,
    val headers: Headers
)


