package ziox.ramiro.saes.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.material.bottomappbar.BottomAppBar
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.activities.StudentPerformanceActivity
import ziox.ramiro.saes.databases.AppLocalDatabase
import ziox.ramiro.saes.databases.KardexClass
import ziox.ramiro.saes.databases.KardexDao
import ziox.ramiro.saes.databinding.FragmentKardexBinding
import ziox.ramiro.saes.databinding.ViewKardexSemesterCourseItemBinding
import ziox.ramiro.saes.databinding.ViewKardexSemesterSectionItemBinding
import ziox.ramiro.saes.utils.*

/**
 * Creado por Ramiro el 10/14/2018 a las 5:12 PM para SAESv2.
 */
class KardexFragment : Fragment() {
    private val kardexList = ArrayList<KardexData>()
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private lateinit var kardexDatabase: KardexDao

    data class KardexData(val semester: String, val data: ArrayList<KardexItemData>?)
    data class KardexItemData(
        val courseName: String,
        val courseKey: String,
        val period: String,
        val evaluationMethod: String,
        val finalScore: String
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = FragmentKardexBinding.inflate(inflater, container, false)
        kardexDatabase = AppLocalDatabase.getInstance(requireContext()).kardexDao()
        rootView.parent.addBottomInsetPadding()
        val jsi = JSInterface(rootView)

        (activity as SAESActivity?)?.showFab(
            R.drawable.ic_unfold_more_black_24dp,
            {
                crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                if(jsi.expanded){
                    (activity as? SAESActivity)?.changeFabIcon(R.drawable.ic_unfold_more_black_24dp)
                }else{
                    (activity as? SAESActivity)?.changeFabIcon(R.drawable.ic_unfold_less_black_24dp)
                }
                jsi.toggleViews()
            },
            BottomAppBar.FAB_ALIGNMENT_MODE_END
        )

        rootView.header.studentPerformanceButton.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            activity?.startActivity(Intent(activity, StudentPerformanceActivity::class.java))
        }

