package ziox.ramiro.saes.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import com.google.firebase.perf.FirebasePerformance
import kotlin.math.absoluteValue


fun createWebView(context: Context?, client: WebViewClient, progressBar: ProgressBar?) : WebView {
    val webView = WebView(context!!)
    initWebView(webView, client, progressBar)

    return webView
}

@SuppressLint("SetJavaScriptEnabled")
fun initWebView(webView: WebView, client: WebViewClient, progressBar: ProgressBar?){
    webView.settings.javaScriptEnabled = true
    webView.settings.domStorageEnabled = true

    webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

    progressBar?.visibility = View.VISIBLE

    webView.webChromeClient = object : WebChromeClient(){
        var trackInitialized = false
        val trace = FirebasePerformance.getInstance().newTrace("webViewLoading")

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            progressBar?.isIndeterminate = false
            progressBar?.progress = newProgress

            if(!trackInitialized){
                trace.putAttribute("url", view?.url ?: "")
                trace.start()
                trackInitialized = true
            }

            if(newProgress > 90){
                progressBar?.visibility = View.GONE
                trace.stop()
                view?.stopLoading()
            }else{
                progressBar?.visibility = View.VISIBLE
            }
        }
    }

    webView.webViewClient = client
}

fun initSpinner(
    context: Context?,
    spinner: Spinner,
    data: Array<String>,
    itemSelectedListener: AdapterView.OnItemSelectedListener?
){
    if (context != null) {
        val data2 = Array(data.size){
            data[it]
        }
        val adapter : ArrayAdapter<String> = ArrayAdapter(
            context,
            android.R.layout.simple_dropdown_item_1line,
            data2
        )
        adapter.setDropDownViewResource(ziox.ramiro.saes.R.layout.view_spinner_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = itemSelectedListener
    }
}

@Suppress("unused")
fun WebView.showIn(view: ViewGroup) = view.addView(this, view.width, dpToPixel(this.context, 500))

fun isDarkTheme(context: Context?) : Boolean{
    if(context == null) return false
    return context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

@Suppress("DEPRECATION")
fun setSystemUiLightStatusBar(activity: Activity, isLightStatusBar: Boolean) {
    val window = activity.window
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val systemUiAppearance = if (isLightStatusBar) {
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            } else {
                0
            }
            window.insetsController?.setSystemBarsAppearance(systemUiAppearance,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else {
            val systemUiVisibilityFlags = if (isLightStatusBar) {
                window.decorView.systemUiVisibility or SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                window.decorView.systemUiVisibility and SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            window.decorView.systemUiVisibility = systemUiVisibilityFlags
        }
    }
}

fun setStatusBarByTheme(activity: Activity){
    if(isDarkTheme(activity)){
        setSystemUiLightStatusBar(activity, false)
    }else{
        setSystemUiLightStatusBar(activity, true)
    }
}

fun initTheme(activity: Activity){
    try {
        AppCompatDelegate.setDefaultNightMode(
            getPreference(
                activity,
                "dark_mode",
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                } else {
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                }
            )
        )

        setStatusBarByTheme(activity)
    } catch (e: Exception) {
        Log.e("ViewUtils", e.toString())
    }
}

fun View.addBottomInsetPadding(onComplete: () -> Unit = {}){
    val paddingBottom = this.paddingBottom

    if(EDGE_INSET_BOTTOM >= 0){
        this.updatePadding(bottom = paddingBottom + EDGE_INSET_BOTTOM)
        onComplete()
    }else{
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
            EDGE_INSET_TOP = windowInsets.systemWindowInsetTop
            EDGE_INSET_BOTTOM = windowInsets.systemWindowInsetBottom
            this.updatePadding(bottom = paddingBottom + EDGE_INSET_BOTTOM)
            onComplete()
            windowInsets
        }
    }
}

fun View.addTopInsetPadding(onComplete: () -> Unit = {}){
    val paddingTop = this.paddingTop

    if(EDGE_INSET_TOP >= 0){
        this.updatePadding(top = paddingTop + EDGE_INSET_TOP)
        onComplete()
    }else{
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
            EDGE_INSET_TOP = windowInsets.systemWindowInsetTop
            EDGE_INSET_BOTTOM = windowInsets.systemWindowInsetBottom
            this.updatePadding(top = paddingTop + EDGE_INSET_TOP)
            onComplete()
            windowInsets
        }
    }
}

fun TextView.setTextInPercentageChange(original: Number, change: Number){
    val difference = change.toDouble() - original.toDouble()

    text = when {
        difference > 0 -> {
            setTextColor(ContextCompat.getColor(context, ziox.ramiro.saes.R.color.colorSuccess))
            "+${(100*difference.absoluteValue/original.toDouble()).toStringPrecision(1)}%"
        }
        difference < 0 -> {
            setTextColor(ContextCompat.getColor(context, ziox.ramiro.saes.R.color.colorDanger))
            "-${(100*difference.absoluteValue/original.toDouble()).toStringPrecision(1)}%"
        }
        else -> "0.0%"
    }
}