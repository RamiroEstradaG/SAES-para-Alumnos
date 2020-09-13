package ziox.ramiro.saes.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.firebase.perf.FirebasePerformance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ziox.ramiro.saes.R
import ziox.ramiro.saes.fragments.SelectSchoolNivelMedioSuperiorFragment
import java.security.MessageDigest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.min


/**
 * Creado por Ramiro el 10/12/2018 a las 7:31 PM para SAESv2.
 */

val MES = arrayOf(
    "ENE",
    "FEB",
    "MAR",
    "ABR",
    "MAY",
    "JUN",
    "JUL",
    "AGO",
    "SEP",
    "OCT",
    "NOV",
    "DIC"
)

val MES_COMPLETO = arrayOf(
    "Enero",
    "Febrero",
    "Marzo",
    "Abril",
    "Mayo",
    "Junio",
    "Julio",
    "Agosto",
    "Septiembre",
    "Octubre",
    "Noviembre",
    "Diciembre"
)

var EDGE_INSET_BOTTOM : Int = -1

enum class ValType{
    INT,
    STRING,
    FLOAT,
    BOOLEAN,
    LONG
}
fun <T>setPreference(context: Context?, key : String, value : T){
    val pref = context?.getSharedPreferences("preferences", Context.MODE_PRIVATE)?.edit()
    when(value){
        is Int -> pref?.putInt(key, value)
        is Long -> pref?.putLong(key, value)
        is Float -> pref?.putFloat(key, value)
        is String -> pref?.putString(key, value)
        is Boolean -> pref?.putBoolean(key, value)
    }
    pref?.apply()
}

fun <K, V>Map<K, V>.getKeyOfValue(value: Any, default: K) : K{
    for(m in this){
        if(m.value == value){
            return m.key
        }
    }

    return default
}

