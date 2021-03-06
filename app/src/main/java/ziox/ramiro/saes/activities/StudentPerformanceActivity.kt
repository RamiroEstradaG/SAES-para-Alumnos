package ziox.ramiro.saes.activities

import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import ziox.ramiro.saes.R
import ziox.ramiro.saes.databases.*
import ziox.ramiro.saes.databinding.ActivityStudentPerformanceBinding
import ziox.ramiro.saes.fragments.SelectHighSchoolFragment
import ziox.ramiro.saes.utils.*
import kotlin.math.max


const val CHANGE_THRESHOLD = 0.5
/**
 * Creado por Ramiro el 12/15/2018 a las 5:08 PM para SAESv2.
 */
class StudentPerformanceActivity : AppCompatActivity() {
    private val scores = ArrayList<Entry>()
    private val overallScores = ArrayList<Entry>()
    private lateinit var binding: ActivityStudentPerformanceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentPerformanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initTheme(this)
        setSystemUiLightStatusBar(this, false)
        binding.scrollView.addBottomInsetPadding()
        initChart()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if (this.isNetworkAvailable()) {
            initWebView(binding.dataVisualizador, object : WebViewClient() {
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
                                "           window.JSI.addSemester();" +
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
                                "   }" +
                                "}" +
                                "window.JSI.onComplete();"
                    )
                    super.onPageFinished(view, url)
                }
            }, binding.layoutProgressBar)

            binding.dataVisualizador.addJavascriptInterface(JSI(), "JSI")

            binding.dataVisualizador.loadUrl(getUrl(this) + "Alumnos/boleta/kardex.aspx")
        } else {
            val jsi = JSI()
            val db = AppLocalDatabase.getInstance(this).kardexDao()
            val courses = db.getAll()

            var currentSemester = "_"
            for (course in courses){
                if (course.semester != currentSemester) {
                    if (currentSemester != "_") {
                        jsi.onSemesterCompleted()
                    }
                    jsi.addSemester()
                    currentSemester = course.semester
                }

                if (course.courseName != "_") {
                    jsi.addItem(
                        arrayOf(
                            "",
                            course.courseName,
                            course.semester,
                            "",
                            "",
                            course.finalScore
                        )
                    )
                }
            }

            jsi.onSemesterCompleted()
            jsi.onComplete()

            binding.layoutProgressBar.visibility = View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initChart(){
        binding.personalProgressChart.description.text = ""

        binding.personalProgressChart.setDrawBorders(false)
        binding.personalProgressChart.setNoDataText("Esperando datos")
        binding.personalProgressChart.isDoubleTapToZoomEnabled = false
        binding.personalProgressChart.setScaleEnabled(false)
        binding.personalProgressChart.xAxis.axisMinimum = -0.1f
        binding.personalProgressChart.xAxis.setDrawGridLines(false)
        binding.personalProgressChart.xAxis.granularity = 1f

        binding.personalProgressChart.xAxis.axisLineWidth = 2f
        binding.personalProgressChart.xAxis.axisLineColor = ContextCompat.getColor(
            this,
            R.color.colorTextPrimary
        )
        binding.personalProgressChart.xAxis.textColor = ContextCompat.getColor(
            this,
            R.color.colorTextPrimary
        )
        binding.personalProgressChart.xAxis.valueFormatter = IAxisValueFormatter { value, _ -> "${value.toInt() + 1}º" }
        binding.personalProgressChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.personalProgressChart.xAxis.textSize = 14f

        binding.personalProgressChart.axisLeft.granularity = 0.5f
        binding.personalProgressChart.axisLeft.setDrawGridLines(true)
        binding.personalProgressChart.axisLeft.setDrawZeroLine(false)
        binding.personalProgressChart.axisLeft.disableGridDashedLine()
        binding.personalProgressChart.axisLeft.gridLineWidth = 1.5f
        binding.personalProgressChart.axisLeft.setDrawAxisLine(false)
        binding.personalProgressChart.axisLeft.textColor = ContextCompat.getColor(
            this,
            R.color.colorTextPrimary
        )
        binding.personalProgressChart.axisLeft.setLabelCount(2, false)
        binding.personalProgressChart.axisLeft.textSize = 12f

        binding.personalProgressChart.axisRight.isEnabled = false

        binding.personalProgressChart.isDragXEnabled = true
        binding.personalProgressChart.scaleX = 1f
        binding.personalProgressChart.scaleY = 1f

        binding.personalProgressChart.legend.textSize = 12f
        binding.personalProgressChart.legend.textColor = ContextCompat.getColor(
            this,
            R.color.colorTextPrimary
        )

        binding.personalProgressChart.invalidate()
    }

    inner class JSI {
        private var items = 0
        private var totalItems = 0
        private var tmpOverallScore = 0f
        private var lastPeriod = ""

        @JavascriptInterface
        fun addSemester() {
            items = 0
            scores.add(Entry(scores.size.toFloat(), 0f))
        }

        @JavascriptInterface
        fun addItem(data: Array<String>) {
            scores.last().y += data[5].toIntOrNull()?.toFloat() ?: 0f
            tmpOverallScore += data[5].toIntOrNull()?.toFloat() ?: 0f
            items++
            totalItems++
            if(lastPeriod < data[3]){
                lastPeriod = data[3]
            }
        }

        @JavascriptInterface
        fun onSemesterCompleted() {
            if (items > 0) {
                scores.last().y = scores.last().y / items
            }

            overallScores.add(Entry(overallScores.size.toFloat(), tmpOverallScore / totalItems))
        }

        private fun getScoresDataSet() : LineDataSet{
            val scoresDataSet = LineDataSet(scores, "Promedio por semestre")
            scoresDataSet.color =
                ContextCompat.getColor(this@StudentPerformanceActivity, R.color.colorDanger)
            scoresDataSet.valueTextColor = ContextCompat.getColor(
                this@StudentPerformanceActivity,
                R.color.colorDanger
            )
            scoresDataSet.valueTextSize = 10f
            scoresDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            scoresDataSet.lineWidth = 4f
            scoresDataSet.setCircleColor(Color.TRANSPARENT)
            scoresDataSet.circleHoleColor = Color.TRANSPARENT

            return scoresDataSet
        }

        private fun getOverallScoresDataSet() : LineDataSet{
            val promedioDataSet = LineDataSet(overallScores, "Promedio global")
            promedioDataSet.color = ContextCompat.getColor(
                this@StudentPerformanceActivity,
                R.color.colorInfo
            )
            promedioDataSet.setCircleColor(
                ContextCompat.getColor(
                    this@StudentPerformanceActivity,
                    R.color.colorTextPrimary
                )
            )
            promedioDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            promedioDataSet.enableDashedLine(32f, 12f, 1f)
            promedioDataSet.lineWidth = 4f
            promedioDataSet.valueTextSize = 10f
            promedioDataSet.valueTextColor = ContextCompat.getColor(
                this@StudentPerformanceActivity,
                R.color.colorTextPrimary
            )
            return promedioDataSet
        }

        @JavascriptInterface
        fun onComplete() {
            runOnUiThread {
                val scoresDataSet = getScoresDataSet()
                val overallScoresDataSet = getOverallScoresDataSet()

                val isShareStatsEnable = isShareStatsEnable(this@StudentPerformanceActivity)

                if(isShareStatsEnable != 0){
                    binding.shareStatsPermissionCard.visibility = View.GONE

                    if (isShareStatsEnable == 1){
                        initSharedStatsData(overallScores, lastPeriod)
                    }
                }else{
                    binding.shareStatsPermissionCard.visibility = View.VISIBLE
                }

                binding.shareStatsDenyButton.setOnClickListener {
                    binding.shareStatsPermissionCard.visibility = View.GONE
                    setShareStatsEnable(this@StudentPerformanceActivity, -1)
                }

                binding.buttonStatsPermission.setOnClickListener {
                    binding.shareStatsPermissionCard.visibility = View.GONE
                    setShareStatsEnable(this@StudentPerformanceActivity, 1)
                    initSharedStatsData(overallScores, lastPeriod)
                }

                if(scores.size > 0){
                    val entry = scores.maxByOrNull {
                        it.y
                    }
                    val index = scores.indexOf(entry)

                    if(index >= 0){
                        binding.nombreMejorSemestre.text = "${index+1}° Semestre"
                    }else{
                        binding.nombreMejorSemestre.text = "Sin datos"
                    }
                }

                if(scores.size > 0){
                    val entry = scores.minByOrNull {
                        it.y
                    }
                    val index = scores.indexOf(entry)

                    if(index >= 0){
                        binding.nombrePeorSemestre.text = "${index+1}° Semestre"
                    }else{
                        binding.nombrePeorSemestre.text = "Sin datos"
                    }
                }

                if(scores.isNotEmpty()){
                    val last = scores.last()
                    val currentDatabase = AppLocalDatabase.getInstance(this@StudentPerformanceActivity).gradesDao()
                    val data = currentDatabase.getAll()
                    var currentSum = 0.0
                    var notNullElements = 0

                    for (calif in data){
                        currentSum += if(calif.finalScore.toIntOrNull() == null){
                            0
                        }else{
                            notNullElements++
                            calif.finalScore.toInt()
                        }
                    }

                    currentSum /= notNullElements

                    if (notNullElements > 0){
                        binding.ahoraVsUltimo.setTextInPercentageChange(
                            last.y,
                            currentSum.toFloat()
                        )
                    }
                }

                if(scores.isNotEmpty() && overallScores.isNotEmpty()){
                    binding.ultimoVsPromedio.setTextInPercentageChange(
                        overallScores.last().y,
                        scores.last().y
                    )
                }

                if(overallScores.size >= 2){
                    binding.promedioVsElAnterior.setTextInPercentageChange(
                        overallScores[overallScores.size - 2].y,
                        overallScores.last().y
                    )

                    val typedArray = overallScores.map {
                        it.y
                    }.subList(
                        when {
                            overallScores.size >= 4 -> {
                                overallScores.size - 4
                            }
                            overallScores.size == 3 -> {
                                overallScores.size - 3
                            }
                            else -> {
                                0
                            }
                        }, overallScores.size
                    ).toTypedArray()

                    val piv = typedArray.size/2

                    val mean1 = typedArray.copyOfRange(0, piv).mean()
                    val mean2 = typedArray.copyOfRange(piv, typedArray.size).mean()

                    binding.tendenciaLabel.text = when{
                        mean1.minus(mean2) < -CHANGE_THRESHOLD ->{
                            binding.tendenciaLabel.setTextColor(
                                ContextCompat.getColor(
                                    this@StudentPerformanceActivity,
                                    R.color.colorSuccess
                                )
                            )
                            "Al alza"
                        }

                        mean1.minus(mean2) > CHANGE_THRESHOLD -> {
                            binding.tendenciaLabel.setTextColor(
                                ContextCompat.getColor(
                                    this@StudentPerformanceActivity,
                                    R.color.colorDanger
                                )
                            )
                            "A la baja"
                        }

                        else -> {
                            "Lateral"
                        }
                    }
                }

                if(scores.size >= 2){
                    binding.ultimoVsAnterior.setTextInPercentageChange(
                        scores[scores.size - 2].y,
                        scores.last().y
                    )

                    val typedArray = scores.map {
                        it.y
                    }.subList(
                        when {
                            scores.size >= 4 -> {
                                scores.size - 4
                            }
                            scores.size == 3 -> {
                                scores.size - 3
                            }
                            else -> {
                                0
                            }
                        }, scores.size
                    ).toTypedArray()

                    val piv = typedArray.size/2

                    val mean1 = typedArray.copyOfRange(0, piv).mean()
                    val mean2 = typedArray.copyOfRange(piv, typedArray.size).mean()

                    binding.tendenciaCalificacionesLabel.text = when{
                        mean1.minus(mean2) < -CHANGE_THRESHOLD ->{
                            binding.tendenciaCalificacionesLabel.setTextColor(
                                ContextCompat.getColor(
                                    this@StudentPerformanceActivity,
                                    R.color.colorSuccess
                                )
                            )
                            "Al alza"
                        }

                        mean1.minus(mean2) > CHANGE_THRESHOLD ->{
                            binding.tendenciaCalificacionesLabel.setTextColor(
                                ContextCompat.getColor(
                                    this@StudentPerformanceActivity,
                                    R.color.colorDanger
                                )
                            )
                            "A la baja"
                        }

                        else -> {
                            "Lateral"
                        }
                    }
                }



                val dataSets = listOf<ILineDataSet>(scoresDataSet, overallScoresDataSet)
                val maxX = max(scores.size, overallScores.size)
                binding.personalProgressChart.data = LineData(dataSets)
                binding.personalProgressChart.setVisibleXRange(-0.1f, maxX - 0.8f)
                binding.personalProgressChart.data.isHighlightEnabled = false
                binding.personalProgressChart.invalidate()
                binding.mainLayoutPerformance.visibility = View.VISIBLE
            }
        }

        @JavascriptInterface
        fun notFound() {

        }
    }

    private fun initSharedStatsData(overallScores: ArrayList<Entry>, lastPeriod: String){
        if (overallScores.isNotEmpty()){
            getSharedStatsData(lastPeriod, overallScores.last().y)
        }else{
            getSharedStatsData(lastPeriod)
        }
    }

    private fun getSharedStatsData(lastPeriod: String, lastScore: Float = 0f){
        binding.sharedStatsLayout.visibility = View.VISIBLE

        val schoolName = getSchoolName(this)
        val careerName = getCareerName(this)

        initUser(
            this, getBoleta(this), User(
                schoolName,
                getCareerName(this).toProperCase(),
                SelectHighSchoolFragment.highSchoolMap.containsKey(schoolName),
                scores.map { it.y },
                overallScores.map { it.y },
                lastPeriod
            )
        ){
            getStatistics().addOnSuccessListener {
                if(it.data != null){
                    binding.ultimoVsIpn.setTextInPercentageChange(
                        it.data!!["promedio"] as? Double ?: 0.0, lastScore
                    )
                }else{
                    binding.ultimoVsIpn.text = "—%"
                }
            }

            getStatistics(schoolName).addOnSuccessListener {
                if(it.data != null){
                    binding.ultimoVsUnidad.setTextInPercentageChange(
                        it.data!!["promedio"] as? Double ?: 0.0, lastScore
                    )
                }else{
                    binding.ultimoVsUnidad.text = "—%"
                }
            }

            getStatistics(careerName.toProperCase()).addOnSuccessListener {
                if(it.data != null){
                    binding.ultimoVsCarrera.setTextInPercentageChange(
                        it.data!!["promedio"] as? Double ?: 0.0, lastScore
                    )
                }else{
                    binding.ultimoVsCarrera.text = "—%"
                }
            }
        }
    }
}