package ziox.ramiro.saes.ui.app_widgets

import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.features.saes.features.schedule.data.models.getRangeBy
import ziox.ramiro.saes.ui.theme.GlanceTheme
import ziox.ramiro.saes.ui.theme.glanceCurrentTheme
import ziox.ramiro.saes.utils.getInitials
import ziox.ramiro.saes.utils.toHour

class ScheduleFullWidget: GlanceAppWidget(){
    @Composable
    override fun Content() {
        ScheduleFull()
    }

    @Preview(showBackground = true)
    @Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
    @Composable
    private fun ScheduleFull(){
        val context = LocalContext.current
        val today = WeekDay.today()
        var scheduleList by remember {
            mutableStateOf<List<ClassSchedule>?>(null)
        }


        GlanceTheme {
            var columnHeight by remember { mutableStateOf(0) }

            Box(
                modifier = GlanceModifier
                    .background(glanceCurrentTheme().colors.background)
                    .fillMaxSize()
            ) {
                if (scheduleList != null) {
                    scheduleList?.let { stateList ->
                        if(stateList.isNotEmpty()){
                            val range = stateList.getRangeBy { it.scheduleDayTime }.let {
                                IntRange(it.first, it.last - 2)
                            }

                            Row {
                                HourColumn(
                                    modifier = GlanceModifier.defaultWeight(),
                                    hourRange = range
                                )
                                listOf(
                                    WeekDay.MONDAY,
                                    WeekDay.TUESDAY,
                                    WeekDay.WEDNESDAY,
                                    WeekDay.THURSDAY,
                                    WeekDay.FRIDAY
                                ).forEach { weekDay ->
                                    Column(
                                        modifier = GlanceModifier
                                            .defaultWeight()
                                            .fillMaxSize()
                                            .background(
                                                if (today == weekDay) {
                                                    glanceCurrentTheme().danger.copy(
                                                        alpha = 0.1f
                                                    )
                                                } else {
                                                    Color.Transparent
                                                }
                                            )
                                    ) {
                                        ColumnTitle(
                                            text = weekDay.dayName.first().toString(),
                                            textColor = if (today == weekDay){
                                                glanceCurrentTheme().danger
                                            }else{
                                                glanceCurrentTheme().primaryText
                                            }
                                        )
                                        Box(
                                            modifier = GlanceModifier
                                                .fillMaxSize()
                                        ) {
                                            stateList
                                                .filter { it.scheduleDayTime.weekDay == weekDay }
                                                .forEach {
                                                    ClassItemView(
                                                        it,
                                                        columnHeight,
                                                        range
                                                    )
                                                }
                                        }
                                    }
                                }
                            }
                        }else{
                            Box(
                                modifier = GlanceModifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Abre tu horario en la aplicación para actualizar",
                                    style = glanceCurrentTheme().typography.h5.let {
                                        TextStyle(
                                            fontSize = it.fontSize,
                                            fontWeight = FontWeight.Bold,
                                            color = ColorProvider(glanceCurrentTheme().secondaryText),
                                            textAlign = TextAlign.Center
                                        )
                                    },
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Cargando...")
                    }
                }
            }
        }

        LaunchedEffect(key1 = scheduleList){
            val db = LocalAppDatabase.invoke(context).scheduleRepository()

            scheduleList = db.getMySchedule()
        }
    }

    @Composable
    fun ClassItemView(
        classSchedule: ClassSchedule,
        columnHeight: Int,
        range: IntRange
    ){
        val hourSize = columnHeight/(range.last - range.first + 1)
        val paddingTop = hourSize.times((classSchedule.scheduleDayTime.start.toDouble() - range.first).toFloat()).toInt()
        val height = hourSize.times(classSchedule.scheduleDayTime.duration.toFloat()).toInt()

        Box(
            modifier = GlanceModifier
                .padding(top = paddingTop)
                .fillMaxWidth()
                .height(height)
                .background(Color(classSchedule.color.toULong())),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = classSchedule.className.getInitials(),
                style = glanceCurrentTheme().typography.h5.let {
                    TextStyle(
                        fontSize = it.fontSize,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(color = Color.White),
                        textAlign = TextAlign.Center
                    )
                },
            )
        }
    }

    @Composable
    fun ColumnTitle(
        text: String,
        textColor: Color = glanceCurrentTheme().primaryText
    ){
        Text(
            modifier = GlanceModifier.fillMaxWidth(),
            text = text,
            style = TextStyle(
                color = ColorProvider(textColor)
            )
        )
    }

    @Composable
    private fun HourColumn(
        modifier: GlanceModifier = GlanceModifier,
        hourRange: IntRange
    ){
        Column(
            modifier = modifier
        ) {
            ColumnTitle(text = "")
            hourRange.forEach {
                Column(
                    modifier = GlanceModifier.defaultWeight()
                ) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(glanceCurrentTheme().divider)
                    ){}
                    Text(
                        modifier = GlanceModifier.fillMaxWidth(),
                        text = it.toDouble().toHour(),
                        style = TextStyle(
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}

class ScheduleFullWidgetReceiver : GlanceAppWidgetReceiver(){
    override val glanceAppWidget: GlanceAppWidget = ScheduleFullWidget()

}
