package ziox.ramiro.saes.fragments

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.fragment_cita_reinscripcion.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.CreateHorarioActivity
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.sql.ReinscripcionDatabase
import ziox.ramiro.saes.utils.*

/**
 * Creado por Ramiro el 12/12/2018 a las 5:11 PM para SAESv2.
 */
class CitaReinscripcionFragment : Fragment() {
    lateinit var rootView: View
    lateinit var citaReinscripcionDatabase: ReinscripcionDatabase
    private val crashlytics = FirebaseCrashlytics.getInstance()

    companion object {
        const val TIPO_FECHA_CITA = "FCITA"
        const val TIPO_CARGAS = "CARGAS"
        const val TIPO_PERIODOS = "PERIODOS"
        const val TIPO_CREDITOS = "CREDITOS"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_cita_reinscripcion, container, false)
        citaReinscripcionDatabase = ReinscripcionDatabase(activity)
        rootView.citaReinscripcionParent.addBottomInsetPadding()

        rootView.buttonGenerarHorario.setOnClickListener {
            startActivity(Intent(activity, CreateHorarioActivity::class.java))
        }

        rootView.buttonRevisarOcupabilidad.setOnClickListener {
            (activity as SAESActivity?)?.postNavigationItemSelected(R.id.nav_ocupabilidad, false)
        }