        if (activity?.isNetworkAvailable() == true) {
            val kardexWebView = createWebView(activity, object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.loadUrl(
                        "javascript: var table = document.querySelector(\"#ctl00_mainCopy_Panel1 > #ctl00_mainCopy_Lbl_Kardex\").getElementsByTagName(\"tbody\");" +
                                "if(table == null)" +
                                "   window.JSI.notFound();" +
                                "else{" +
                                "   window.JSI.setFinalScore(document.getElementById(\"ctl00_mainCopy_Lbl_Promedio\").innerText);" +
                                "   window.JSI.setUserName(document.getElementById(\"ctl00_mainCopy_Lbl_Nombre\").getElementsByTagName(\"td\")[3].innerText);" +
                                "   window.JSI.setCareerName(document.getElementById(\"ctl00_mainCopy_Lbl_Carrera\").innerText);" +
                                "   for(var i = 0 ; i < table.length ; i++){" +
                                "       var row = table[i].getElementsByTagName(\"tr\");" +
                                "       var titulo = row[0].innerText;" +
                                "       if(titulo.toLowerCase().match(/semestre\$/g).length > 0){" +
                                "           window.JSI.addSemesterData(titulo);" +
                                "           for(var e = 2; e < row.length ; ++e){" +
                                "               var col = row[e].getElementsByTagName(\"td\");" +
                                "               var data = [];" +
                                "               for(var k = 0; k < col.length ; ++k){" +
                                "                   data.push(col[k].innerText);" +
                                "               }" +
                                "               window.JSI.addItem(data);" +
                                "           }" +
                                "           window.JSI.onSemesterCompleted();" +
                                "       }" +
                                "    }" +
                                "}" +
                                "window.JSI.onComplete();"
                    )
                }
            }, (activity as SAESActivity?)?.getProgressBar())

            kardexWebView.addJavascriptInterface(jsi, "JSI")

            kardexWebView.loadUrl(getUrl(activity) + "Alumnos/boleta/kardex.aspx")
        } else {
            jsi.isOffline = true
            val data = kardexDatabase.getAll()
            var currentSemester = "_"
            for (item in data){
                if (item.semester != currentSemester) {
                    jsi.addSemesterData(item.semester)
                    currentSemester = item.semester
                }

                if (item.courseName != "_") {
                    jsi.addItem(arrayOf("", item.courseName, item.semester, "", "", item.finalScore))
                } else {
                    jsi.setFinalScore(item.finalScore)
                }
            }

            jsi.onComplete()
        }

        return rootView.root
    }

    inner class JSInterface(val rootView: FragmentKardexBinding, var isOffline: Boolean = false) {
        var expanded = false
            private set
        private val items = ArrayList<ViewKardexSemesterSectionItemBinding>()

        @JavascriptInterface
        fun setUserName(userName: String) {
            setPreference(activity, "nombre", userName.toProperCase())
        }

        @JavascriptInterface
        fun setFinalScore(finalScore: String) {
            if (!isOffline) {
                kardexDatabase.deleteAll()

                kardexDatabase.insert(KardexClass("_", "_", finalScore))
            }

            activity?.runOnUiThread {
                rootView.header.finalScoreTextView.text = finalScore
                if(finalScore.toDoubleOrNull() ?: 10.0 < 6.0){
                    rootView.header.finalScoreTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDanger))
                }
            }

            kardexList.add(
                KardexData(
                    finalScore,
                    null
                )
            )
        }

        @JavascriptInterface
        fun setCareerName(careerName: String) {
            setPreference(activity, "carrera", careerName)
        }

        @JavascriptInterface
        fun addSemesterData(semester: String) {
            kardexList.add(
                KardexData(
                    semester,
                    ArrayList()
                )
            )
        }

        @JavascriptInterface
        fun addItem(data: Array<String>) {
            try {
                kardexList.last().data?.add(
                    KardexItemData(
                        data[1].toProperCase(),
                        data[0],
                        data[3],
                        data[4],
                        data[5]
                    )
                )

                if (!isOffline) {
                    kardexDatabase.insert(
                        KardexClass(
                            kardexList.last().data!!.last().courseName,
                            kardexList.last().semester,
                            kardexList.last().data!!.last().finalScore
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(this.javaClass.canonicalName, e.toString())
            }
        }

        @JavascriptInterface
        fun onSemesterCompleted() {

        }

        @JavascriptInterface
        fun onComplete() {
            for (kardexData in kardexList) {
                if (kardexData.data != null) {
                    if(activity !is SAESActivity) return

                    val holder = ViewKardexSemesterSectionItemBinding.inflate(layoutInflater)
                    items.add(holder)

                    activity?.runOnUiThread {
                        holder.sectionTextView.text = kardexData.semester.toProperCase()

                        holder.selfButton.setOnClickListener {
                            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                            holder.collapsibleContainer.toggle()
                            if (!holder.collapsibleContainer.isExpanded) {
                                holder.sectionTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary))
                                holder.arrowImageView.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary))
                            } else {
                                holder.sectionTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDanger))
                                holder.arrowImageView.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorDanger))
                            }

                            rotateView(holder.arrowImageView, !holder.collapsibleContainer.isExpanded)
                        }
                    }

                    for (kardexItemData in kardexData.data) {
                        val courseItemBinding = ViewKardexSemesterCourseItemBinding.inflate(layoutInflater)
                        activity?.runOnUiThread {
                            courseItemBinding.itemMateriaNombre.text = kardexItemData.courseName

                            val finalScore = kardexItemData.finalScore.toIntOrNull()

                            courseItemBinding.itemMateriaCalif.text = finalScore?.toString() ?: "-"

                            if(finalScore != null && activity != null){
                                if(finalScore < 6){
                                    courseItemBinding.itemMateriaCalif.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDanger))
                                }
                            }

                            holder.kardexContentLayout.addView(courseItemBinding.root)
                        }
                    }
                    activity?.runOnUiThread {
                        rootView.parent.addView(holder.root)
                    }
                }
            }
        }

        @JavascriptInterface
        fun notFound() {

        }

        fun toggleViews() {
            for (item in items) {
                if (expanded) {
                    activity?.runOnUiThread {
                        item.collapsibleContainer.collapse()
                        item.sectionTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary))
                        item.arrowImageView.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary))
                    }
                } else {
                    activity?.runOnUiThread {
                        item.collapsibleContainer.expand()
                        item.sectionTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDanger))
                        item.arrowImageView.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorDanger))
                    }
                }
                rotateView(item.arrowImageView, expanded)
            }
            expanded = !expanded
        }

        private fun rotateView(imageView: ImageView, expanded: Boolean) {
            val anim = RotateAnimation(
                180f, 0f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f
            )
            anim.duration = 300

            activity?.runOnUiThread {
                imageView.startAnimation(anim)
                imageView.rotation = if (expanded) 0f else 180f
            }
        }
    }
}