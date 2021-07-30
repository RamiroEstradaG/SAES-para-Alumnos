package ziox.ramiro.saes.features.saes.features.performance.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingFlat
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.data.repositories.UserFirebaseRepository
import ziox.ramiro.saes.features.saes.features.grades.data.repositories.GradesWebViewRepository
import ziox.ramiro.saes.features.saes.features.grades.view_models.GradesViewModel
import ziox.ramiro.saes.features.saes.features.home.ui.components.gradeColor
import ziox.ramiro.saes.features.saes.features.kardex.data.models.KardexData
import ziox.ramiro.saes.features.saes.features.kardex.data.repositories.KardexWebViewRepository
import ziox.ramiro.saes.features.saes.features.kardex.view_models.KardexViewModel
import ziox.ramiro.saes.features.saes.features.performance.data.models.TriStateBoolean
import ziox.ramiro.saes.features.saes.features.performance.data.repositories.PerformanceFirebaseRepository
import ziox.ramiro.saes.features.saes.features.performance.view_models.PerformanceViewModel
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.MES_COMPLETO
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.toStringPrecision
import kotlin.math.absoluteValue
import kotlin.math.max


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Performance(
    kardexViewModel: KardexViewModel = viewModel(
        factory = viewModelFactory { KardexViewModel(KardexWebViewRepository(LocalContext.current)) }
    ),
    gradesViewModel: GradesViewModel = viewModel(
        factory = viewModelFactory { GradesViewModel(GradesWebViewRepository(LocalContext.current)) }
    ),
    performanceViewModel: PerformanceViewModel = viewModel(
        factory = viewModelFactory {
            PerformanceViewModel(
                PerformanceFirebaseRepository(),
                KardexWebViewRepository(LocalContext.current),
                UserFirebaseRepository()
            )
        }
    )
) = Crossfade(targetState = kardexViewModel.kardexData.value) {
    if(it != null){
        performanceViewModel.checkPerformancePermissions(LocalContext.current)

        Box(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(
                    top = 16.dp,
                    start = 32.dp,
                    end = 32.dp,
                    bottom = 64.dp
                )
            ) {
                AnimatedVisibility(visible = performanceViewModel.permissionToSaveData.value == TriStateBoolean.UNSET) {
                    PermissionCard(performanceViewModel)
                }

                if (it.kardexPeriods.size >= 2){
                    OtherStatisticsCard(
                        title = "Mejor semestre hasta el momento",
                        value = it.kardexPeriods.maxByOrNull {period -> period.average }?.periodName ?: "",
                        backgroundColor = getCurrentTheme().info,
                        contentColor = MaterialTheme.colors.onPrimary
                    )
                    OtherStatisticsCard(
                        title = "Peor semestre hasta el momento",
                        value = it.kardexPeriods.minByOrNull {period -> period.average }?.periodName ?: "",
                        backgroundColor = getCurrentTheme().danger,
                        contentColor = MaterialTheme.colors.onPrimary
                    )
                }
                AnimatedVisibility(visible = performanceViewModel.careerPerformance.value != null) {
                    performanceViewModel.careerPerformance.value?.let { data ->
                        ComparePerformanceCard(
                            title1 = data.name,
                            value1 = data.average,
                            title2 = "Tu promedio",
                            value2 = it.generalScore,
                            sizeOfData = data.sizeOfData,
                            monthUpdated = data.lastUpdate
                        )
                    }
                }

                AnimatedVisibility(visible = performanceViewModel.schoolPerformance.value != null) {
                    performanceViewModel.schoolPerformance.value?.let { data ->
                        ComparePerformanceCard(
                            title1 = data.name,
                            value1 = data.average,
                            title2 = "Tu promedio",
                            value2 = it.generalScore,
                            sizeOfData = data.sizeOfData,
                            monthUpdated = data.lastUpdate
                        )
                    }
                }


                AnimatedVisibility(visible = performanceViewModel.generalPerformance.value != null) {
                    performanceViewModel.generalPerformance.value?.let { data ->
                        ComparePerformanceCard(
                            title1 = data.name,
                            value1 = data.average,
                            title2 = "Tu promedio",
                            value2 = it.generalScore,
                            sizeOfData = data.sizeOfData,
                            monthUpdated = data.lastUpdate
                        )
                    }
                }
                if (gradesViewModel.grades.value != null){
                    gradesViewModel.grades.value?.let { gradesValue ->
                        val grades = gradesValue.mapNotNull { classGrades -> classGrades.finalScore }
                        val gradesAverage = if(grades.isNotEmpty()){
                            grades.sum().div(grades.size.toDouble())
                        }else null
                        ComparePerformanceCard(
                            title2 = "Semestre en curso",
                            value2 = gradesAverage,
                            title1 = it.kardexPeriods.last().periodName,
                            value1 = it.kardexPeriods.last().average
                        )
                    }
                }
                if (it.kardexPeriods.size >= 2){
                    ComparePerformanceCard(
                        title2 = it.kardexPeriods.last().periodName,
                        value2 = it.kardexPeriods.last().average,
                        title1 = it.kardexPeriods[it.kardexPeriods.lastIndex - 1].periodName,
                        value1 = it.kardexPeriods[it.kardexPeriods.lastIndex - 1].average
                    )
                    ComparePerformanceCard(
                        title2 = "Promedio general",
                        value2 = it.generalScore,
                        title1 = "Promedio anterior",
                        value1 = it.generalScoreAt(it.kardexPeriods.lastIndex - 1)
                    )
                }
                if (it.kardexPeriods.isNotEmpty()){
                    ComparePerformanceCard(
                        title2 = it.kardexPeriods.last().periodName,
                        value2 = it.kardexPeriods.last().average,
                        title1 = "Promedio general",
                        value1 = it.generalScore
                    )
                }
                KardexChart(it)
                if(it.kardexPeriods.size >= 2){
                    TendencyCard(
                        title = "Tendencia del promedio por semestre",
                        values = it.kardexPeriods.map { period -> period.average }
                    )
                    TendencyCard(
                        title = "Tendencia del promedio general",
                        values = it.kardexPeriods.mapIndexed {i, _ -> it.generalScoreAt(i) }
                    )
                }
            }
        }

    }else{
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
    ErrorSnackbar(performanceViewModel.error)
}




