package ziox.ramiro.saes.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.SimpleAdapter
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.TeacherEvaluationActivity
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databinding.FragmentTeacherEvaluationListBinding
import ziox.ramiro.saes.utils.*

/**
 * Creado por Ramiro el 12/10/2018 a las 1:03 PM para SAES.
 */
class TeacherEvaluationListFragment : Fragment() {
    val data : ArrayList<Map<String, Any>> = ArrayList()
    lateinit var rootView: FragmentTeacherEvaluationListBinding
    private lateinit var teacherListWebView : WebView
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = FragmentTeacherEvaluationListBinding.inflate(inflater, container, false)
        rootView.teacherListView.addBottomInsetPadding()

        rootView.teacherListView.adapter = SimpleAdapter(activity, data, R.layout.sample_evaluacion_profesores_list_item, arrayOf("nombre", "materia"), intArrayOf(R.id.evalProfesorNombre, R.id.evalProfesorMateria))

        val interstitialAd = InterstitialAd(context)

        if(activity?.haveDonated() == false) {
            interstitialAd.adUnitId = resources.getString(R.string.calificacionProfesoresAdId)
            interstitialAd.adListener = object : AdListener() {
                override fun onAdClosed() {
                    super.onAdClosed()
                    interstitialAd.loadAd(AdRequest.Builder().build())

                    val intent = Intent(activity, TeacherEvaluationActivity::class.java)
                    intent.putExtra("nombre", data[0]["nombre"] as String)
                    intent.putExtra("url", data[0]["url"] as String)
                    activity?.startActivity(intent)
                }
            }
            interstitialAd.loadAd(AdRequest.Builder().build())
        }

        rootView.teacherListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            crashlytics.log("Click en Profesor en la clase ${this.javaClass.canonicalName}")
            if(rootView.teacherListView.count > 1){
                val intent = Intent(activity, TeacherEvaluationActivity::class.java)
                intent.putExtra("nombre", data[position]["nombre"] as String)
                intent.putExtra("url", data[position]["url"] as String)
                activity?.startActivity(intent)
            }else{
                if(activity?.haveDonated() == false) {
                    interstitialAd.show()
                }else{
                    val intent = Intent(activity, TeacherEvaluationActivity::class.java)
                    intent.putExtra("nombre", data[0]["nombre"] as String)
                    intent.putExtra("url", data[0]["url"] as String)
                    activity?.startActivity(intent)
                }
            }
        }

        teacherListWebView = createWebView(activity, object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                activity?.runOnUiThread {
                    data.clear()
                    (rootView.teacherListView.adapter as SimpleAdapter).notifyDataSetChanged()
                }
                view?.loadUrl("javascript: " +
                        "if(document.getElementById(\"ctl00_mainCopy_GV_Profe\") == null){" +
                        "   window.JSI.notFound();" +
                        "}else{" +
                        "   var rows = document.getElementById(\"ctl00_mainCopy_GV_Profe\").getElementsByTagName(\"tr\");" +
                        "   for(var i = 1 ; i < rows.length; i++){" +
                        "       var cols = rows[i].getElementsByTagName(\"td\");" +
                        "       var url = cols[3].getElementsByTagName(\"a\")[1].href;" +
                        "       window.JSI.addTeacher(cols[2].innerText, cols[1].innerText, url);" +
                        "   }" +
                        "}")
            }
        },(activity as SAESActivity?)?.getProgressBar())

        teacherListWebView.addJavascriptInterface(JSI(), "JSI")

        teacherListWebView.loadUrl(getUrl(activity)+"/Alumnos/Evaluacion_docente/califica_profe.aspx")

        return rootView.root
    }

    override fun onResume() {
        super.onResume()
        teacherListWebView.reload()
    }

    inner class JSI{

        @JavascriptInterface
        fun addTeacher(teacherName: String, courseName: String, url: String){
            val teacherData = hashMapOf(
                    "nombre" to teacherName.toProperCase(),
                    "materia" to courseName.toProperCase(),
                    "inicial" to teacherName[0],
                    "url" to url
            )

            activity?.runOnUiThread {
                data.add(teacherData)
                (rootView.teacherListView.adapter as SimpleAdapter).notifyDataSetChanged()
                if(activity is SAESActivity){
                    (activity as SAESActivity).hideEmptyText()
                }

            }
        }

        @JavascriptInterface
        fun notFound(){
            activity?.runOnUiThread {
                if(activity is SAESActivity){
                    (activity as SAESActivity).showEmptyText("No hay profesores a evaluar")
                }
            }
        }
    }


}