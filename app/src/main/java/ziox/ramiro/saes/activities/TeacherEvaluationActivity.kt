package ziox.ramiro.saes.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.util.Base64.NO_WRAP
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ziox.ramiro.saes.databinding.ActivityTeacherEvaluationBinding
import ziox.ramiro.saes.utils.addBottomInsetPadding
import ziox.ramiro.saes.utils.initTheme
import ziox.ramiro.saes.utils.initWebView
import ziox.ramiro.saes.utils.setSystemUiLightStatusBar


/**
 * Creado por Ramiro el 12/10/2018 a las 1:52 PM para SAES.
 */
class TeacherEvaluationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTeacherEvaluationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherEvaluationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initTheme(this)
        setSystemUiLightStatusBar(this, false)
        binding.contentWebView.addBottomInsetPadding()

        val name = intent?.extras?.getString("nombre")
        val url = intent?.extras?.getString("url")!!

        binding.toolbar.title = name
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        initWebView(binding.contentWebView, object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.sendingProgressBar.visibility = View.VISIBLE
                binding.contentWebView.visibility = View.INVISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.sendingProgressBar.visibility = View.GONE
                injectCSS()
                binding.contentWebView.visibility = View.VISIBLE
                view?.loadUrl(
                    "javascript:" +
                            "document.getElementsByTagName(\"head\")[0].innerHtml = document.getElementsByTagName(\"head\")[0].innerHtml+\"<meta name=\'viewport\' content=\'width=device-width, initial-scale=1.0\'/>\";" +
                            "if(document.getElementById(\"ctl00_mainCopy_Aceptar\") == null){" +
                            "   window.JSI.onFinishTest();" +
                            "}"
                )
            }
        }, null)

        binding.contentWebView.addJavascriptInterface(JSI(), "JSI")

        binding.contentWebView.loadUrl(url)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            this.finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun injectCSS() {
        try {
            val inputStream = assets.open("ProfEvalStyles.css")
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            val encoded = Base64.encodeToString(buffer, NO_WRAP)
            binding.contentWebView.loadUrl(
                "javascript:(function() {" +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var style = document.createElement('style');" +
                        "style.type = 'text/css';" +
                        "style.innerHTML = window.atob('" + encoded + "');" +
                        "parent.appendChild(style)" +
                        "})()"
            )
        } catch (e: Exception) {
            Log.e(this.javaClass.canonicalName, e.toString())
        }

    }

    inner class JSI {
        @JavascriptInterface
        fun onFinishTest() {
            runOnUiThread {
                Toast.makeText(
                    this@TeacherEvaluationActivity,
                    "¡Evaluacion correctamente enviada!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            this@TeacherEvaluationActivity.finish()
        }
    }
}