fun getPreference(context: Context?, type: ValType, key : String) : Any?{
    val pref = context?.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    return when(type){
        ValType.INT -> pref?.getInt(key, 0)
        ValType.LONG -> pref?.getLong(key, 0)
        ValType.FLOAT -> pref?.getFloat(key, 0f)
        ValType.STRING -> pref?.getString(key, "")
        ValType.BOOLEAN -> pref?.getBoolean(key, false)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T>getPreference(context: Context?, key : String, default: T) : T{
    val pref = context?.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    return when (default) {
        is Int -> pref?.getInt(key, default) as? T
        is Long -> pref?.getLong(key, default) as? T
        is Float -> pref?.getFloat(key, default) as? T
        is String -> pref?.getString(key, default) as? T
        is Boolean -> pref?.getBoolean(key, default) as? T
        else -> null
    } ?:default
}

fun Double.toHour(): String{
    return "${this.toInt().toString().padStart(2, '0')}:${((this%1.0)*60).toInt().toString().padStart(2, '0')}"
}

fun Double.decimal() = this%1

fun hourToDouble(hora: String) : Double{
    val horaValue = Regex("[0-9]+:[0-9]+").find(hora)?.value?.split(":") ?: return -1.0

    return horaValue[0].toDouble()+(horaValue[1].toDouble()/60.0)
}

fun removePreference(context: Context?, key: String){
    context?.getSharedPreferences("preferences", Context.MODE_PRIVATE)?.edit()?.remove(key)?.apply()
}

fun createWebView(context: Context?, client : WebViewClient, progressBar: ProgressBar?) : WebView {
    val webView = WebView(context!!)
    initWebView(webView, client, progressBar)

    return webView
}

fun dpToPixel(context: Context?, dp : Int) = context?.resources?.displayMetrics?.density!!.times(dp).toInt()

@SuppressLint("SetJavaScriptEnabled")
fun initWebView(webView: WebView, client : WebViewClient, progressBar: ProgressBar?){
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

fun mesToInt(mes: String): Int{
    for((i, v) in MES.withIndex()){
        if(v == mes.toUpperCase(Locale.ROOT)) return i
    }
    return 0
}

fun initSpinner(context: Context?, spinner: Spinner, data: Array<String>, itemSelectedListener: AdapterView.OnItemSelectedListener?){
    if (context != null) {
        val data2 = Array(data.size){
            data[it]
        }
        val adapter : ArrayAdapter<String> = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, data2)
        adapter.setDropDownViewResource(ziox.ramiro.saes.R.layout.view_spinner_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = itemSelectedListener
    }
}

fun dividirHoras(hora: String) : Pair<Double, Double>?{
    val horas = Regex("[0-9]+:[0-9]+-[0-9]+:[0-9]+").find(hora.replace(" ", ""))?.value?.split("-")

    return if(horas?.size == 2){
        val hora1 = horas[0].split(":")
        val hora2 = horas[1].split(":")
        Pair(hora1[0].toDouble()+(hora1[1].toDouble()/60.0), hora2[0].toDouble()+(hora2[1].toDouble()/60.0))
    }else{
        null
    }
}

fun String.toDateString() : String{
    val str2 = this.replace(". ","")

    val format = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a.", Locale.US)

    try {
        val date = format.parse(str2)
        try {
            val strFormat = SimpleDateFormat("EEEE, d 'de' MMMM 'del' yyyy 'a las' HH:mm", Locale.US)
            return strFormat.format(date?: Date())
        } catch (e: Exception) {

        }
    } catch (e: Exception) {

    }
    return ""
}

fun String.toDate() : Date {
    val str2 = this.replace(". ","")
    val format = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a.", Locale.US)

    try {
        return format.parse(str2)?: Date()
    } catch (e: Exception) {

    }
    return Date()
}



fun String.toProperCase(): String {
    val str = this.toLowerCase(Locale.ROOT).split(" ").filter {
        it.isNotEmpty()
    }

    return if (str.isEmpty()) {
        ""
    } else {
        var res = ""
        for ((i, arr) in str.withIndex()) {
            res += if (i == str.lastIndex) {
                if (arr.matches(Regex("[ivx]+"))) {
                    arr.toUpperCase(Locale.ROOT)
                } else {
                    arr.capitalize()
                }
            } else if (arr.matches(Regex("de|del|y|o|e|por|a|u|para|las|los|la|el|en"))) {
                "$arr "
            } else {
                "${arr.capitalize()} "
            }
        }
        res
    }
}

fun String.getInitials(): String {
    var siglas = ""
    val rem = this.toUpperCase(Locale.ROOT).replace(Regex("( )(((DE|DEL|Y|O|E|A|U|POR|PARA|LAS|LOS|LA|EL|EN) (DE|DEL|Y|O|E|A|U|POR|PARA|LAS|LOS|LA|EL|EN))|DE|DEL|Y|O|E|A|U|POR|PARA|LAS|LOS|LA|EL|EN)( )"),
        " ").replace(Regex(" \\([^)]*\\)"), "")

    val str = rem.split(" ").filter {
        it.isNotEmpty()
    }

    if(str.isEmpty()) return ""

    if (str.size == 1) {
        siglas = rem.substring(0, min(this.length, 4))
    } else {
        if (str.last().matches(Regex("[IVX]+")) && str.size == 2) {
            siglas = str.first().substring(0, min(this.length, 3)) + " " + str.last().toUpperCase(Locale.ROOT)
        } else {
            for ((i, arr) in str.withIndex()) {
                siglas += if (i == str.lastIndex) {
                    if (arr.matches(Regex("[IVX]+"))) {
                        " ${arr.toUpperCase(Locale.ROOT)}"
                    } else {
                        arr.first()
                    }
                } else {
                    arr.first()
                }
            }
        }
    }

    return siglas
}

@Suppress("DEPRECATION")
fun Context.isNetworkAvailable() : Boolean{
    if(getPreference(this, "offline_mode", false)){
        return false
    }

    val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            }
        }
    } else {
        try {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        } catch (e: Exception) {}
    }

    return false
}

@Suppress("unused")
fun WebView.showIn(view : ViewGroup) = view.addView(this, view.width, dpToPixel(this.context, 500))

fun downloadFile(context: Context?, name : String){
    context?.startActivity(
        Intent(Intent.ACTION_VIEW)
            .setData(
                Uri.parse(
                    getUrl(context)+"PDF/Alumnos/Reinscripciones/${getBoleta(context)}-$name.pdf"
                )
            )
    )
}

fun getBoleta(context: Context?) = getPreference(context, "boleta", "")

fun getUrl(context: Context?) : String = getPreference(context, ValType.STRING, "new_url_escuela") as String? ?: ""

fun getNameEscuela(context: Context?) = getPreference(
    context,
    "name_escuela",
    "Instituto Politecnico Nacional"
)

