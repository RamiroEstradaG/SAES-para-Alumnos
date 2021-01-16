package ziox.ramiro.saes.activities

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
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
import com.google.firebase.perf.metrics.AddTrace
import ziox.ramiro.saes.R
import ziox.ramiro.saes.databases.enablePersistence
import ziox.ramiro.saes.databinding.ActivityMainBinding
import ziox.ramiro.saes.fragments.SelectHighSchoolFragment
import ziox.ramiro.saes.fragments.SelectUniversityFragment
import ziox.ramiro.saes.utils.*
import java.util.*

/**
 * Creado por Ramiro el 10/12/2018 a las 4:04 PM para SAESv2.
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var loginWebView: WebView
    private lateinit var binding : ActivityMainBinding
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private var loginAttemptCount = 1

    @AddTrace(name = "onCreateMain", enabled = true)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initTheme(this)

        binding.offlineButton.setOnClickListener(this)
        binding.selectSchoolDragger.setOnClickListener(this)
        binding.loginButton.setOnClickListener(this)
        binding.bottomSheetContainer.closeButton.setOnClickListener(this)
        binding.aboutButton.setOnClickListener(this)

        handleIntent()
        Notification.scheduleRepeatingRTCNotification(this)
        enablePersistence(true)
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
        setPreference(this, "offline_mode", false)
        MobileAds.initialize(this)
        initForm()
        initBottomSheet()

        loginWebView = createWebView(this, SSLWebViewClient(this), null)
        loginWebView.addJavascriptInterface(JSInterface(), "JSI")

        binding.selectSchoolDragger.addBottomInsetPadding{
            bottomSheetBehavior.peekHeight = EDGE_INSET_BOTTOM + dpToPixel(this@MainActivity, 40)
        }

        if(!this.haveDonated()){
            binding.adView.loadAd(AdRequest.Builder().build())
        }else{
            binding.adView.visibility = View.GONE
        }

        if (!this.isNetworkAvailable()) {
            loginInOfflineMode()
            binding.loadingFragment.offlineImage.visibility = View.VISIBLE
            binding.loadingFragment.progressBar.visibility = View.GONE
        }

        if (getUrl(this) == "") {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.selectSchoolDragger.visibility = View.GONE
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            loadSchool()
        }
    }

    private fun initForm(){
        binding.loginBoleta.editText?.text = Editable.Factory().newEditable(getBoleta(this))
        binding.loginPass.editText?.text = Editable.Factory().newEditable(getPreference(this, "pass", ""))
        binding.loginRecordarPass.isChecked = getPreference(this, "recordar_contrasena", false)
    }

    private fun initBottomSheet(){
        bottomSheetBehavior = BottomSheetBehavior.from(binding.selectSchoolBottomSheet)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            override fun onSlide(p0: View, p1: Float) {}

            override fun onStateChanged(p0: View, p1: Int) {
                when (p1) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.selectSchoolDragger.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        bottomSheetBehavior.peekHeight = EDGE_INSET_BOTTOM + dpToPixel(this@MainActivity, 40)
                        binding.selectSchoolDragger.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        bottomSheetBehavior.peekHeight = 0
                        binding.selectSchoolDragger.visibility = View.GONE
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
        binding.bottomSheetContainer.selectSchoolToolbar.title = "Selecciona tu escuela"
        binding.bottomSheetContainer.selectSchoolTabLayout.setupWithViewPager(binding.bottomSheetContainer.selectSchoolViewPager)
        binding.bottomSheetContainer.selectSchoolViewPager.adapter = Adapter(supportFragmentManager)
    }

    private fun handleIntent(){
        if (intent.action == Intent.ACTION_VIEW) {
            if (intent.data?.path != "www.saes.ipn.mx") {
                val url = "${intent.data?.scheme}://${intent.data?.host}/"
                setPreference(this, "new_url_escuela", url)
                if (SelectHighSchoolFragment.highSchoolMap.containsValue(url)) {
                    setPreference(
                        this,
                        "name_escuela",
                        SelectHighSchoolFragment.highSchoolMap.getKeyOfValue(
                            url,
                            ""
                        )
                    )
                } else if (SelectUniversityFragment.superiorMap.containsValue(url)) {
                    setPreference(
                        this,
                        "name_escuela",
                        SelectUniversityFragment.superiorMap.getKeyOfValue(url, "")
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

    fun hideLoading() {
        binding.loadingFragment.root.visibility = View.GONE
    }

    private fun validate(): Boolean {
        binding.loginBoleta.error = null
        binding.loginPass.error = null
        binding.loginCaptcha.error = null

        var hasError = false

        if (binding.loginBoleta.editText?.text!!.isEmpty()) {
            hasError = true
            binding.loginBoleta.error = "Este campo está vacío"
        }

        if (binding.loginPass.editText?.text!!.isEmpty()) {
            hasError = true
            binding.loginPass.error = "Este campo está vacío"
        }

        if (binding.loginCaptcha.editText?.text!!.isEmpty()) {
            hasError = true
            binding.loginCaptcha.error = "Este campo está vacío"
        }

        return !hasError
    }

    fun onSchoolSelected() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        loadSchool()
    }

    private fun loadSchool() {
        binding.captchaDisplayer.visibility = View.GONE

        try {
            val res = resources.getIdentifier(
                getPreference(
                    this,
                    "name_escuela",
                    "IPN"
                ).toLowerCase(Locale.ROOT).replace(" ", ""), "drawable", packageName
            )
            binding.loginLogo.setImageResource(
                if (res > 0) {
                    res
                } else {
                    R.drawable.ic_logopoli
                }
            )
        } catch (e: Exception) {
            Log.e(this.javaClass.canonicalName, e.toString())
        }

        loginWebView.loadUrl(getUrl(this))
    }

    inner class JSInterface {
        @JavascriptInterface
        fun onLoginStatusChanged(isLogged: Boolean, captchaSrc: String?) {
            if (isLogged) {
                setPreference(this@MainActivity, "recordar_contrasena", binding.loginRecordarPass.isChecked)
                setPreference(this@MainActivity, "boleta", binding.loginBoleta.editText?.text!!.toString())
                if (binding.loginRecordarPass.isChecked) {
                    setPreference(this@MainActivity, "pass", binding.loginPass.editText?.text!!.toString())
                }
                val intentLogged = Intent(this@MainActivity, SAESActivity::class.java)

                if(intent.hasExtra(SAESActivity.INTENT_EXTRA_REDIRECT)){
                    intentLogged.putExtra(SAESActivity.INTENT_EXTRA_REDIRECT, intent.getStringExtra(SAESActivity.INTENT_EXTRA_REDIRECT))
                }
                startActivity(intentLogged)
                this@MainActivity.finish()
            } else {
                runOnUiThread {
                    bottomSheetBehavior.peekHeight = EDGE_INSET_BOTTOM + dpToPixel(this@MainActivity, 40)
                    binding.selectSchoolDragger.visibility = View.VISIBLE
                    hideLoading()
                    if (captchaSrc != null) {
                        binding.captchaDisplayer.loadUrl(captchaSrc)
                    }
                    binding.captchaDisplayer.visibility = View.VISIBLE
                }
            }
        }

        @JavascriptInterface
        fun error(url: String) {
            runOnUiThread {
                Snackbar.make(binding.loginParent, "Error: $url", Snackbar.LENGTH_SHORT).show()
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        @JavascriptInterface
        fun onLoginFailed(msg : String){

            runOnUiThread {
                if(msg.contains("captcha", true)){
                    binding.loginCaptcha.error = msg
                }else{
                    binding.loginPass.error = msg
                }

                binding.loginCaptcha.editText?.text?.clear()
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
                0 -> SelectUniversityFragment()
                1 -> SelectHighSchoolFragment()
                else -> Fragment()
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
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
        crashlytics.log("Click en ${resources.getResourceName(binding.offlineButton.id)} en la clase ${this.localClassName}")
        when(button.id){
            binding.offlineButton.id -> loginInOfflineMode()
            binding.selectSchoolDragger.id -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.bottomSheetContainer.closeButton.id -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            binding.loginButton.id -> login()
            binding.aboutButton.id -> startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    private fun loginInOfflineMode(){
        if (getPreference(this, "offline_switch", false)) {
            setPreference(this, "offline_mode", true)
            val intentLogged = Intent(this@MainActivity, SAESActivity::class.java)

            if(intent.hasExtra(SAESActivity.INTENT_EXTRA_REDIRECT)){
                intentLogged.putExtra(SAESActivity.INTENT_EXTRA_REDIRECT, intent.getStringExtra(SAESActivity.INTENT_EXTRA_REDIRECT))
            }
            startActivity(intentLogged)
            this@MainActivity.finish()
        } else {
            Snackbar.make(
                binding.loginParent,
                "Activa el modo offline en las configuraciones para acceder sin internet.",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun login(){
        if (validate()) {
            if (loginAttemptCount++ % 4 > 0) {
                binding.captchaDisplayer.visibility = View.GONE
                loginWebView.loadUrl(
                    "javascript: document.getElementById(\"ctl00_leftColumn_LoginUser_UserName\").value = \"${binding.loginBoleta.editText?.text.toString()}\";" +
                            "document.getElementById(\"ctl00_leftColumn_LoginUser_Password\").value = \"${binding.loginPass.editText?.text.toString().replace(
                                Regex("[\"\\\\]")
                            ) { matchResult ->
                                "\\${matchResult.value}"
                            }}\";" +
                            "document.getElementById(\"ctl00_leftColumn_LoginUser_CaptchaCodeTextBox\").value = \"${binding.loginCaptcha.editText?.text.toString()}\";" +
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