package ziox.ramiro.saes.features.saes.features.schedule.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.features.saes.features.schedule.data.models.getRangeBy
import ziox.ramiro.saes.features.saes.features.schedule.ui.screens.getHourHeight
import ziox.ramiro.saes.features.saes.features.schedule.ui.screens.hourWidth
import ziox.ramiro.saes.utils.toHour

@Composable
fun ScheduleWeekContainer(
    classSchedules: List<ClassSchedule>,
    selectedDayOfWeek: MutableState<WeekDay?> = mutableStateOf(null)
) {
    val hourRange = classSchedules.getRangeBy { it.hourRange }

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
            hourRange = hourRange
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
            hourRange = hourRange
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
            hourRange = hourRange
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
            hourRange = hourRange
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
            hourRange = hourRange
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
) = Column(
    modifier = Modifier.size(hourWidth, classSchedules.getHourHeight() * (hourRange.last - hourRange.first))
) {
    hourRange.forEach {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .height(classSchedules.getHourHeight()),
            text = it.toDouble().toHour(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1
        )
    }
}