@Composable
fun ComparePerformanceCard(
    title1: String,
    value1: Double?,
    title2: String,
    value2: Double?,
    sizeOfData: Int? = null,
    monthUpdated: Int? = null
) = Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
    elevation = 0.dp
) {
    Column {
        Row {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title1,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value1?.toStringPrecision(2) ?: "-",
                    style = MaterialTheme.typography.h4,
                    color = gradeColor(value1?.toInt())
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title2,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value2?.toStringPrecision(2) ?: "-",
                    style = MaterialTheme.typography.h4,
                    color = gradeColor(value2?.toInt())
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            if(sizeOfData != null){
                Text(
                    text = "S=${sizeOfData}",
                    style = MaterialTheme.typography.caption
                )
            }

            if(monthUpdated != null){
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = "Actualizado en ${MES_COMPLETO[monthUpdated]}",
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        val difference = if(value1 != null && value2 != null){
            (value2 - value1) / value1
        }else null

        val sign = when{
            difference == null || difference == 0.0 -> ""
            difference < 0 -> "-"
            else -> "+"
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "",
                textAlign = TextAlign.Center
            )
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = when {
                    difference == null -> Icons.Outlined.WatchLater
                    difference > 0 -> Icons.Rounded.TrendingUp
                    difference < 0 -> Icons.Rounded.TrendingDown
                    else -> Icons.Rounded.TrendingFlat
                },
                contentDescription = "Arrows",
                tint = valuePivotZeroColor(difference)
            )
            Text(
                text = "$sign${difference?.times(100)?.absoluteValue?.toStringPrecision(2) ?: "-"}%",
                style = MaterialTheme.typography.caption,
                color = valuePivotZeroColor(difference)
            )
        }
    }
}

@Composable
fun PermissionCard(
    performanceViewModel: PerformanceViewModel
) = Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
    elevation = 0.dp
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Estadísticas avanzadas"
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "Esto permite hacer comparaciones por carrera, unidad y el IPN. Los resultados dependen de la cantidad de usuarios que deciden compartir sus estadísticas.\n\nLos datos que se almacenarán son:\n• Número de boleta.\n• Unidad académica.\n• Nombre de la carrera.\n• Promedio global.\n• Kárdex.",
            style = MaterialTheme.typography.body2
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            ziox.ramiro.saes.ui.components.TextButton(
                text = "RECHAZAR",
                textColor = getCurrentTheme().danger
            ){
                UserPreferences.invoke(context).setPreference(PreferenceKeys.PerformanceSaveDataPermission, false)
                performanceViewModel.checkPerformancePermissions(context)
            }
            ziox.ramiro.saes.ui.components.TextButton(
                text = "Aceptar",
                textColor = getCurrentTheme().info
            ){
                UserPreferences.invoke(context).setPreference(PreferenceKeys.PerformanceSaveDataPermission, true)
                performanceViewModel.checkPerformancePermissions(context)
            }
        }
    }
}

