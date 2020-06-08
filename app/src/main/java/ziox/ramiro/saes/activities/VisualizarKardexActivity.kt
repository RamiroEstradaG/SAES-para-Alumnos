package ziox.ramiro.saes.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.android.synthetic.main.activity_visualizar_kardex.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.fragments.SelectSchoolNivelMedioSuperiorFragment
import ziox.ramiro.saes.sql.CalificacionesDatabase
import ziox.ramiro.saes.sql.KardexDatabase
import ziox.ramiro.saes.utils.*
import kotlin.math.absoluteValue
import kotlin.math.max

/**
 * Creado por Ramiro el 12/15/2018 a las 5:08 PM para SAESv2.
 */
@Suppress("UNCHECKED_CAST")
class VisualizarKardexActivity : AppCompatActivity() {
    private val calificaciones = ArrayList<Entry>()
    private val promedio = ArrayList<Entry>()
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_kardex)
        initTheme(this)
        setLightStatusBar(this)
        rendimientoScroll.addBottomInsetPadding()

        toolbar.setNavigationOnClickListener {
            crashlytics.log("Click en BackButton en la clase ${this.localClassName}")
            finish()
        }

        val description = Description()
        description.text = "Calificaciones a través del tiempo"
        description.textColor = ContextCompat.getColor(this, R.color.colorPrimaryText)
        chartKardex.description = description

        chartKardex.setDrawBorders(false)
        chartKardex.setNoDataText("Esperando datos")
        chartKardex.isDoubleTapToZoomEnabled = false
        chartKardex.setScaleEnabled(false)

        chartKardex.xAxis.setDrawGridLines(false)
        chartKardex.xAxis.granularity = 1f
        chartKardex.xAxis.textColor = ContextCompat.getColor(this, R.color.colorPrimaryText)
        chartKardex.xAxis.valueFormatter =
            IAxisValueFormatter { value, _ -> "${value.toInt() + 1}º Semestre" }

        chartKardex.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chartKardex.xAxis.labelRotationAngle = 33f

        chartKardex.axisLeft.granularity = 0.5f
        chartKardex.axisLeft.setDrawZeroLine(true)
        chartKardex.axisLeft.textColor = ContextCompat.getColor(this, R.color.colorPrimaryText)
        chartKardex.axisLeft.setLabelCount(2, false)
        chartKardex.axisLeft.enableGridDashedLine(12f, 12f, 1f)

        chartKardex.axisRight.isEnabled = false

        chartKardex.isDragXEnabled = true
        chartKardex.scaleX = 1f
        chartKardex.scaleY = 1f

        chartKardex.legend.textSize = 14f
        chartKardex.legend.textColor = ContextCompat.getColor(this, R.color.colorPrimaryText)

        chartKardex.invalidate()

        if (this.isNetworkAvailable()) {
            initWebView(dataVisualizador, object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    view?.loadUrl(
                        "javascript: var table = document.getElementById(\"ctl00_mainCopy_Lbl_Kardex\").getElementsByTagName(\"tbody\");\n" +
                                "if(table == null)" +
                                "window.JSI.notFound();" +
                                "else{" +
                                "   for(var i = 0 ; i < table.length ; i++){" +
                                "       var row = table[i].getElementsByTagName(\"tr\");" +
                                "       var titulo = row[0].innerText;" +
                                "       if(titulo.toLowerCase().match(/semestre\$/g).length > 0){" +
                                "           window.JSI.addSemestre();" +
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
                                "   }" +
                                "}" +
                                "window.JSI.onComplete();"
                    )
                    super.onPageFinished(view, url)
                }
            }, progressVisualizador)

            dataVisualizador.addJavascriptInterface(JSI(), "JSI")

            dataVisualizador.loadUrl(getUrl(this) + "Alumnos/boleta/kardex.aspx")
        } else {
            val jsi = JSI()
            val db = KardexDatabase(this)
            db.createTable()
            val data = db.getAll()

            var currentSemestre = "_"
            while (data.moveToNext()) {
                val v = KardexDatabase.cursorAsData(data)
                if (v.semestre != currentSemestre) {
                    if (currentSemestre != "_") {
                        jsi.onSemestreCompleted()
                    }
                    jsi.addSemestre()
                    currentSemestre = v.semestre
                }

                if (v.name != "_") {
                    jsi.addMateria(arrayOf("", v.name, v.semestre, "", "", v.calificacion))
                }
            }

            jsi.onSemestreCompleted()
            jsi.onComplete()

            progressVisualizador.visibility = View.GONE

            data.close()
        }
    }

    inner class JSI {
        private var materias = 0
        private var materiasTotal = 0
        private var promedioGlobal = 0f
        private var lastPeriodo = ""

        @JavascriptInterface
        fun addSemestre() {
            materias = 0
            calificaciones.add(Entry(calificaciones.size.toFloat(), 0f))
        }

        @JavascriptInterface
        fun addMateria(data: Array<String>) {
            calificaciones.last().y += data[5].toIntOrNull()?.toFloat() ?: 0f
            promedioGlobal += data[5].toIntOrNull()?.toFloat() ?: 0f
            materias++
            materiasTotal++
            if(lastPeriodo < data[3]){
                lastPeriodo = data[3]
            }
        }

        @JavascriptInterface
        fun onSemestreCompleted() {
            if (materias > 0) {
                calificaciones.last().y = calificaciones.last().y / materias
            }

            promedio.add(Entry(promedio.size.toFloat(), promedioGlobal / materiasTotal))
        }

        @JavascriptInterface
        fun onComplete() {
            runOnUiThread {
                val calificacionesDataSet = LineDataSet(calificaciones, "Promedio por semestre")
                calificacionesDataSet.color =
                    ContextCompat.getColor(this@VisualizarKardexActivity, R.color.colorHighlight)
                calificacionesDataSet.valueTextColor = ContextCompat.getColor(this@VisualizarKardexActivity, R.color.colorHighlight)
                calificacionesDataSet.valueTextSize = 14f
                calificacionesDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                calificacionesDataSet.lineWidth = 2f
                calificacionesDataSet.setCircleColor(Color.TRANSPARENT)
                calificacionesDataSet.circleHoleColor = Color.TRANSPARENT

                if(isShareStatsEnable(this@VisualizarKardexActivity)){
                    cardStatsPermissions.visibility = View.GONE
                    evalSharedStats(promedio, lastPeriodo)
                }else{
                    cardStatsPermissions.visibility = View.VISIBLE
                }

                buttonStatsPermission.setOnClickListener {
                    cardStatsPermissions.visibility = View.GONE
                    setShareStatsEnable(this@VisualizarKardexActivity, true)
                    evalSharedStats(promedio, lastPeriodo)
                }

                if(calificaciones.size > 0){
                    val entry = calificaciones.maxBy {
                        it.y
                    }
                    val index = calificaciones.indexOf(entry)

                    if(index >= 0){
                        nombreMejorSemestre.text = "${index+1}° Semestre"
                        promedioMejorSemestre.text = (entry?.y?.toDouble() ?: 0.0).toStringPresition(1).toString()
                    }else{
                        nombreMejorSemestre.text = "Sin datos"
                    }
                }

                if(calificaciones.size > 0){
                    val entry = calificaciones.minBy {
                        it.y
                    }
                    val index = calificaciones.indexOf(entry)

                    if(index >= 0){
                        nombrePeorSemestre.text = "${index+1}° Semestre"
                        promedioPeorSemestre.text = (entry?.y?.toDouble()?:0.0).toStringPresition(1).toString()
                    }else{
                        nombrePeorSemestre.text = "Sin datos"
                    }
                }

                if(calificaciones.isNotEmpty()){
                    val last = calificaciones.last()
                    val currentDatabase = CalificacionesDatabase(this@VisualizarKardexActivity)
                    currentDatabase.createTable()
                    val data = currentDatabase.getAll()
                    var current = 0.0
                    var notNull = 0

                    while(data.moveToNext()){
                        val calif = CalificacionesDatabase.cursorAsClaseData(data)
                        current += if(calif.final.toIntOrNull() == null){
                            0
                        }else{
                            notNull++
                            calif.final.toInt()
                        }
                    }

                    current /= notNull

                    data.close()
                    currentDatabase.close()

                    if (notNull > 0){
                        val diference = current - last.y

                        ahoraVsUltimo.text = when {
                            diference > 0 -> {
                                ahoraVsUltimo.setTextColor(ContextCompat.getColor(this@VisualizarKardexActivity, R.color.colorSuccess))
                                "+${(100*diference.absoluteValue/current).toStringPresition(1)}%"
                            }
                            diference < 0 -> {
                                ahoraVsUltimo.setTextColor(ContextCompat.getColor(this@VisualizarKardexActivity, R.color.colorHighlight))
                                "-${(100*diference.absoluteValue/current).toStringPresition(1)}%"
                            }
                            else -> "0.0%"
                        }
                    }
                }

                if(calificaciones.size >= 2){
                    val last = calificaciones.last()
                    val prev = calificaciones[calificaciones.size-2]

                    val diference = last.y - prev.y

                    ultimoVsAnterior.text = when {
                        diference > 0 -> {
                            ultimoVsAnterior.setTextColor(ContextCompat.getColor(this@VisualizarKardexActivity, R.color.colorSuccess))
                            "+${(100*diference.absoluteValue/calificaciones.last().y.toDouble()).toStringPresition(1)}%"
                        }
                        diference < 0 -> {
                            ultimoVsAnterior.setTextColor(ContextCompat.getColor(this@VisualizarKardexActivity, R.color.colorHighlight))
                            "-${(100*diference.absoluteValue/calificaciones.last().y.toDouble()).toStringPresition(1)}%"
                        }
                        else -> "0.0%"
                    }
                }

                if(promedio.size >= 2){
                    val last = promedio.last()
                    val prev = promedio[promedio.size-2]

                    val diference = last.y - prev.y

                    promedioVsElAnterior.text = when {
                        diference > 0 -> {
                            promedioVsElAnterior.setTextColor(ContextCompat.getColor(this@VisualizarKardexActivity, R.color.colorSuccess))
                            "+${(100*diference.absoluteValue/promedio.last().y.toDouble()).toStringPresition(1)}%"
                        }
                        diference < 0 -> {
                            promedioVsElAnterior.setTextColor(ContextCompat.getColor(this@VisualizarKardexActivity, R.color.colorHighlight))
                            "-${(100*diference.absoluteValue/promedio.last().y.toDouble()).toStringPresition(1)}%"
                        }
                        else -> "0.0%"
                    }
                }

                if(promedio.size >= 2){
                    val typedArray = promedio.map {
                        it.y
                    }.subList(
                        when {
                            promedio.size >= 4 -> {
                                promedio.size-4
                            }
                            promedio.size == 3 -> {
                                promedio.size-3
                            }
                            else -> {
                                0
                            }
                        }, promedio.size).toTypedArray()

                    val piv = typedArray.size/2

                    val mean1 = typedArray.copyOfRange(0, piv).mean()
                    val mean2 = typedArray.copyOfRange(piv, typedArray.size).mean()

                    tendenciaLabel.text = when{
                        mean1.minus(mean2) < -0.05 ->{
                            tendenciaLabel.setTextColor(ContextCompat.getColor(this@VisualizarKardexActivity, R.color.colorSuccess))
                            "Al alza"
                        }

                        mean1.minus(mean2) > 0.05 -> {
                            tendenciaLabel.setTextColor(ContextCompat.getColor(this@VisualizarKardexActivity, R.color.colorHighlight))
                            "A la baja"
                        }

                        else -> {
                            "Lateral"
                        }
                    }
                }

                if(calificaciones.size >= 2){
                    val typedArray = calificaciones.map {
                        it.y
                    }.subList(when {
                        calificaciones.size >= 4 -> {
                            calificaciones.size-4
                        }
                        calificaciones.size == 3 -> {
                            calificaciones.size-3
                        }
                        else -> {
                            0
                        }
                    }, calificaciones.size).toTypedArray()

                    val piv = typedArray.size/2

                    val mean1 = typedArray.copyOfRange(0, piv).mean()
                    val mean2 = typedArray.copyOfRange(piv, typedArray.size).mean()

                    tendenciaCalificacionesLabel.text = when{
                        mean1.minus(mean2) < -0.05 ->{
                            tendenciaCalificacionesLabel.setTextColor(ContextCompat.getColor(this@VisualizarKardexActivity, R.color.colorSuccess))
                            "Al alza"
                        }

                        mean1.minus(mean2) > 0.05 ->{
                            tendenciaCalificacionesLabel.setTextColor(ContextCompat.getColor(this@VisualizarKardexActivity, R.color.colorHighlight))
                            "A la baja"
                        }

                        else -> {
                            "Lateral"
                        }
                    }
                }

                if(calificaciones.isNotEmpty() && promedio.isNotEmpty()){
                    val diference = calificaciones.last().y - promedio.last().y

                    ultimoVsPromedio.text = when {
                        diference > 0 -> {
                            ultimoVsPromedio.setTextColor(ContextCompat.getColor(this@VisualizarKardexActivity, R.color.colorSuccess))
                            "+${(100*diference.absoluteValue/calificaciones.last().y.toDouble()).toStringPresition(1)}%"
                        }
                        diference < 0 -> {
                            ultimoVsPromedio.setTextColor(ContextCompat.getColor(this@VisualizarKardexActivity, R.color.colorHighlight))
                            "-${(100*diference.absoluteValue/calificaciones.last().y.toDouble()).toStringPresition(1)}%"
                        }
                        else -> "0.0%"
                    }
                }

                val promedioDataSet = LineDataSet(promedio, "Promedio global")
                promedioDataSet.color = ContextCompat.getColor(this@VisualizarKardexActivity, R.color.colorInfo)
                promedioDataSet.setCircleColor(ContextCompat.getColor(this@VisualizarKardexActivity, R.color.colorPrimaryText))
                promedioDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                promedioDataSet.enableDashedLine(24f, 12f, 1f)
                promedioDataSet.lineWidth = 2f
                promedioDataSet.valueTextSize = 14f
                promedioDataSet.valueTextColor = ContextCompat.getColor(this@VisualizarKardexActivity, R.color.colorPrimaryText)

                val dataSets = listOf<ILineDataSet>(calificacionesDataSet, promedioDataSet)
                chartKardex.data = LineData(dataSets)

                chartKardex.invalidate()

                val maxX = max(calificaciones.size, promedio.size)

                chartKardex.setVisibleXRange(0f, maxX-0.3f)
                chartKardex.data.isHighlightEnabled = false


                mainLayoutPerformance.visibility = View.VISIBLE
            }
        }

        @JavascriptInterface
        fun notFound() {

        }
    }

    private fun evalSharedStats(promedio : ArrayList<Entry>, lastPeriodo : String){
        if (promedio.isNotEmpty()){
            initSharedStats(lastPeriodo, promedio.last().y)
        }else{
            initSharedStats(lastPeriodo)
        }
    }

    private fun initSharedStats(lastPeriodo : String, lastPromedio : Float = 0f){
        sharedStatsLayout.visibility = View.VISIBLE

        val unidad = getNameEscuela(this)
        val carrera = getCarrera(this)

        initUser(this, getBoleta(this), User(
            unidad,
            getCarrera(this).toProperCase(),
            SelectSchoolNivelMedioSuperiorFragment.medioSuperiorMap.containsKey(unidad),
            calificaciones.map { it.y },
            promedio.map { it.y },
            lastPeriodo
        )){
            getStatistics().addOnSuccessListener {
                if(it.data != null){
                    val diference = lastPromedio.minus(it.data!!["promedio"] as? Double ?: 0.0)

                    ultimoVsIpn.text = when {
                        diference > 0 -> {
                            ultimoVsIpn.setTextColor(ContextCompat.getColor(this, R.color.colorSuccess))
                            "+${(100*diference.absoluteValue/lastPromedio).toStringPresition(1)}%"
                        }
                        diference < 0 -> {
                            ultimoVsIpn.setTextColor(ContextCompat.getColor(this, R.color.colorHighlight))
                            "-${(100*diference.absoluteValue/lastPromedio).toStringPresition(1)}%"
                        }
                        else -> "0.0%"
                    }
                }else{
                    ultimoVsIpn.text = "—%"
                }
            }

            getStatistics(unidad).addOnSuccessListener {
                if(it.data != null){
                    val diference = lastPromedio.minus(it.data!!["promedio"] as? Double ?: 0.0)

                    ultimoVsUnidad.text = when {
                        diference > 0 -> {
                            ultimoVsUnidad.setTextColor(ContextCompat.getColor(this, R.color.colorSuccess))
                            "+${(100*diference.absoluteValue/lastPromedio).toStringPresition(1)}%"
                        }
                        diference < 0 -> {
                            ultimoVsUnidad.setTextColor(ContextCompat.getColor(this, R.color.colorHighlight))
                            "-${(100*diference.absoluteValue/lastPromedio).toStringPresition(1)}%"
                        }
                        else -> "0.0%"
                    }
                }else{
                    ultimoVsUnidad.text = "—%"
                }
            }

            getStatistics(carrera.toProperCase()).addOnSuccessListener {
                if(it.data != null){
                    val diference = lastPromedio.minus(it.data!!["promedio"] as? Double ?: 0.0)

                    ultimoVsCarrera.text = when {
                        diference > 0 -> {
                            ultimoVsCarrera.setTextColor(ContextCompat.getColor(this, R.color.colorSuccess))
                            "+${(100*diference.absoluteValue/lastPromedio).toStringPresition(1)}%"
                        }
                        diference < 0 -> {
                            ultimoVsCarrera.setTextColor(ContextCompat.getColor(this, R.color.colorHighlight))
                            "-${(100*diference.absoluteValue/lastPromedio).toStringPresition(1)}%"
                        }
                        else -> "0.0%"
                    }
                }else{
                    ultimoVsCarrera.text = "—%"
                }
            }
        }
    }
}