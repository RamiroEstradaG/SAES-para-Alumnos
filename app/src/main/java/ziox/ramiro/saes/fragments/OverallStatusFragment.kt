package ziox.ramiro.saes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databinding.FragmentOverallStatusBinding
import ziox.ramiro.saes.utils.createWebView
import ziox.ramiro.saes.utils.dpToPixel
import ziox.ramiro.saes.utils.getUrl
import ziox.ramiro.saes.utils.toProperCase

/**
 * Creado por Ramiro el 12/5/2018 a las 11:40 PM para SAESv2.
 */
class OverallStatusFragment : Fragment() {
    lateinit var rootView: FragmentOverallStatusBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = FragmentOverallStatusBinding.inflate(inflater, container, false)

        val webView = createWebView(activity, object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.loadUrl("javascript:" +
                        "var tab1 = document.getElementById(\"ctl00_mainCopy_grvEstatus_alumno\");" +
                        "if(tab1 != null){" +
                        "   var row = tab1.getElementsByTagName(\"tr\");" +
                        "   for(var i = 1 ; i < row.length ; ++i){" +
                        "       window.JSI.addFailedCourse(row[i].children[1].innerText);" +
                        "   }" +
                        "}" +
                        "var tab2 = document.getElementById(\"ctl00_mainCopy_GridView2\");" +
                        "if(tab2 != null){" +
                        "   var row = tab2.getElementsByTagName(\"tr\");" +
                        "   for(var i = 1 ; i < row.length ; ++i){" +
                        "       window.JSI.addOutdatedCourse(row[i].children[1].innerText);" +
                        "   }" +
                        "}" +
                        "if(tab1 == null && tab2 == null){" +
                        "   window.JSI.onEmptyLayout();" +
                        "}")
            }
        }, (activity as SAESActivity).getProgressBar())

        webView.addJavascriptInterface(JSI(), "JSI")

        webView.loadUrl(getUrl(activity)+"Alumnos/boleta/Estado_Alumno.aspx")

        return rootView.root
    }

    inner class JSI{
        @JavascriptInterface
        fun addFailedCourse(name : String){
            val textView = TextView(activity)
            textView.textSize = 18f
            textView.text = name.toProperCase()
            textView.setPadding(dpToPixel(activity, 8),dpToPixel(activity, 8),dpToPixel(activity, 8),dpToPixel(activity, 8))
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorSecondaryText))

            activity?.runOnUiThread {
                rootView.failedCoursesLayout.addView(textView)
                rootView.failedCourseTitle.visibility = View.VISIBLE
            }
        }

        @JavascriptInterface
        fun addOutdatedCourse(name : String){
            val textView = TextView(activity)
            textView.textSize = 18f
            textView.text = name.toProperCase()
            textView.setPadding(dpToPixel(activity, 8),dpToPixel(activity, 8),dpToPixel(activity, 8),dpToPixel(activity, 8))
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorSecondaryText))

            activity?.runOnUiThread {
                rootView.outdatedCoursesLayout.addView(textView)
                rootView.outatedCourseTitle.visibility = View.VISIBLE
            }
        }

        @JavascriptInterface
        fun onEmptyLayout(){
            if(activity is SAESActivity){
                (activity as SAESActivity).showEmptyText("Tu estado general es regular")
            }
        }
    }
}