@Composable
fun KardexChart(
    kardexData: KardexData
) = Card(
    modifier = Modifier.padding(bottom = 16.dp),
    elevation = 0.dp
) {
    val currentThemeColors = getCurrentTheme()
    val scores = getScoresDataSet(kardexData)
    val averages = getOverallScoresDataSet(kardexData)

    Column(
        modifier = Modifier.padding(
            horizontal = 16.dp,
            vertical = 8.dp
        ),
    ) {
        Text(
            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 16.dp
            ),
            text = "Promedio a lo largo del tiempo"
        )
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            factory = {
                LineChart(it).apply {
                    description.text = ""

                    setDrawBorders(false)
                    setNoDataText("Esperando datos")
                    isDoubleTapToZoomEnabled = false
                    setScaleEnabled(false)
                    xAxis.axisMinimum = -0.1f
                    xAxis.setDrawGridLines(false)
                    xAxis.granularity = 1f

                    xAxis.axisLineWidth = 2f
                    xAxis.axisLineColor = currentThemeColors.primaryText.toArgb()
                    xAxis.textColor = currentThemeColors.primaryText.toArgb()
                    xAxis.valueFormatter = object : IndexAxisValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "${value.toInt()+1}°"
                        }
                    }
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.textSize = 14f

                    axisLeft.granularity = 0.5f
                    axisLeft.setDrawGridLines(true)
                    axisLeft.setDrawZeroLine(false)
                    axisLeft.disableGridDashedLine()
                    axisLeft.gridLineWidth = 1.5f
                    axisLeft.setDrawAxisLine(false)
                    axisLeft.textColor = currentThemeColors.primaryText.toArgb()
                    axisLeft.setLabelCount(2, false)
                    axisLeft.textSize = 12f

                    axisRight.isEnabled = false

                    isDragXEnabled = true
                    scaleX = 1f
                    scaleY = 1f

                    legend.textSize = 12f
                    legend.textColor = currentThemeColors.primaryText.toArgb()

                    invalidate()
                }
            }
        ){
            val datasets = listOf(scores, averages)
            val xMax = max(scores.xMax, averages.xMax)

            it.data = LineData(datasets)
            it.setVisibleXRange(-0.1f, xMax - 0.8f)
            it.data.isHighlightEnabled = false
            it.invalidate()
        }
    }
}

@Composable
fun OtherStatisticsCard(
    title: String,
    value: String,
    backgroundColor: Color?,
    contentColor: Color?
) = Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
    elevation = 0.dp,
    backgroundColor = backgroundColor ?: MaterialTheme.colors.surface
) {

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = title,
            color = contentColor ?: MaterialTheme.colors.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.h4,
            color = contentColor ?: MaterialTheme.colors.onSurface
        )
    }
}

@Composable
fun TendencyCard(
    title: String,
    values: List<Double>,
    changeThreshold: Double = 0.3
) = Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
    elevation = 0.dp
) {
    val lastThree = values.takeLast(3)
    val firstHalf = lastThree.subList(0, lastThree.size/2)
    val secondHalf = lastThree.subList(lastThree.size/2, lastThree.size)

    val averageFirstHalf = firstHalf.sum().div(firstHalf.size.toDouble())
    val averageSecondHalf = secondHalf.sum().div(secondHalf.size.toDouble())

    val averageDifference = averageSecondHalf - averageFirstHalf

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = title
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(42.dp),
                imageVector = when {
                    averageDifference > changeThreshold -> Icons.Rounded.TrendingUp
                    averageDifference < -changeThreshold -> Icons.Rounded.TrendingDown
                    else -> Icons.Rounded.TrendingFlat
                },
                contentDescription = "Arrows",
                tint = valuePivotZeroColor(if (averageDifference in -changeThreshold..changeThreshold) 0.0 else averageDifference)
            )
            Text(
                text = when{
                    averageDifference > changeThreshold -> "Al Alza"
                    averageDifference < -changeThreshold -> "A la baja"
                    else -> "Lateral"
                },
                style = MaterialTheme.typography.h4,
                color = valuePivotZeroColor(if (averageDifference in -changeThreshold..changeThreshold) 0.0 else averageDifference)
            )
        }
    }
}

@Composable
fun getScoresDataSet(kardexData: KardexData) : LineDataSet {
    val scores = kardexData.kardexPeriods.mapIndexed { i, period ->
        Entry(i.toFloat(), period.average.toFloat())
    }
    val scoresDataSet = LineDataSet(scores, "Promedio por semestre")
    scoresDataSet.color = getCurrentTheme().colors.primary.toArgb()
    scoresDataSet.valueTextColor = getCurrentTheme().colors.primary.toArgb()
    scoresDataSet.valueTextSize = 10f
    scoresDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
    scoresDataSet.lineWidth = 4f
    scoresDataSet.setCircleColor(Color.Transparent.toArgb())
    scoresDataSet.circleHoleColor = Color.Transparent.toArgb()

    return scoresDataSet
}

@Composable
fun getOverallScoresDataSet(kardexData: KardexData) : LineDataSet {
    val averages = kardexData.kardexPeriods.mapIndexed { i, _ ->
        Entry(i.toFloat(), kardexData.generalScoreAt(i).toFloat())
    }
    val averageDataSet = LineDataSet(averages, "Promedio global")
    averageDataSet.color = getCurrentTheme().colors.secondary.toArgb()
    averageDataSet.setCircleColor(getCurrentTheme().primaryText.toArgb())
    averageDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
    averageDataSet.enableDashedLine(32f, 12f, 1f)
    averageDataSet.lineWidth = 4f
    averageDataSet.valueTextSize = 10f
    averageDataSet.valueTextColor = getCurrentTheme().primaryText.toArgb()
    return averageDataSet
}

@Composable
fun valuePivotZeroColor(difference: Double?) = when{
    difference == null || difference == 0.0 -> getCurrentTheme().primaryText
    difference < 0 -> getCurrentTheme().danger
    else -> getCurrentTheme().info
}