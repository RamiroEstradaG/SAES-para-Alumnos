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
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.ScheduleGeneratorActivity
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databases.*
import ziox.ramiro.saes.databinding.FragmentReEnrollmentAppointmentBinding
import ziox.ramiro.saes.utils.*

/**
 * Creado por Ramiro el 12/12/2018 a las 5:11 PM para SAESv2.
 */
class ReEnrollmentAppointmentFragment : Fragment() {
    lateinit var rootView: FragmentReEnrollmentAppointmentBinding
    lateinit var reEnrollmentDao: ReEnrollmentDao
    private val firebaseCrashlytics = FirebaseCrashlytics.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = FragmentReEnrollmentAppointmentBinding.inflate(inflater, container, false)
        reEnrollmentDao = AppLocalDatabase.getInstance(requireContext()).reEnrollmentDao()
        rootView.parent.addBottomInsetPadding()

        rootView.scheduleGeneratorButton.setOnClickListener {
            startActivity(Intent(activity, ScheduleGeneratorActivity::class.java))
        }

        rootView.occupancyButton.setOnClickListener {
            (activity as SAESActivity?)?.postNavigationItemSelected(R.id.nav_ocupabilidad, false)
        }


        if (activity?.isNetworkAvailable() == true) {
            val citaWebView = createWebView(activity, object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    reEnrollmentDao.deleteAll()
                    view?.loadUrl(
                        "javascript:" +
                                "if(table != null){" +
                                "   var table = document.getElementById(\"ctl00_mainCopy_grvEstatus_alumno\");" +
                                "   window.JSI.setAppointmentDate(table.getElementsByTagName(\"tr\")[1].children[2].innerText, table.getElementsByTagName(\"tr\")[1].children[3].innerText);" +
                                "}" +

                                "window.JSI.setLoad(parseFloat(document.getElementById(\"ctl00_mainCopy_CREDITOSCARRERA\").getElementsByTagName(\"tr\")[1].children[3].innerText)," +
                                "                       parseFloat(document.getElementById(\"ctl00_mainCopy_CREDITOSCARRERA\").getElementsByTagName(\"tr\")[1].children[2].innerText)," +
                                "                       parseFloat(document.getElementById(\"ctl00_mainCopy_CREDITOSCARRERA\").getElementsByTagName(\"tr\")[1].children[1].innerText));" +

                                "window.JSI.setPeriods(parseInt(document.getElementById(\"ctl00_mainCopy_alumno\").getElementsByTagName(\"tr\")[1].children[3].innerText)," +
                                "                           parseInt(document.getElementById(\"ctl00_mainCopy_CREDITOSCARRERA\").getElementsByTagName(\"tr\")[1].children[4].innerText)," +
                                "                           parseInt(document.getElementById(\"ctl00_mainCopy_CREDITOSCARRERA\").getElementsByTagName(\"tr\")[1].children[5].innerText));" +

                                "window.JSI.setCredits(parseFloat(document.getElementById(\"ctl00_mainCopy_alumno\").getElementsByTagName(\"tr\")[1].children[1].innerText)," +
                                "                           parseFloat(document.getElementById(\"ctl00_mainCopy_CREDITOSCARRERA\").getElementsByTagName(\"tr\")[1].children[0].innerText));"
                    )

                    rootView.loadingText.visibility = View.GONE
                    rootView.appointmentStatus.visibility = View.VISIBLE
                }
            }, (activity as SAESActivity?)?.getProgressBar())

            citaWebView.addJavascriptInterface(JSI(), "JSI")

            citaWebView.loadUrl(getUrl(activity) + "Alumnos/Reinscripciones/fichas_reinscripcion.aspx")
        } else {
            val jsi = JSI(true)
            val data = reEnrollmentDao.getAll()

            for (enrollmentData in  data){
                when (enrollmentData.type) {
                    TYPE_APPOINTMENT_DATE -> jsi.setAppointmentDate(enrollmentData.value1, enrollmentData.value2)
                    TYPE_LOAD -> jsi.setLoad(enrollmentData.value1.toFloat(), enrollmentData.value2.toFloat(), enrollmentData.value2.toFloat())
                    TYPE_PERIOD -> jsi.setPeriods(enrollmentData.value1.toInt(), enrollmentData.value2.toInt(), enrollmentData.value3.toInt())
                    TYPE_CREDITS -> jsi.setCredits(enrollmentData.value1.toDouble(), enrollmentData.value2.toDouble())
                }
            }

            rootView.loadingText.visibility = View.GONE
            rootView.appointmentStatus.visibility = View.VISIBLE
        }

        return rootView.root
    }

    @SuppressLint("SetTextI18n")
    inner class JSI(private val isOffline: Boolean = false) {
        @JavascriptInterface
        fun setAppointmentDate(appointmentDate: String, appointmentExpiration: String) {
            activity?.runOnUiThread {
                rootView.appointmentStatus.visibility = View.GONE
                rootView.appointmentLayout.visibility = View.VISIBLE

                rootView.startDateTextView.text = appointmentDate.toDateString()
                rootView.finishDateTextView.text = appointmentExpiration.toDateString()

                rootView.addToPersonalCalendarButton.setOnClickListener {
                    firebaseCrashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                    val intent = Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.Events.TITLE, "Reinscripción")
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, appointmentDate.toDate().time)
                        .putExtra(
                            CalendarContract.EXTRA_EVENT_END_TIME,
                            appointmentExpiration.toDate().time
                        )

                    if (intent.resolveActivity(requireContext().packageManager) != null)
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
                reEnrollmentDao.insert(
                    ReEnrollmentData(
                        TYPE_APPOINTMENT_DATE,
                        appointmentDate,
                        appointmentExpiration,
                        ""
                    )
                )
            }
        }

        @JavascriptInterface
        fun setLoad(minimumLoad: Float, mediumLoad: Float, maximumLoad: Float) {
            activity?.runOnUiThread {
                ValueAnimator.ofInt(0, (minimumLoad * 100).toInt()).apply {
                    duration = 400
                    interpolator = AccelerateDecelerateInterpolator()

                    addUpdateListener {
                        rootView.minimumLoadTextView.text =
                            ((it.animatedValue as Int) / 100f).toString()
                    }

                    start()
                }
                ValueAnimator.ofInt(0, (mediumLoad * 100).toInt()).apply {
                    duration = 400
                    interpolator = AccelerateDecelerateInterpolator()

                    addUpdateListener {
                        rootView.mediumLoadTextView.text =
                            ((it.animatedValue as Int) / 100f).toString()
                    }

                    start()
                }
                ValueAnimator.ofInt(0, (maximumLoad * 100).toInt()).apply {
                    duration = 400
                    interpolator = AccelerateDecelerateInterpolator()

                    addUpdateListener {
                        rootView.maximumLoadTextView.text =
                            ((it.animatedValue as Int) / 100f).toString()
                    }

                    start()
                }


            }

            if (!isOffline) {
                reEnrollmentDao.insert(
                    ReEnrollmentData(
                        TYPE_LOAD,
                        minimumLoad.toString(),
                        mediumLoad.toString(),
                        maximumLoad.toString()
                    )
                )
            }
        }

        @JavascriptInterface
        fun setPeriods(completed: Int, normalFinishPeriod: Int, periodMaximumLimit: Int) {
            activity?.runOnUiThread {
                rootView.currenCompletedPeriodsProgressBar.max = periodMaximumLimit * 100
                rootView.normalFinishPeriodProgressBar.max = periodMaximumLimit * 100

                ValueAnimator.ofInt(0, normalFinishPeriod * 100).apply {
                    duration = 400
                    interpolator = AccelerateDecelerateInterpolator()

                    addUpdateListener {
                        rootView.normalFinishPeriodProgressBar.progress = (it.animatedValue as Int)
                        rootView.normalFinishPeriodTextView.text =
                            "Duración normal (${(it.animatedValue as Int) / 100})"
                    }

                    start()
                }

                ValueAnimator.ofInt(0, completed * 100).apply {
                    duration = 400
                    interpolator = AccelerateDecelerateInterpolator()

                    addUpdateListener {
                        rootView.currenCompletedPeriodsProgressBar.progress = (it.animatedValue as Int)
                        rootView.currentCompletedPeriodsTextView.text =
                            "Cursado (${(it.animatedValue as Int) / 100})"
                    }

                    start()
                }
            }

            if (!isOffline) {
                reEnrollmentDao.insert(
                    ReEnrollmentData(
                        TYPE_PERIOD,
                        completed.toString(),
                        normalFinishPeriod.toString(),
                        periodMaximumLimit.toString()
                    )
                )
            }
        }

        @JavascriptInterface
        fun setCredits(obtained: Double, maximumLimit: Double) {
            activity?.runOnUiThread {
                rootView.obtainedCreditsProgressBar.max = maximumLimit.toInt() * 100

                ValueAnimator.ofInt(0, obtained.toInt() * 100).apply {
                    duration = 600
                    interpolator = AccelerateDecelerateInterpolator()

                    addUpdateListener {
                        rootView.obtainedCreditsProgressBar.progress = (it.animatedValue as Int)
                        rootView.obtainedCreditsTextView.text =
                            "Créditos obtenidos: ${((it.animatedValue as Int) / 100.0)} de $maximumLimit (${(((it.animatedValue as Int).div(100)).div(maximumLimit)).times(100).toStringPrecision(2)}%)"
                    }

                    start()
                }
            }

            if (!isOffline) {
                reEnrollmentDao.insert(
                    ReEnrollmentData(
                        TYPE_CREDITS,
                        obtained.toString(),
                        maximumLimit.toString(),
                        ""
                    )
                )
            }
        }
    }
}