fun isDarkTheme(context: Context?) : Boolean{
    if(context == null) return false
    return context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

fun setLightStatusBar(context: Context?){
    if(context is AppCompatActivity){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            context.window.decorView.systemUiVisibility = 0
        }
        context.window.statusBarColor = Color.TRANSPARENT
    }
}

fun setDarkStatusBar(context: Context?){
    if(context is AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}

@Suppress("unused")
fun pixelToDp(px : Float, context: Context?) : Float{
    return px / (context?.resources?.displayMetrics?.density ?: 1f)
}

fun View.addBottomInsetPadding(onComplete : () -> Unit = {}){
    val paddingBottom = this.paddingBottom

    if(EDGE_INSET_BOTTOM >= 0){
        this.updatePadding(bottom = paddingBottom + EDGE_INSET_BOTTOM)
        onComplete()
    }else{
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
            EDGE_INSET_BOTTOM = windowInsets.systemWindowInsetBottom
            this.updatePadding(bottom = paddingBottom + EDGE_INSET_BOTTOM)
            onComplete()
            windowInsets
        }
    }
}

fun setStatusBarByTheme(context: Context?){
    if(isDarkTheme(context)){
        setLightStatusBar(context)
    }else{
        setDarkStatusBar(context)
    }
}

fun initTheme(context: Context?){
    try {
        AppCompatDelegate.setDefaultNightMode(
            getPreference(
                context,
                "dark_mode",
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                } else {
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                }
            )
        )

        setStatusBarByTheme(context)
    } catch (e: Exception) {
        Log.e("error", e.toString())
    }
}

fun Context.haveDonated() : Boolean {
    val bp = BillingProcessor(
        this,
        this.resources.getString(R.string.billingKey),
        object : BillingProcessor.IBillingHandler{
            override fun onBillingInitialized() {}
            override fun onPurchaseHistoryRestored() {}
            override fun onProductPurchased(productId: String, details: TransactionDetails?) {}
            override fun onBillingError(errorCode: Int, error: Throwable?) {}
        }
    )


    return bp.listOwnedProducts().isNotEmpty()
}

fun Double.toStringPresition(digits : Int) : String{
    val numberFormat = NumberFormat.getNumberInstance()
    numberFormat.maximumFractionDigits = digits
    return numberFormat.format(this)
}

fun Array<out Any?>.mean() : Double{
    val tmp = this.filterNotNull()
    if(this.isEmpty()) return 0.0

    if(this.first() !is Number) throw NumberFormatException()
    var mean = 0.0

    for(n in tmp){
        mean += (n as Number).toDouble()
    }

    return mean.div(tmp.size)
}

fun getCarrera(context: Context?) = getPreference(context, "carrera", "Sin definir")

fun isShareStatsEnable(context: Context?) = getPreference(context, "ShareStatsEnable", false)
fun setShareStatsEnable(context: Context?, value : Boolean) = setPreference(context, "ShareStatsEnable", value)

@Suppress("unused")
object HashUtils {
    private const val HEX_CHARS = "0123456789abcdef"

    fun sha512(input: String) = hashString("SHA-512", input)

    fun sha256(input: String) = hashString("SHA-256", input)

    fun sha1(input: String) = hashString("SHA-1", input)

    private fun hashString(type: String, input: String): String {
        val bytes = MessageDigest
            .getInstance(type)
            .digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }

        return result.toString()
    }
}

fun generateRandomString(size: Int) : String{
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
    val radom = Random(Calendar.getInstance().timeInMillis)
    var res = ""
    for (i in 0 until size){
        res += chars[radom.nextInt().absoluteValue%chars.length]
    }
    return res
}

fun Date.toCalendar() : Calendar{
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar
}

fun getHashUserId(context: Context?) = HashUtils.sha256(getBoleta(context)+getNameEscuela(context))

fun getBasicUser(context: Context?) = User(
    getNameEscuela(context),
    getCarrera(context).toProperCase(),
    SelectSchoolNivelMedioSuperiorFragment.medioSuperiorMap.containsKey(getCarrera(context))
)

fun Calendar.format() : String = SimpleDateFormat("EEEE, d 'de' MMMM'\n'hh:mm a", Locale("es", "MX")).format(this.time)

fun <T>List<T>.joinToSentence() : String{
    return if (this.size > 1){
        "${this.subList(0, this.lastIndex).joinToString(", ")} y ${this.last()}"
    }else{
        this.joinToString(", ")
    }
}

fun String.isUrl() : Boolean{
    return this.matches(Regex("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)"))
}