package ziox.ramiro.saes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.fragment_calificaciones.view.*
import kotlinx.android.synthetic.main.view_calificacion_semestre.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databases.CalificacionesDatabase
import ziox.ramiro.saes.utils.*

/**
 * Creado por Ramiro el 12/6/2018 a las 12:25 AM para SAESv2.
 */
class CalificacionesFragment : Fragment() {
    lateinit var rootView: View
    lateinit var calificacionesDatabase: CalificacionesDatabase
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_calificaciones, container, false)
        calificacionesDatabase = CalificacionesDatabase(activity)
        calificacionesDatabase.createTable()

        rootView.mainLayout.addBottomInsetPadding()

        if(activity?.haveDonated() == false) {
            rootView.adView2.loadAd(AdRequest.Builder().build())
        }else{
            rootView.adView2.visibility = View.GONE
            rootView.adSeparator.visibility = View.GONE
        }

        if (activity?.isNetworkAvailable() == true) {
            val webView = createWebView(activity, object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    calificacionesDatabase.deleteTable()
                    calificacionesDatabase.createTable()

                    view?.loadUrl(
                        "javascript:" +
                                "if(document.getElementById(\"ctl00_mainCopy_Btn_Evaluar\") != null){" +
                                "   window.JSI.onCalificarProfesor();" +
                                "} else if(document.getElementById(\"ctl00_mainCopy_GV_Calif\") == null){" +
                                "   window.JSI.onEmptyLayout();" +
                                "} else {" +
                                "   for(var i = 1 ; i < document.getElementById(\"ctl00_mainCopy_GV_Calif\").children[0].children.length ; i++) " +
                                "       window.JSI.addCalificacion(document.getElementById(\"ctl00_mainCopy_GV_Calif\").children[0].children[i].children[1].innerText," +
                                "           document.getElementById(\"ctl00_mainCopy_GV_Calif\").children[0].children[i].children[2].innerText," +
                                "           document.getElementById(\"ctl00_mainCopy_GV_Calif\").children[0].children[i].children[3].innerText," +
                                "           document.getElementById(\"ctl00_mainCopy_GV_Calif\").children[0].children[i].children[4].innerText," +
                                "           document.getElementById(\"ctl00_mainCopy_GV_Calif\").children[0].children[i].children[5].innerText," +
                                "           document.getElementById(\"ctl00_mainCopy_GV_Calif\").children[0].children[i].children[6].innerText);" +
                                "}"
                    )
                }
            }, (activity as SAESActivity).getProgressBar())

            rootView.buttonCalificarProfesor.setOnClickListener {
                crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                (activity as SAESActivity).postNavigationItemSelected(R.id.nav_eval_prof, false)
            }

            webView.addJavascriptInterface(JSI(), "JSI")

            webView.loadUrl(getUrl(activity) + "Alumnos/Informacion_semestral/calificaciones_sem.aspx")
        } else {
            val jsi = JSI(true)
            val data = calificacionesDatabase.getAll()

            if (data.count == 0) {
                jsi.onEmptyLayout()
            } else {
                while (data.moveToNext()) {
                    val v = CalificacionesDatabase.cursorAsClaseData(data)
                    jsi.addCalificacion(v.materia, v.p1, v.p2, v.p3, v.extra, v.final)
                }
            }
        }

        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        if(::calificacionesDatabase.isInitialized){
            calificacionesDatabase.close()
        }
    }

    inner class JSI(private val isOffline: Boolean = false) {
        @JavascriptInterface
        fun addCalificacion(
            materia: String,
            p1: String,
            p2: String,
            p3: String,
            extra: String,
            final: String
        ) {
            val cardView = LayoutInflater.from(activity)
                .inflate(R.layout.view_calificacion_semestre, null, false)
            rootView.calificarProfesorWarning.visibility = View.GONE
            activity?.runOnUiThread {
                cardView.calif_materia_btn.text = materia.toProperCase()
                cardView.calif_parcial1.text = p1
                cardView.calif_parcial2.text = p2
                cardView.calif_parcial3.text = p3
                cardView.calif_extra.text = extra
                cardView.calif_final.text = final
                rootView.mainLayout.addView(cardView)

                if(activity != null){
                    if(p1.toIntOrNull() != null) {
                        if (p1.toInt() < 6) {
                            cardView.calif_parcial1.setTextColor(
                                ContextCompat.getColor(
                                    activity!!,
                                    R.color.colorHighlight
                                )
                            )
                        }
                    }
                    if(p2.toIntOrNull() != null) {
                        if (p2.toInt() < 6) {
                            cardView.calif_parcial2.setTextColor(
                                ContextCompat.getColor(
                                    activity!!,
                                    R.color.colorHighlight
                                )
                            )
                        }
                    }
                    if(p3.toIntOrNull() != null) {
                        if (p3.toInt() < 6) {
                            cardView.calif_parcial3.setTextColor(
                                ContextCompat.getColor(
                                    activity!!,
                                    R.color.colorHighlight
                                )
                            )
                        }
                    }
                    if(extra.toIntOrNull() != null) {
                        if (extra.toInt() < 6) {
                            cardView.calif_extra.setTextColor(
                                ContextCompat.getColor(
                                    activity!!,
                                    R.color.colorHighlight
                                )
                            )
                        }
                    }
                    if(final.toIntOrNull() != null) {
                        if (final.toInt() < 6) {
                            cardView.calif_final.setTextColor(
                                ContextCompat.getColor(
                                    activity!!,
                                    R.color.colorHighlight
                                )
                            )
                        }
                    }
                }
            }

            if (!isOffline) {
                calificacionesDatabase.addMateria(
                    CalificacionesDatabase.Data(
                        materia,
                        "",
                        p1,
                        p2,
                        p3,
                        extra,
                        final
                    )
                )
            }
        }

        @JavascriptInterface
        fun onEmptyLayout() {
            if (activity is SAESActivity) {
                activity?.runOnUiThread {
                    rootView.calificarProfesorWarning.visibility = View.GONE
                }
                (activity as SAESActivity).showEmptyText("No hay materias")
            }
        }

        @JavascriptInterface
        fun onCalificarProfesor() {
            if (activity is SAESActivity) {
                activity?.runOnUiThread {
                    rootView.calificarProfesorWarning.visibility = View.VISIBLE
                }
            }
        }
    }
}