package ziox.ramiro.saes.data.data_providers

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import okhttp3.Headers
import org.json.JSONArray
import org.json.JSONObject
import ziox.ramiro.saes.data.models.SAESWebViewClient
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.UtilsJavascriptInterface
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random


class WebViewProvider(
    context: Context,
    val path: String = "/",
    val withTestFile: String? = null
) {
    val webView = createWebView(context).also {
        it.addJavascriptInterface(ResultJavascriptInterface(), "JSI")
        Log.d("WebViewProvider", "JavascriptInterface Attached")
    }
    val javascriptInterfaceJobs = mutableMapOf<String, JavascriptInterfaceJob>()
    val userPreferences = UserPreferences.invoke(context)

    @Composable
    fun WebViewProviderDebugView() = AndroidView(
        modifier = Modifier.height(600.dp),
        factory = {
            webView
        }
    )

    companion object {
        const val DEFAULT_TIMEOUT = 20000L

        fun scriptTemplate(jobId: String) = """
            javascript:
            function next(obj){
                window.JSI.result("$jobId", JSON.stringify(obj));
            }
            function throwError(error){
                window.JSI.error("$jobId", JSON.stringify({
                    error: error.message,
                    stack: error.stack,
                    currentPage: document.getElementsByTagName("html")[0].outerHTML
                }));
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

            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
            }

            webView.webChromeClient = object : WebChromeClient(){
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    Log.d("WebViewProvider", "Progress: $newProgress%")
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
        timeout: Long = DEFAULT_TIMEOUT,
        noinline resultAdapter: (ScrapResult) -> T
    ): T {
        val url = userPreferences.getPreference(PreferenceKeys.SchoolUrl, "") + path
        val performanceTrace = Firebase.performance.newTrace(url).also { it.start() }
        val jobId = generateJobId()
        val scriptBase = "${scriptTemplate(jobId)}\n$script"

        return withTimeout(timeout){
            suspendCancellableCoroutine<ScrapResultAdapter<Any?>> {
                it.invokeOnCancellation { performanceTrace.stop() }

                javascriptInterfaceJobs[jobId] = JavascriptInterfaceJob(jobId, false, resultAdapter, it)

                attachWebViewClient(jobId, it) {
                    if (reloadPage) {
                        webView.loadUrl(scriptBase)
                    }
                }

                if (reloadPage) {
                    if(withTestFile == null){
                        webView.loadUrl(url)
                    }else{
                        webView.loadUrl("file:///android_asset/${withTestFile}")
                    }
                } else {
                    webView.loadUrl(scriptBase)
                }
            }.toPrimitiveType<T>().also {
                performanceTrace.stop()
            }
        }
    }

    fun handleResume(jobId: String, block: () -> Unit) {
        if(javascriptInterfaceJobs[jobId]?.isResumed == false && javascriptInterfaceJobs[jobId]?.continuation?.isActive == true){
            kotlin.runCatching {
                block()
            }
            javascriptInterfaceJobs[jobId]?.isResumed = true
        }
    }

    suspend inline fun <reified T>runMultipleThenScrap(
        preRequests: List<String>,
        postRequest: String,
        reloadPage: Boolean = true,
        timeout: Long = DEFAULT_TIMEOUT,
        noinline resultAdapter: (ScrapResult) -> T
    ): T {
        val url = userPreferences.getPreference(PreferenceKeys.SchoolUrl, "") + path
        val performanceTrace = Firebase.performance.newTrace(url).also { it.start() }
        val jobId = generateJobId()
        var currentScriptIndex = 0
        val postRequestBase = "${scriptTemplate(jobId)}\n$postRequest"

        return withTimeout(timeout){
            suspendCancellableCoroutine<ScrapResultAdapter<Any?>> {
                it.invokeOnCancellation { performanceTrace.stop() }

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
                    if(withTestFile == null){
                        webView.loadUrl(url)
                    }else{
                        webView.loadUrl("file:///android_asset/${withTestFile}")
                    }
                } else {
                    Log.d("WebViewProvider", "Running script at $currentScriptIndex")
                    webView.loadUrl("${scriptTemplate(jobId)}\n${preRequests[currentScriptIndex++]}")
                }
            }.toPrimitiveType<T>().also {
                performanceTrace.stop()
            }
        }
    }

    suspend inline fun <reified T>runThenScrap(
        preRequest: String,
        postRequest: String,
        reloadPage: Boolean = true,
        timeout: Long = DEFAULT_TIMEOUT,
        noinline resultAdapter: (ScrapResult) -> T
    ): T {
        val url = userPreferences.getPreference(PreferenceKeys.SchoolUrl, "") + path
        val performanceTrace = Firebase.performance.newTrace(url).also { it.start() }
        var isFirstLoad = true
        val jobId = generateJobId()
        val preRequestBase = "${scriptTemplate(jobId)}\n$preRequest"
        val postRequestBase = "${scriptTemplate(jobId)}\n$postRequest"

        return withTimeout(timeout){
            suspendCancellableCoroutine<ScrapResultAdapter<Any?>> {
                it.invokeOnCancellation { performanceTrace.stop() }

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
                    if(withTestFile == null){
                        webView.loadUrl(url)
                    }else{
                        webView.loadUrl("file:///android_asset/${withTestFile}")
                    }
                } else {
                    webView.loadUrl(preRequestBase)
                }
            }.toPrimitiveType<T>().also {
                performanceTrace.stop()
            }
        }
    }

    fun attachWebViewClient(
        jobId: String,
        continuation: CancellableContinuation<ScrapResultAdapter<Any?>>,
        onPageFinished: () -> Unit
    ){
        webView.webViewClient = object : SAESWebViewClient(
            webView.context,
            jobId,
            continuation,
            { _jobId, _handler ->
                handleResume(_jobId, _handler)
            }
        ){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                onPageFinished()
            }
        }
    }

    private fun getCookies() : Headers = runBlocking(Dispatchers.Main){
        val url = userPreferences.getPreference(PreferenceKeys.SchoolUrl, "") + path
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

        @JavascriptInterface
        fun error(jobId: String, errorJson: String){
            Log.e("WebViewProvider", "Error received for $jobId")
            runBlocking {
                handleResume(jobId){
                    javascriptInterfaceJobs[jobId]?.let { job ->
                        val errorObject = JSONObject(errorJson)
                        val errorMessage = errorObject.optString("error", "Unknown error")
                        val stackTrace = errorObject.optString("stack", "")
                        val sourceCode = errorObject.optString("currentPage", "")

                        Log.e("WebViewProvider", "Error in job $jobId: $errorMessage\nStack trace: $stackTrace")

                        job.continuation.resumeWithException(
                            ScrapException(
                                message = errorMessage,
                                stackTrace = stackTrace,
                                sourceCode = sourceCode
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
    val continuation: CancellableContinuation<ScrapResultAdapter<Any?>>
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

class ScrapException(
    message: String,
    stackTrace: String? = null,
    val sourceCode: String? = null
) : Exception(
    "ScrapException: $message\nStack trace: $stackTrace"
)


