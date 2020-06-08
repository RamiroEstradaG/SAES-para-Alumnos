package ziox.ramiro.saes.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_calendario_ets.view.*
import org.json.JSONArray
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.utils.*
import ziox.ramiro.saes.views.scheduleview.ScheduleView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Creado por Ramiro el 1/27/2019 a las 8:08 AM para SAESv2.
 */

class CalendarioETSFragment : Fragment() {
    private lateinit var bottomSheet: BottomSheetBehavior<LinearLayout>
    private lateinit var rootView: View
    private lateinit var calendarioETS: WebView
    private val crashlytics = FirebaseCrashlytics.getInstance()

    private val etsMap = HashMap<GregorianCalendar, ArrayList<ScheduleView.EventData>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_calendario_ets, container, false)
        bottomSheet = BottomSheetBehavior.from(rootView.bottomSheetFiltro)
        setLightStatusBar(activity)
        rootView.bottomSheetScrollCalendarioETS.addBottomInsetPadding()

        bottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState){
                    BottomSheetBehavior.STATE_HIDDEN -> (activity as? SAESActivity)?.showDragIcon()
                    else -> (activity as? SAESActivity)?.hideDragIcon()
                }
            }

        })

        bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN

        rootView.spinnerTurno.isKeepIndexEnable = true

        (activity as? SAESActivity)?.setOnDragHorizontaly {
            if (it){
                rootView.scheduleETS.scrollToPrev()
            }else{
                rootView.scheduleETS.scrollToNext()
            }
        }

        (activity as? SAESActivity)?.showFab(
            R.drawable.ic_filter_list_white_24dp,
            View.OnClickListener {
                crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
                if (bottomSheet.state == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
                } else {
                    bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
                }
            },
            BottomAppBar.FAB_ALIGNMENT_MODE_END
        )

        calendarioETS = createWebView(activity, object : WebViewClient() {
            private var isPeriodoSelected = false

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (isPeriodoSelected) {
                    view?.loadUrl(
                        "javascript:" +
                                "var els = document.getElementById(\"ctl00_mainCopy_dpdTipoETSactual\").getElementsByTagName(\"option\");" +
                                "var tipo = [];" +
                                "for(var i = 0 ; i < els.length ; ++i){" +
                                "    tipo.push(els[i].innerText);" +
                                "}" +
                                "var els2 = document.getElementById(\"ctl00_mainCopy_dpdcarrera\").getElementsByTagName(\"option\");" +
                                "var carr = [];" +
                                "for(var i = 0 ; i < els2.length ; ++i){" +
                                "    carr.push(els2[i].innerText);" +
                                "}" +
                                "var els3 = document.getElementById(\"ctl00_mainCopy_dpdplan\").getElementsByTagName(\"option\");" +
                                "var plan = [];" +
                                "for(var i = 0 ; i < els3.length ; ++i){" +
                                "    plan.push(els3[i].innerText);" +
                                "}" +
                                "var els4 = document.getElementById(\"ctl00_mainCopy_dpdespecialidad\").getElementsByTagName(\"option\");" +
                                "var espe = [];" +
                                "for(var i = 0 ; i < els4.length ; ++i){" +
                                "    espe.push(els4[i].innerText);" +
                                "}" +
                                "var els5 = document.getElementById(\"ctl00_mainCopy_dpdSemestre\").getElementsByTagName(\"option\");" +
                                "var seme = [];" +
                                "for(var i = 0 ; i < els5.length ; ++i){" +
                                "    seme.push(els5[i].innerText);" +
                                "}" +
                                "var els6 = document.getElementById(\"ctl00_mainCopy_DpdTurno\").getElementsByTagName(\"option\");" +
                                "var turno = [];" +
                                "for(var i = 0 ; i < els6.length ; ++i){" +
                                "    turno.push(els6[i].innerText);" +
                                "}" +
                                "window.JSI.loadSpinner(JSON.stringify(tipo), JSON.stringify(carr), JSON.stringify(plan), JSON.stringify(espe), JSON.stringify(seme), JSON.stringify(turno));" +
                                "" +
                                "if(document.getElementById(\"ctl00_mainCopy_grvcalendario\") != null){" +
                                "   window.JSI.clear();" +
                                "   let rows = document.getElementById(\"ctl00_mainCopy_grvcalendario\").getElementsByTagName(\"tr\");" +
                                "   for(let i = 1 ; i < rows.length ; i++){" +
                                "       let cols = rows[i].getElementsByTagName(\"td\");" +
                                "       window.JSI.addMateria(cols[3].innerText, cols[1].innerText, cols[5].innerText, cols[6].innerText, cols[4].innerText);" +
                                "   }" +
                                "   window.JSI.onCalendarioFinish();" +
                                "}"
                    )
                } else {
                    isPeriodoSelected = true
                    view?.loadUrl(
                        "javascript: " +
                                "var els = document.getElementById(\"ctl00_mainCopy_dpdperiodoActual\").getElementsByTagName(\"option\");" +
                                "var v1 = [];" +
                                "for(var i = 0 ; i < els.length ; ++i){" +
                                "    v1.push(els[i].innerText);" +
                                "}" +
                                "document.getElementById(\"ctl00_mainCopy_dpdperiodoActual\").selectedIndex = window.JSI.getLastPeriodo(JSON.stringify(v1));" +
                                "__doPostBack('ctl00\$mainCopy\$dpdperiodoActual','');"
                    )
                }
            }
        }, (activity as SAESActivity?)?.getProgressBar())

        calendarioETS.addJavascriptInterface(JSI(), "JSI")
        calendarioETS.loadUrl(getUrl(activity) + "Academica/Calendario_ets.aspx")

        return rootView
    }

    inner class JSI {
        private var canUpdateTipo = true
        private var canUpdateCarrera = true
        private var canUpdatePlan = true
        private var canUpdateEspecialidad = true
        private var canUpdateSemestre = true
        private var canUpdateTurno = true

        var isLoading = true

        @JavascriptInterface
        @SuppressLint("SetTextI18n")
        fun getLastPeriodo(p: String): Int {

            val arr = JSONArray(p)
            var res = -1
            var maxVal = 0.0

            for (i in 0 until arr.length()) {
                val periodo = arr.getString(i).split(Regex("20"), 2)
                if (periodo.size == 2) {
                    if (periodo[1].toInt() + (mesToInt(periodo[0]) / 12.0) > maxVal) {
                        res = i
                        maxVal = periodo[1].toInt() + (mesToInt(periodo[0]) / 12.0)
                    }
                }
            }

            activity?.runOnUiThread {
                rootView.labelPeriodo.text =
                    MES_COMPLETO[((maxVal % 1) * 12).toInt()] + " del 20" + maxVal.toInt()
            }

            return res
        }

        @JavascriptInterface
        fun loadSpinner(
            tipoEts: String,
            carrera: String,
            plan: String,
            especialidad: String,
            semestre: String,
            turno: String
        ) {

            val tip = JSONArray(tipoEts)
            val car = JSONArray(carrera)
            val pla = JSONArray(plan)
            val esp = JSONArray(especialidad)
            val sem = JSONArray(semestre)
            val tur = JSONArray(turno)

            val ti = Array(tip.length()) {
                tip.getString(it)
            }
            val c = Array(car.length()) {
                car.getString(it).toProperCase()
            }
            val p = Array(pla.length()) {
                pla.getString(it)
            }
            val e = Array(esp.length()) {
                esp.getString(it).toProperCase()
            }
            val s = Array(sem.length()) {
                sem.getString(it)
            }
            val tu = Array(tur.length()) {
                tur.getString(it).toProperCase()
            }

            activity?.runOnUiThread {
                if (canUpdateTipo) {
                    rootView.spinnerTipoETS.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateCarrera = true
                            canUpdatePlan = true
                            canUpdateEspecialidad = true
                            canUpdateSemestre = true
                            canUpdateTurno = true

                            canUpdateTipo = false

                            if (!isLoading) {
                                calendarioETS.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdTipoETSactual\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdTipoETSactual','');"
                                )
                                isLoading = true
                            }
                        }

                    }
                    rootView.spinnerTipoETS.setOptions(ti)
                }

                if (canUpdateCarrera) {
                    rootView.spinnerCarrera.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdatePlan = true
                            canUpdateEspecialidad = true
                            canUpdateSemestre = true
                            canUpdateTurno = true

                            canUpdateCarrera = false

                            if (!isLoading) {
                                calendarioETS.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdcarrera\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdcarrera','');"
                                )
                                isLoading = true
                            }
                        }

                    }
                    rootView.spinnerCarrera.setOptions(c)
                }

                if (canUpdatePlan) {
                    rootView.spinnerPlanDeEstudios.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateEspecialidad = true
                            canUpdateSemestre = true
                            canUpdateTurno = true

                            canUpdatePlan = false

                            if (!isLoading) {
                                calendarioETS.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdplan\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdplan','');"
                                )
                                isLoading = true
                            }
                        }
                    }
                    rootView.spinnerPlanDeEstudios.setOptions(p)
                }

                if (canUpdateEspecialidad) {
                    rootView.spinnerEspecialidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateSemestre = true
                            canUpdateTurno = true

                            canUpdateEspecialidad = false

                            if (!isLoading) {
                                calendarioETS.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdespecialidad\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdespecialidad','');"
                                )
                                isLoading = true
                            }
                        }
                    }
                    rootView.spinnerEspecialidad.setOptions(e)
                }

                if (canUpdateSemestre) {
                    rootView.spinnerSemestre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateTurno = true

                            canUpdateSemestre = false

                            if (!isLoading) {
                                calendarioETS.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_dpdSemestre\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$dpdSemestre','');"
                                )
                                isLoading = true
                            }
                        }
                    }
                    rootView.spinnerSemestre.setOptions(s)
                }

                if (canUpdateTurno) {
                    rootView.spinnerTurno.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            canUpdateTurno = false

                            if (!isLoading) {
                                calendarioETS.loadUrl(
                                    "javascript: document.getElementById(\"ctl00_mainCopy_DpdTurno\").selectedIndex = $position;" +
                                            "__doPostBack('ctl00\$mainCopy\$DpdTurno','');"
                                )
                                isLoading = true
                            }
                        }

                    }
                    rootView.spinnerTurno.setOptions(tu)
                }

                isLoading = false
            }
        }

        @JavascriptInterface
        fun addMateria(
            fecha: String,
            materia: String,
            edificio: String,
            salon: String,
            hora: String
        ) {
            val fechaSplit = fecha.split(" ")

            val horaSplit = hora.substring(0..hora.length-3).split(":")
            val hour = horaSplit.first().toInt()
            val minutos = horaSplit.last().toInt()
            val meridian = if(hora.substring(hora.length-2 until hora.length) == "PM" && hour < 12){
                12
            }else{
                0
            }

            val key = GregorianCalendar(
                fechaSplit[2].toInt(),
                MES.indexOf(fechaSplit[0].toUpperCase(Locale.ROOT)),
                fechaSplit[1].toInt(),
                hour+meridian,
                minutos,
                0
            )

            if(!etsMap.containsKey(key)){
                etsMap[key] = ArrayList()
            }

            etsMap[key]?.add(ScheduleView.EventData(materia.toProperCase(), edificio, salon))
        }

        @JavascriptInterface
        fun onCalendarioFinish() {
            activity?.runOnUiThread {
                val keys = etsMap.keys.toTypedArray()
                keys.sortBy {
                    it.timeInMillis
                }

                var dia = -1
                var hour = -1.0
                for(k in keys){
                    val currentDia = k.get(Calendar.DAY_OF_YEAR)
                    val currentHour = k.get(Calendar.HOUR_OF_DAY)+(k.get(Calendar.MINUTE)/60.0)
                    if(currentDia != dia){
                        rootView.scheduleETS.addDay("${k.get(Calendar.DAY_OF_MONTH)} de ${MES_COMPLETO[k.get(Calendar.MONTH)]}")
                        dia = currentDia
                        hour = -1.0
                    }

                    if(currentHour != hour){
                        var horaString = k.get(Calendar.HOUR)
                        if(horaString == 0) horaString = 12
                        rootView.scheduleETS.addHour("${horaString.toString().padStart(2,'0')}:${k.get(Calendar.MINUTE).toString().padStart(2, '0')}", if(k.get(Calendar.AM_PM) == Calendar.AM)"A.M." else "P.M.")
                        hour = currentHour
                    }

                    for(e in etsMap[k]!!){
                        rootView.scheduleETS.addEvent(e)
                    }
                }

                rootView.scheduleETS.notifyDataSetChanged()
            }
        }

        @JavascriptInterface
        fun clear() {
            etsMap.clear()
            activity?.runOnUiThread {
                rootView.scheduleETS.clear()
            }
        }
    }
}