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
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databases.AppLocalDatabase
import ziox.ramiro.saes.databases.CourseGrade
import ziox.ramiro.saes.databases.GradesDao
import ziox.ramiro.saes.databinding.FragmentGradesBinding
import ziox.ramiro.saes.databinding.ViewCourseGradesBinding
import ziox.ramiro.saes.utils.*

/**
 * Creado por Ramiro el 12/6/2018 a las 12:25 AM para SAESv2.
 */
class GradesFragment : Fragment() {
    lateinit var rootView: FragmentGradesBinding
    lateinit var gradesDao: GradesDao
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = FragmentGradesBinding.inflate(inflater, container, false)
        gradesDao = AppLocalDatabase.getInstance(requireContext()).gradesDao()

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
                    gradesDao.deleteAll()

                    view?.loadUrl(
                        "javascript:" +
                                "if(document.getElementById(\"ctl00_mainCopy_Btn_Evaluar\") != null){" +
                                "   window.JSI.onRequireTeacherRate();" +
                                "} else if(document.getElementById(\"ctl00_mainCopy_GV_Calif\") == null){" +
                                "   window.JSI.onEmptyLayout();" +
                                "} else {" +
                                "   for(var i = 1 ; i < document.getElementById(\"ctl00_mainCopy_GV_Calif\").children[0].children.length ; i++) " +
                                "       window.JSI.addGrade(document.getElementById(\"ctl00_mainCopy_GV_Calif\").children[0].children[i].children[1].innerText," +
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
            val grades = gradesDao.getAll()

            if (grades.isEmpty()) {
                jsi.onEmptyLayout()
            } else {
                for (courseGrade in grades){
                    jsi.addGrade(
                        courseGrade.courseName,
                        courseGrade.partialOne,
                        courseGrade.partialTwo,
                        courseGrade.partialThree,
                        courseGrade.extraordinary,
                        courseGrade.finalScore
                    )
                }
            }
        }

        return rootView.root
    }

    inner class JSI(private val isOffline: Boolean = false) {
        @JavascriptInterface
        fun addGrade(
            courseName: String,
            p1: String,
            p2: String,
            p3: String,
            extra: String,
            final: String
        ) {
            val cardView = ViewCourseGradesBinding.inflate(layoutInflater)
            rootView.calificarProfesorWarning.visibility = View.GONE
            activity?.runOnUiThread {
                cardView.courseNameTextView.text = courseName.toProperCase()
                cardView.partialOneTextView.text = p1
                cardView.partialTwoTextView.text = p2
                cardView.partialThreeTextView.text = p3
                cardView.extraordinaryTextView.text = extra
                cardView.finalScoreTextView.text = final
                rootView.mainLayout.addView(cardView.root)

                if(activity != null){
                    if(p1.toIntOrNull() != null) {
                        if (p1.toInt() < 6) {
                            cardView.partialOneTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDanger))
                        }else{
                            cardView.partialOneTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorInfo))
                        }
                    }
                    if(p2.toIntOrNull() != null) {
                        if (p2.toInt() < 6) {
                            cardView.partialTwoTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDanger))
                        }else{
                            cardView.partialTwoTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorInfo))
                        }
                    }
                    if(p3.toIntOrNull() != null) {
                        if (p3.toInt() < 6) {
                            cardView.partialThreeTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDanger))
                        }else{
                            cardView.partialThreeTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorInfo))
                        }
                    }
                    if(extra.toIntOrNull() != null) {
                        if (extra.toInt() < 6) {
                            cardView.extraordinaryTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDanger))
                        }else{
                            cardView.extraordinaryTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorInfo))
                        }
                    }
                    if(final.toIntOrNull() != null) {
                        if (final.toInt() < 6) {
                            cardView.finalScoreTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDanger))
                        }else{
                            cardView.finalScoreTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorInfo))
                        }
                    }
                }
            }

            if (!isOffline) {
                gradesDao.insert(
                    CourseGrade(
                        courseName,
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
        fun onRequireTeacherRate() {
            if (activity is SAESActivity) {
                activity?.runOnUiThread {
                    rootView.calificarProfesorWarning.visibility = View.VISIBLE
                }
            }
        }
    }
}