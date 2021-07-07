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
import ziox.ramiro.saes.features.saes.features.schedule.ui.screens.getHourHeight
import ziox.ramiro.saes.features.saes.features.schedule.ui.screens.getHourRange
import ziox.ramiro.saes.features.saes.features.schedule.ui.screens.hourWidth
import ziox.ramiro.saes.utils.toHour

@Composable
fun ScheduleWeekContainer(
    classSchedules: List<ClassSchedule>,
    selectedDayOfWeek: MutableState<ClassSchedule.WeekDay?> = mutableStateOf(null)
) {
    val hourRange = classSchedules.getHourRange()

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
                if(selectedDayOfWeek.value == null || selectedDayOfWeek.value == ClassSchedule.WeekDay.MONDAY){
                    weight(1f)
                }else{
                    width(0.dp)
                }
            },
            weekDay = ClassSchedule.WeekDay.MONDAY,
            classSchedules = classSchedules,
            hourRange = hourRange
        ){
            selectedDayOfWeek.value = if(selectedDayOfWeek.value != it){
                it
            }else null
        }
        ScheduleDayContainer(
            selectedDayOfWeek = selectedDayOfWeek,
            weekDay = ClassSchedule.WeekDay.TUESDAY,
            modifier = Modifier.animateContentSize().run {
                if(selectedDayOfWeek.value == null || selectedDayOfWeek.value == ClassSchedule.WeekDay.TUESDAY){
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
                if(selectedDayOfWeek.value == null || selectedDayOfWeek.value == ClassSchedule.WeekDay.WEDNESDAY){
                    weight(1f)
                }else{
                    width(0.dp)
                }
            },
            weekDay = ClassSchedule.WeekDay.WEDNESDAY,
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
                if(selectedDayOfWeek.value == null || selectedDayOfWeek.value == ClassSchedule.WeekDay.THURSDAY){
                    weight(1f)
                }else{
                    width(0.dp)
                }
            },
            weekDay = ClassSchedule.WeekDay.THURSDAY,
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
                if(selectedDayOfWeek.value == null || selectedDayOfWeek.value == ClassSchedule.WeekDay.FRIDAY){
                    weight(1f)
                }else{
                    width(0.dp)
                }
            },
            weekDay = ClassSchedule.WeekDay.FRIDAY,
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