        if (activity?.isNetworkAvailable() == true) {
            val citaWebView = createWebView(activity, object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    citaReinscripcionDatabase.deleteTable()
                    citaReinscripcionDatabase.createTable()
                    view?.loadUrl(
                        "javascript:" +
                                "if(table != null){" +
                                "   var table = document.getElementById(\"ctl00_mainCopy_grvEstatus_alumno\");" +
                                "   window.JSI.setFechaCita(table.getElementsByTagName(\"tr\")[1].children[2].innerText, table.getElementsByTagName(\"tr\")[1].children[3].innerText);" +
                                "}" +

                                "window.JSI.setCargas(parseFloat(document.getElementById(\"ctl00_mainCopy_CREDITOSCARRERA\").getElementsByTagName(\"tr\")[1].children[3].innerText)," +
                                "                       parseFloat(document.getElementById(\"ctl00_mainCopy_CREDITOSCARRERA\").getElementsByTagName(\"tr\")[1].children[2].innerText)," +
                                "                       parseFloat(document.getElementById(\"ctl00_mainCopy_CREDITOSCARRERA\").getElementsByTagName(\"tr\")[1].children[1].innerText));" +

                                "window.JSI.setPeriodos(parseInt(document.getElementById(\"ctl00_mainCopy_alumno\").getElementsByTagName(\"tr\")[1].children[3].innerText)," +
                                "                           parseInt(document.getElementById(\"ctl00_mainCopy_CREDITOSCARRERA\").getElementsByTagName(\"tr\")[1].children[4].innerText)," +
                                "                           parseInt(document.getElementById(\"ctl00_mainCopy_CREDITOSCARRERA\").getElementsByTagName(\"tr\")[1].children[5].innerText));" +

                                "window.JSI.setCreditos(parseFloat(document.getElementById(\"ctl00_mainCopy_alumno\").getElementsByTagName(\"tr\")[1].children[1].innerText)," +
                                "                           parseFloat(document.getElementById(\"ctl00_mainCopy_CREDITOSCARRERA\").getElementsByTagName(\"tr\")[1].children[0].innerText));"
                    )

                    rootView.loadingText.visibility = View.GONE
                    rootView.citaStatus.visibility = View.VISIBLE
                }
            }, (activity as SAESActivity?)?.getProgressBar())

            citaWebView.addJavascriptInterface(JSI(), "JSI")

            citaWebView.loadUrl(getUrl(activity) + "Alumnos/Reinscripciones/fichas_reinscripcion.aspx")
        } else {
            citaReinscripcionDatabase.createTable()
            val jsi = JSI(true)
            val data = citaReinscripcionDatabase.getAll()

            while (data.moveToNext()) {
                val v = ReinscripcionDatabase.cursorAsClaseData(data)

                when (v.tipo) {
                    TIPO_FECHA_CITA -> jsi.setFechaCita(v.v1, v.v2)
                    TIPO_CARGAS -> jsi.setCargas(v.v1.toFloat(), v.v2.toFloat(), v.v2.toFloat())
                    TIPO_PERIODOS -> jsi.setPeriodos(v.v1.toInt(), v.v2.toInt(), v.v3.toInt())
                    TIPO_CREDITOS -> jsi.setCreditos(v.v1.toDouble(), v.v2.toDouble())
                }
            }

            rootView.loadingText.visibility = View.GONE
            rootView.citaStatus.visibility = View.VISIBLE
        }

        return rootView
    }

    @SuppressLint("SetTextI18n")
    inner class JSI(private val isOffline: Boolean = false) {
        @JavascriptInterface
        fun setFechaCita(cita: String, caducidad: String) {
            activity?.runOnUiThread {
                rootView.citaStatus.visibility = View.GONE
                rootView.citaLayout.visibility = View.VISIBLE

                rootView.labelFechaInicio.text = cita.toDateString()
                rootView.labelFechaFinal.text = caducidad.toDateString()

                rootView.buttonRecordatorio.setOnClickListener {
                    crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    val intent = Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.Events.TITLE, "Reinscripción")
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cita.toDate().time)
                        .putExtra(
                            CalendarContract.EXTRA_EVENT_END_TIME,
                            caducidad.toDate().time
                        )

                    if (intent.resolveActivity(activity!!.packageManager) != null)
                        startActivity(intent)
                    else
                        Toast.makeText(
                            activity,
                            "No hay aplicaciones que puedan ejecutar esta accion",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }

            if (!isOffline) {
                citaReinscripcionDatabase.addData(
                    ReinscripcionDatabase.Data(
                        TIPO_FECHA_CITA,
                        cita,
                        caducidad,
                        ""
                    )
                )
            }
        }

        @JavascriptInterface
        fun setCargas(minima: Float, media: Float, maxima: Float) {
            activity?.runOnUiThread {
                ValueAnimator.ofInt(0, (minima * 100).toInt()).apply {
                    duration = 400
                    interpolator = AccelerateDecelerateInterpolator()

                    addUpdateListener {
                        rootView.labelCargaMinima.text =
                            ((it.animatedValue as Int) / 100f).toString()
                    }

                    start()
                }
                ValueAnimator.ofInt(0, (media * 100).toInt()).apply {
                    duration = 400
                    interpolator = AccelerateDecelerateInterpolator()

                    addUpdateListener {
                        rootView.labelCargaMedia.text =
                            ((it.animatedValue as Int) / 100f).toString()
                    }

                    start()
                }
                ValueAnimator.ofInt(0, (maxima * 100).toInt()).apply {
                    duration = 400
                    interpolator = AccelerateDecelerateInterpolator()

                    addUpdateListener {
                        rootView.labelCargaMaxima.text =
                            ((it.animatedValue as Int) / 100f).toString()
                    }

                    start()
                }


            }

            if (!isOffline) {
                citaReinscripcionDatabase.addData(
                    ReinscripcionDatabase.Data(
                        TIPO_CARGAS,
                        minima.toString(),
                        media.toString(),
                        maxima.toString()
                    )
                )
            }
        }

        @JavascriptInterface
        fun setPeriodos(cursado: Int, normal: Int, maximo: Int) {
            activity?.runOnUiThread {
                rootView.progressPeriodosCursados.max = maximo * 100
                rootView.progressDuracionCarrera.max = maximo * 100

                ValueAnimator.ofInt(0, normal * 100).apply {
                    duration = 400
                    interpolator = AccelerateDecelerateInterpolator()

                    addUpdateListener {
                        rootView.progressDuracionCarrera.progress = (it.animatedValue as Int)
                        rootView.labelDuracionNormal.text =
                            "Duración normal (${(it.animatedValue as Int) / 100})"
                    }

                    start()
                }

                ValueAnimator.ofInt(0, cursado * 100).apply {
                    duration = 400
                    interpolator = AccelerateDecelerateInterpolator()

                    addUpdateListener {
                        rootView.progressPeriodosCursados.progress = (it.animatedValue as Int)
                        rootView.labelPeriodosCursados.text =
                            "Cursado (${(it.animatedValue as Int) / 100})"
                    }

                    start()
                }
            }

            if (!isOffline) {
                citaReinscripcionDatabase.addData(
                    ReinscripcionDatabase.Data(
                        TIPO_PERIODOS,
                        cursado.toString(),
                        normal.toString(),
                        maximo.toString()
                    )
                )
            }
        }

        @JavascriptInterface
        fun setCreditos(obtenido: Double, maximo: Double) {
            activity?.runOnUiThread {
                rootView.progressCreditosObtenidos.max = maximo.toInt() * 100

                ValueAnimator.ofInt(0, obtenido.toInt() * 100).apply {
                    duration = 600
                    interpolator = AccelerateDecelerateInterpolator()

                    addUpdateListener {
                        rootView.progressCreditosObtenidos.progress = (it.animatedValue as Int)
                        rootView.labelCreditosObtenidos.text =
                            "Créditos obtenidos: ${((it.animatedValue as Int) / 100.0)} de $maximo (${(((it.animatedValue as Int).div(100)).div(maximo)).times(100).toStringPresition(2)}%)"
                    }

                    start()
                }
            }

            if (!isOffline) {
                citaReinscripcionDatabase.addData(
                    ReinscripcionDatabase.Data(
                        TIPO_CREDITOS,
                        obtenido.toString(),
                        maximo.toString(),
                        ""
                    )
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(::citaReinscripcionDatabase.isInitialized){
            citaReinscripcionDatabase.close()
        }
    }
}