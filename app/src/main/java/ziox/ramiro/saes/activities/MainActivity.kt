package ziox.ramiro.saes.activities

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_select_school.*
import kotlinx.android.synthetic.main.fragment_loading.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.fragments.SelectSchoolNivelMedioSuperiorFragment
import ziox.ramiro.saes.fragments.SelectSchoolNivelSuperiorFragment
import ziox.ramiro.saes.utils.*
import java.util.*

/**
 * Creado por Ramiro el 10/12/2018 a las 4:04 PM para SAESv2.
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var loginWebView: WebView
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private var logsIntento = 1

    @AddTrace(name = "onCreateMain", enabled = true)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initTheme(this)

        offlineButton.setOnClickListener(this)
        elegirEscuelaDrag.setOnClickListener(this)
        loginLoginBtn.setOnClickListener(this)
        elegirCarreraClose.setOnClickListener(this)

        handleIntent()
        Notification.scheduleRepeatingRTCNotification(this)
        enablePersistance(true)
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
        setPreference(this, "offline_mode", false)
        MobileAds.initialize(this)
        initForm()
        initBottomSheet()

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        loginWebView = createWebView(this, SSLWebViewClient(this), null)
        loginWebView.addJavascriptInterface(JSInterface(), "JSI")

        elegirEscuelaDrag.addBottomInsetPadding{
            sheetBehavior.peekHeight = EDGE_INSET_BOTTOM + dpToPixel(this@MainActivity, 40)
        }

        if(!this.haveDonated()){
            adView.loadAd(AdRequest.Builder().build())
        }else{
            adView.visibility = View.GONE
        }

        if (!this.isNetworkAvailable()) {
            offlineLogin()
            offlineImage.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }

        if (getUrl(this) == "") {
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            elegirEscuelaDrag.visibility = View.GONE
        } else {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            loadEscuela()
        }
    }

    private fun initForm(){
        loginBoleta.editText?.text = Editable.Factory().newEditable(getBoleta(this))
        loginPass.editText?.text = Editable.Factory().newEditable(getPreference(this, "pass", ""))
        loginRecordarPass.isChecked = getPreference(this, "recordar_contrasena", false)
    }

    private fun initBottomSheet(){
        sheetBehavior = BottomSheetBehavior.from(elegirEscuelaSheet)
        sheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            override fun onSlide(p0: View, p1: Float) {}

            override fun onStateChanged(p0: View, p1: Int) {
                when (p1) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        elegirEscuelaDrag.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        sheetBehavior.peekHeight = EDGE_INSET_BOTTOM + dpToPixel(this@MainActivity, 40)
                        elegirEscuelaDrag.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        sheetBehavior.peekHeight = 0
                        elegirEscuelaDrag.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {

                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {

                    }
                    BottomSheetBehavior.STATE_SETTLING -> {

                    }
                }
            }
        })
        selectSchoolToolbar.title = "Selecciona tu escuela"
        selectSchoolTabLayout.setupWithViewPager(selectSchoolViewPager)
        selectSchoolViewPager.adapter = Adapter(supportFragmentManager)
    }

    private fun handleIntent(){
        if (intent.action == Intent.ACTION_VIEW) {
            if (intent.data?.path != "www.saes.ipn.mx") {
                val url = "${intent.data?.scheme}://${intent.data?.host}/"
                setPreference(this, "new_url_escuela", url)
                if (SelectSchoolNivelMedioSuperiorFragment.medioSuperiorMap.containsValue(url)) {
                    setPreference(
                        this,
                        "name_escuela",
                        SelectSchoolNivelMedioSuperiorFragment.medioSuperiorMap.getKeyOfValue(
                            url,
                            ""
                        )
                    )
                } else if (SelectSchoolNivelSuperiorFragment.superiorMap.containsValue(url)) {
                    setPreference(
                        this,
                        "name_escuela",
                        SelectSchoolNivelSuperiorFragment.superiorMap.getKeyOfValue(url, "")
                    )
                }
            }
        }
        if(intent.hasExtra(SAESActivity.INTENT_EXTRA_REDIRECT)){
            if(intent.getStringExtra(SAESActivity.INTENT_EXTRA_REDIRECT)!!.isUrl()){
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(intent.getStringExtra(SAESActivity.INTENT_EXTRA_REDIRECT))
                    )
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
    }

    private fun registerEvent(
        itemId: String,
        itemName: String,
        contentType: String,
        event: String
    ) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
        firebaseAnalytics.logEvent(event, bundle)
    }

    fun hideLoading() {
        loadingFragment.visibility = View.GONE
    }

    private fun validarInputs(): Boolean {
        loginBoleta.error = null
        loginPass.error = null
        loginCaptcha.error = null

        var hasError = false

        if (loginBoleta.editText?.text!!.isEmpty()) {
            hasError = true
            loginBoleta.error = "Este campo está vacío"
        }

        if (loginPass.editText?.text!!.isEmpty()) {
            hasError = true
            loginPass.error = "Este campo está vacío"
        }

        if (loginCaptcha.editText?.text!!.isEmpty()) {
            hasError = true
            loginCaptcha.error = "Este campo está vacío"
        }

        return !hasError
    }

    fun onEscuelaSelected() {
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        loadEscuela()
    }

    private fun loadEscuela() {
        webViewCaptcha.visibility = View.GONE

        try {
            val res = resources.getIdentifier(
                getPreference(
                    this,
                    "name_escuela",
                    "IPN"
                ).toLowerCase(Locale.ROOT).replace(" ", ""), "drawable", packageName
            )
            loginLogo.setImageResource(
                if (res > 0) {
                    res
                } else {
                    R.drawable.ic_logopoli
                }
            )
        } catch (e: Exception) {

        }

        loginWebView.loadUrl(getUrl(this))
    }

    inner class JSInterface {
        @JavascriptInterface
        fun onLoginStatusChanged(isLogged: Boolean, captchaSrc: String?) {
            if (isLogged) {
                setPreference(this@MainActivity, "recordar_contrasena", loginRecordarPass.isChecked)
                setPreference(this@MainActivity, "boleta", loginBoleta.editText?.text!!.toString())
                if (loginRecordarPass.isChecked) {
                    setPreference(this@MainActivity, "pass", loginPass.editText?.text!!.toString())
                }
                val intentLogged = Intent(this@MainActivity, SAESActivity::class.java)

                if(intent.hasExtra(SAESActivity.INTENT_EXTRA_REDIRECT)){
                    intentLogged.putExtra(SAESActivity.INTENT_EXTRA_REDIRECT, intent.getStringExtra(SAESActivity.INTENT_EXTRA_REDIRECT))
                }
                startActivity(intentLogged)
                this@MainActivity.finish()
            } else {
                runOnUiThread {
                    sheetBehavior.peekHeight = EDGE_INSET_BOTTOM + dpToPixel(this@MainActivity, 40)
                    elegirEscuelaDrag.visibility = View.VISIBLE
                    hideLoading()
                    if (captchaSrc != null) {
                        webViewCaptcha.loadUrl(captchaSrc)
                    }
                    webViewCaptcha.visibility = View.VISIBLE
                }
            }
        }

        @JavascriptInterface
        fun error(url: String) {
            runOnUiThread {
                Snackbar.make(loginParent, "Error: $url", Snackbar.LENGTH_SHORT).show()
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        @JavascriptInterface
        fun onLoginFailed(msg : String){
            runOnUiThread {
                if(msg.contains("captcha", true)){
                    loginCaptcha.error = msg
                }else{
                    loginPass.error = msg
                }

                loginCaptcha.editText?.text?.clear()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        crashlytics.log("Configuracion cambiada en ${this.localClassName}:\n$newConfig")
    }

    class Adapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> SelectSchoolNivelSuperiorFragment()
                1 -> SelectSchoolNivelMedioSuperiorFragment()
                else -> Fragment()
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> "Superior"
                1 -> "Medio Superior"
                else -> ""
            }
        }

        override fun getCount(): Int {
            return 2
        }

    }

    override fun onClick(button: View) {
        crashlytics.log("Click en ${resources.getResourceName(offlineButton.id)} en la clase ${this.localClassName}")
        when(button.id){
            offlineButton.id -> offlineLogin()
            elegirEscuelaDrag.id -> sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            elegirCarreraClose.id -> sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            loginLoginBtn.id -> login()
        }
    }

    private fun offlineLogin(){
        if (getPreference(this, "offline_switch", false)) {
            setPreference(this, "offline_mode", true)
            registerEvent(
                offlineButton.id.toString(),
                "button offline",
                "button",
                FirebaseAnalytics.Event.SELECT_CONTENT
            )
            val intentLogged = Intent(this@MainActivity, SAESActivity::class.java)

            if(intent.hasExtra(SAESActivity.INTENT_EXTRA_REDIRECT)){
                intentLogged.putExtra(SAESActivity.INTENT_EXTRA_REDIRECT, intent.getStringExtra(SAESActivity.INTENT_EXTRA_REDIRECT))
            }
            startActivity(intentLogged)
            this@MainActivity.finish()
        } else {
            Snackbar.make(
                loginParent,
                "Activa el modo offline en las configuraciones para acceder sin internet.",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun login(){
        if (validarInputs()) {
            if (logsIntento++ % 4 > 0) {
                webViewCaptcha.visibility = View.GONE
                loginWebView.loadUrl(
                    "javascript: document.getElementById(\"ctl00_leftColumn_LoginUser_UserName\").value = \"${loginBoleta.editText?.text.toString()}\";" +
                            "document.getElementById(\"ctl00_leftColumn_LoginUser_Password\").value = \"${loginPass.editText?.text.toString().replace(
                                Regex("[\"\\\\]")
                            ) { matchResult ->
                                "\\${matchResult.value}"
                            }}\";" +
                            "document.getElementById(\"ctl00_leftColumn_LoginUser_CaptchaCodeTextBox\").value = \"${loginCaptcha.editText?.text.toString()}\";" +
                            "document.getElementById(\"ctl00_leftColumn_LoginUser_LoginButton\").click();"
                )
            } else {
                val dialog = AlertDialog.Builder(this, R.style.DialogAlert)
                dialog.setTitle("Cuidado")
                dialog.setMessage(
                    "Tienes varios intentos fallidos. Es posible que la aplicación no pueda comunicarse correctamente con el SAES o tus datos sean incorrectos." +
                            " Si continuas es posible que tu cuenta sea suspendida."
                )
                dialog.setPositiveButton("Entendido", null)
                dialog.show()
            }
        }
    }
}