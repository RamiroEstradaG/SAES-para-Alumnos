package ziox.ramiro.saes.features.saes.features.schedule.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowRightAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.Hour
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.features.saes.features.schedule.data.models.getRangeBy
import ziox.ramiro.saes.features.saes.features.schedule.ui.screens.getHourHeight
import ziox.ramiro.saes.features.saes.features.schedule.ui.screens.hourWidth
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.toHour
import java.util.Date

@Composable
fun ScheduleWeekContainer(
    classSchedules: List<ClassSchedule>,
    selectedDayOfWeek: MutableState<WeekDay?> = mutableStateOf(null),
    canEdit: Boolean = true
) {
    val hourRange = classSchedules.getRangeBy { it.scheduleDayTime }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        HourColumn(
            hourRange,
            classSchedules
        )
        ScheduleDayContainer(
            selectedDayOfWeek = selectedDayOfWeek,
            modifier = Modifier.animateContentSize().run {
                if(selectedDayOfWeek.value == null || selectedDayOfWeek.value == WeekDay.MONDAY){
                    weight(1f)
                }else{
                    width(0.dp)
                }
            },
            weekDay = WeekDay.MONDAY,
            classSchedules = classSchedules,
            hourRange = hourRange,
            canEdit = canEdit
        ){
            selectedDayOfWeek.value = if(selectedDayOfWeek.value != it){
                it
            }else null
        }
        ScheduleDayContainer(
            selectedDayOfWeek = selectedDayOfWeek,
            weekDay = WeekDay.TUESDAY,
            modifier = Modifier.animateContentSize().run {
                if(selectedDayOfWeek.value == null || selectedDayOfWeek.value == WeekDay.TUESDAY){
                    weight(1f)
                }else{
                    width(0.dp)
                }
            },
            classSchedules = classSchedules,
            hourRange = hourRange,
            canEdit = canEdit
        ){
            selectedDayOfWeek.value = if(selectedDayOfWeek.value != it){
                it
            }else null
        }
        ScheduleDayContainer(
            selectedDayOfWeek = selectedDayOfWeek,
            modifier = Modifier.animateContentSize().run {
                if(selectedDayOfWeek.value == null || selectedDayOfWeek.value == WeekDay.WEDNESDAY){
                    weight(1f)
                }else{
                    width(0.dp)
                }
            },
            weekDay = WeekDay.WEDNESDAY,
            classSchedules = classSchedules,
            hourRange = hourRange,
            canEdit = canEdit
        ){
            selectedDayOfWeek.value = if(selectedDayOfWeek.value != it){
                it
            }else null
        }
        ScheduleDayContainer(
            selectedDayOfWeek = selectedDayOfWeek,
            modifier = Modifier.animateContentSize().run {
                if(selectedDayOfWeek.value == null || selectedDayOfWeek.value == WeekDay.THURSDAY){
                    weight(1f)
                }else{
                    width(0.dp)
                }
            },
            weekDay = WeekDay.THURSDAY,
            classSchedules = classSchedules,
            hourRange = hourRange,
            canEdit = canEdit
        ){
            selectedDayOfWeek.value = if(selectedDayOfWeek.value != it){
                it
            }else null
        }
        ScheduleDayContainer(
            selectedDayOfWeek = selectedDayOfWeek,
            modifier = Modifier.animateContentSize().run {
                if(selectedDayOfWeek.value == null || selectedDayOfWeek.value == WeekDay.FRIDAY){
                    weight(1f)
                }else{
                    width(0.dp)
                }
            },
            weekDay = WeekDay.FRIDAY,
            classSchedules = classSchedules,
            hourRange = hourRange,
            canEdit = canEdit
        ){
            selectedDayOfWeek.value = if(selectedDayOfWeek.value != it){
                it
            }else null
        }
    }
}


@Composable
fun HourColumn(
    hourRange: IntRange,
    classSchedules: List<ClassSchedule>
) {
    val hourHeight = classSchedules.getHourHeight()
    Box(
        modifier = Modifier.size(hourWidth, hourHeight * (hourRange.last - hourRange.first))
    ){
        val currentHour = Hour.fromDate(Date())
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            hourRange.forEach {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp).background(getCurrentTheme().divider),
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(hourHeight - 1.dp),
                    text = it.toDouble().toHour(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        val iconPadding = hourHeight.times(currentHour.toDouble().minus(hourRange.first).toFloat()).minus(12.dp)
        if(iconPadding >= 0.dp){
            Icon(
                modifier = Modifier
                    .padding(top = iconPadding)
                    .align(Alignment.TopEnd),
                imageVector = Icons.Rounded.ArrowRightAlt,
                contentDescription = "Current hour",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}