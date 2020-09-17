package ziox.ramiro.saes.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.android.synthetic.main.fragment_kardex.view.*
import kotlinx.android.synthetic.main.item_kardex_content.view.*
import kotlinx.android.synthetic.main.item_kardex_content_materia.view.*
import kotlinx.android.synthetic.main.item_kardex_header.view.*
import net.cachapa.expandablelayout.ExpandableLayout
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.activities.VisualizarKardexActivity
import ziox.ramiro.saes.databases.KardexDatabase
import ziox.ramiro.saes.utils.*

/**
 * Creado por Ramiro el 10/14/2018 a las 5:12 PM para SAESv2.
 */
class KardexFragment : Fragment() {
    private val kardexList = ArrayList<KardexData>()
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private lateinit var kardexDatabase: KardexDatabase

    data class KardexData(val semestre: String, val data: ArrayList<MateriaData>?)
    data class MateriaData(
        val nombre: String,
        val clave: String,
        val periodo: String,
        val formaEval: String,
        val calif: String
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_kardex, container, false)
        kardexDatabase = KardexDatabase(activity)
        rootView.kardexMain.addBottomInsetPadding()

        val jsi = JSInterface(rootView)

        (activity as SAESActivity?)?.showFab(
            R.drawable.ic_unfold_more_black_24dp,
            View.OnClickListener {
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

        rootView.buttonKardexStats.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            activity?.startActivity(Intent(activity, VisualizarKardexActivity::class.java))
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
                                "   window.JSI.addPromedio(document.getElementById(\"ctl00_mainCopy_Lbl_Promedio\").innerText);" +
                                "   window.JSI.setNombre(document.getElementById(\"ctl00_mainCopy_Lbl_Nombre\").getElementsByTagName(\"td\")[3].innerText);" +
                                "   window.JSI.setCarrera(document.getElementById(\"ctl00_mainCopy_Lbl_Carrera\").innerText);" +
                                "   for(var i = 0 ; i < table.length ; i++){" +
                                "       var row = table[i].getElementsByTagName(\"tr\");" +
                                "       var titulo = row[0].innerText;" +
                                "       if(titulo.toLowerCase().match(/semestre\$/g).length > 0){" +
                                "           window.JSI.addSemestre(titulo);" +
                                "           for(var e = 2; e < row.length ; ++e){" +
                                "               var col = row[e].getElementsByTagName(\"td\");" +
                                "               var data = [];" +
                                "               for(var k = 0; k < col.length ; ++k){" +
                                "                   data.push(col[k].innerText);" +
                                "               }" +
                                "               window.JSI.addMateria(data);" +
                                "           }" +
                                "           window.JSI.onSemestreCompleted();" +
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
            kardexDatabase.createTable()
            val data = kardexDatabase.getAll()
            var currentSemestre = "_"
            while (data.moveToNext()) {
                val v = KardexDatabase.cursorAsData(data)
                if (v.semestre != currentSemestre) {
                    jsi.addSemestre(v.semestre)
                    currentSemestre = v.semestre
                }

                if (v.name != "_") {
                    jsi.addMateria(arrayOf("", v.name, v.semestre, "", "", v.calificacion))
                } else {
                    jsi.addPromedio(v.calificacion)
                }
            }

            jsi.onComplete()
        }

        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        if(::kardexDatabase.isInitialized){
            kardexDatabase.close()
        }
    }

