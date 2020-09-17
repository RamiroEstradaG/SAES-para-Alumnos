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
import kotlinx.android.synthetic.main.activity_evaluacion_profesor.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.utils.initTheme
import ziox.ramiro.saes.utils.initWebView
import ziox.ramiro.saes.utils.addBottomInsetPadding
import ziox.ramiro.saes.utils.setLightStatusBar


/**
 * Creado por Ramiro el 12/10/2018 a las 1:52 PM para SAES.
 */
class EvaluarProfesorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluacion_profesor)
        initTheme(this)
        setLightStatusBar(this)
        evaluacionWebView.addBottomInsetPadding()

        val nombre = intent?.extras?.getString("nombre")
        val url = intent?.extras?.getString("url")!!

        evaluacionToolbar.title = nombre
        setSupportActionBar(evaluacionToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        initWebView(evaluacionWebView, object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                sendingProgress.visibility = View.VISIBLE
                evaluacionWebView.visibility = View.INVISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                sendingProgress.visibility = View.GONE
                injectCSS()
                evaluacionWebView.visibility = View.VISIBLE
                view?.loadUrl(
                    "javascript:" +
                            "document.getElementsByTagName(\"head\")[0].innerHtml = document.getElementsByTagName(\"head\")[0].innerHtml+\"<meta name=\'viewport\' content=\'width=device-width, initial-scale=1.0\'/>\";" +
                            "if(document.getElementById(\"ctl00_mainCopy_Aceptar\") == null){" +
                            "   window.JSI.terminarTest();" +
                            "}"
                )
            }
        }, null)

        evaluacionWebView.addJavascriptInterface(JSI(), "JSI")

        evaluacionWebView.loadUrl(url)
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
            evaluacionWebView.loadUrl(
                "javascript:(function() {" +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var style = document.createElement('style');" +
                        "style.type = 'text/css';" +
                        "style.innerHTML = window.atob('" + encoded + "');" +
                        "parent.appendChild(style)" +
                        "})()"
            )
        } catch (e: Exception) {
            Log.e("AppException", e.toString())
        }

    }

    inner class JSI {
        @JavascriptInterface
        fun terminarTest() {
            runOnUiThread {
                Toast.makeText(
                    this@EvaluarProfesorActivity,
                    "¡Evaluacion correctamente enviada!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            this@EvaluarProfesorActivity.finish()
        }
    }
}