    inner class JSInterface(val rootView: View, var isOffline: Boolean = false) {
        var expanded = false
            private set
        private val items = ArrayList<Pair<LinearLayout, ExpandableLayout>>()

        @JavascriptInterface
        fun setNombre(nombre: String) {
            setPreference(activity, "nombre", nombre.toProperCase())
        }

        @JavascriptInterface
        fun addPromedio(promedio: String) {
            if (!isOffline) {
                kardexDatabase.deleteTable()
                kardexDatabase.createTable()

                kardexDatabase.addMateria(KardexDatabase.Data("_", "_", promedio))
            }

            activity?.runOnUiThread {
                rootView.kardexItemPromedio.text = promedio
                if(promedio.toDoubleOrNull() ?: 10.0 < 6.0){
                    rootView.kardexItemPromedio.setTextColor(ContextCompat.getColor(activity!!, R.color.colorDanger))
                }
            }

            kardexList.add(
                KardexData(
                    promedio,
                    null
                )
            )
        }

        @JavascriptInterface
        fun setCarrera(carrera: String) {
            setPreference(activity, "carrera", carrera)
        }

        @JavascriptInterface
        fun addSemestre(semestre: String) {
            kardexList.add(
                KardexData(
                    semestre,
                    ArrayList()
                )
            )
        }

        @JavascriptInterface
        fun addMateria(data: Array<String>) {
            try {
                kardexList.last().data?.add(
                    MateriaData(
                        data[1].toProperCase(),
                        data[0],
                        data[3],
                        data[4],
                        data[5]
                    )
                )

                if (!isOffline) {
                    kardexDatabase.addMateria(
                        KardexDatabase.Data(
                            kardexList.last().data!!.last().nombre,
                            kardexList.last().semestre,
                            kardexList.last().data!!.last().calif
                        )
                    )
                }
            } catch (e: Exception) {

            }
        }

        @JavascriptInterface
        fun onSemestreCompleted() {

        }

        @JavascriptInterface
        fun onComplete() {
            for (semestre in kardexList) {
                if (semestre.data != null) {
                    if(activity !is SAESActivity) return

                    val holder = (activity as SAESActivity).layoutInflater.inflate(R.layout.item_kardex_content, null, false)
                    items.add(Pair(holder.kardexItemSemestreBtn, holder.kardexItemContent))

                    activity?.runOnUiThread {
                        holder.kardexBtnText.text = semestre.semestre.toProperCase()

                        holder.kardexItemSemestreBtn.setOnClickListener {
                            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                            holder.kardexItemContent.toggle()
                            if (!holder.kardexItemContent.isExpanded) {
                                if (activity != null) {
                                    holder.kardexItemSemestreBtn.findViewById<TextView>(R.id.kardexBtnText)
                                        .setTextColor(
                                            ContextCompat.getColor(activity!!, R.color.colorPrimaryText)
                                        )
                                    holder.kardexItemSemestreBtn.findViewById<ImageView>(R.id.kardexBtnArrow)
                                        .imageTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!, R.color.colorPrimaryText))
                                }
                            } else {
                                if (context != null) {
                                    holder.kardexItemSemestreBtn.findViewById<TextView>(R.id.kardexBtnText)
                                        .setTextColor(
                                            ContextCompat.getColor(
                                                context!!,
                                                R.color.colorHighlight
                                            )
                                        )
                                    holder.kardexItemSemestreBtn.findViewById<ImageView>(R.id.kardexBtnArrow)
                                        .imageTintList = ColorStateList.valueOf(
                                        ContextCompat.getColor(
                                            context!!,
                                            R.color.colorHighlight
                                        )
                                    )
                                }
                            }

                            rotateView(
                                holder.kardexItemSemestreBtn.findViewById(R.id.kardexBtnArrow),
                                !holder.kardexItemContent.isExpanded
                            )
                        }
                    }

                    for (materia in semestre.data) {
                        val v = layoutInflater.inflate(
                            R.layout.item_kardex_content_materia,
                            null,
                            false
                        )
                        activity?.runOnUiThread {
                            v.itemMateriaNombre.text = materia.nombre

                            val calif = materia.calif.toIntOrNull()

                            v.itemMateriaCalif.text = calif?.toString() ?: "-"

                            if(calif != null && activity != null){
                                if(calif < 6){
                                    v.itemMateriaCalif.setTextColor(ContextCompat.getColor(activity!!, R.color.colorHighlight))
                                }
                            }

                            holder.kardexContentLayout.addView(v)
                        }
                    }
                    activity?.runOnUiThread {
                        rootView.kardexMain.addView(holder)
                    }
                }
            }
        }

        @JavascriptInterface
        fun notFound() {

        }

        fun toggleViews() {
            for (i in items) {
                if (expanded) {
                    activity?.runOnUiThread {
                        i.second.collapse()
                        if (activity != null) {
                            i.first.findViewById<TextView>(R.id.kardexBtnText).setTextColor(
                                ContextCompat.getColor(activity!!, R.color.colorPrimaryText)
                            )
                            i.first.findViewById<ImageView>(R.id.kardexBtnArrow).imageTintList =
                                ColorStateList.valueOf(
                                    ContextCompat.getColor(activity!!, R.color.colorPrimaryText)
                                )
                        }
                    }
                } else {
                    activity?.runOnUiThread {
                        i.second.expand()
                        if (context != null) {
                            i.first.findViewById<TextView>(R.id.kardexBtnText).setTextColor(
                                ContextCompat.getColor(
                                    context!!,
                                    R.color.colorHighlight
                                )
                            )
                            i.first.findViewById<ImageView>(R.id.kardexBtnArrow).imageTintList =
                                ColorStateList.valueOf(
                                    ContextCompat.getColor(
                                        context!!,
                                        R.color.colorHighlight
                                    )
                                )
                        }
                    }
                }
                rotateView(i.first.findViewById(R.id.kardexBtnArrow), expanded)
            }
            expanded = !expanded
        }

        private fun rotateView(imageView: ImageView, exp: Boolean) {
            val anim = RotateAnimation(
                180f, 0f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f
            )
            anim.duration = 300

            activity?.runOnUiThread {
                imageView.startAnimation(anim)
                imageView.rotation = if (exp) 0f else 180f
            }
        }
    }
}