package ziox.ramiro.saes.features.saes.features.schedule.presentation

import android.graphics.ColorSpace
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule

val hourHeight = 150.dp

@Composable
fun Schedule(

) = Column(
    modifier = Modifier.fillMaxSize()
) {
    val selectedDayOfWeek: MutableState<ClassSchedule.WeekDay?> = remember {
        mutableStateOf(null)
    }

    ScheduleHeader(
        selectedDayOfWeek = selectedDayOfWeek
    )
    ScheduleWeekContainer(
        classSchedules = listOf(
            ClassSchedule(
            "",
            Color.Red,
            ClassSchedule.Hour("8:00-9:00", ClassSchedule.WeekDay.WEDNESDAY)
        )),
        selectedDayOfWeek = selectedDayOfWeek
    )
}


@Composable
fun ScheduleHeader(
    selectedDayOfWeek: MutableState<ClassSchedule.WeekDay?> = mutableStateOf(null)
) = if(selectedDayOfWeek.value != null){
    Text(text = selectedDayOfWeek.value!!.dayName)
}else{
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(text = "L")
        Text(text = "M")
        Text(text = "M")
        Text(text = "J")
        Text(text = "V")
    }
}

@Composable
fun ScheduleWeekContainer(
    classSchedules: List<ClassSchedule>,
    selectedDayOfWeek: MutableState<ClassSchedule.WeekDay?> = mutableStateOf(null)
) {
    val hourRange = classSchedules.getHourRange()

    Row(
        modifier = Modifier.fillMaxWidth().height(hourHeight.times(hourRange.last - hourRange.first))
    ) {
        val isMondayExpanded = selectedDayOfWeek.value == ClassSchedule.WeekDay.MONDAY
        val isTuesdayExpanded = selectedDayOfWeek.value == ClassSchedule.WeekDay.TUESDAY
        val isWednesdayExpanded = selectedDayOfWeek.value == ClassSchedule.WeekDay.WEDNESDAY
        val isThursdayExpanded = selectedDayOfWeek.value == ClassSchedule.WeekDay.THURSDAY
        val isFridayExpanded = selectedDayOfWeek.value == ClassSchedule.WeekDay.FRIDAY

        ScheduleDayContainer(
            isExpanded = isMondayExpanded,
            modifier = Modifier.weight(if (!isMondayExpanded && selectedDayOfWeek.value != null) 0f else 1f),
            weekDay = ClassSchedule.WeekDay.MONDAY,
            classSchedules = classSchedules,
            hourRange = hourRange
        ){
            selectedDayOfWeek.value = if(selectedDayOfWeek.value != it){
                it
            }else null
        }
        ScheduleDayContainer(
            isExpanded = isTuesdayExpanded,
            modifier = Modifier.weight(if (!isTuesdayExpanded && selectedDayOfWeek.value != null) 0f else 1f),
            weekDay = ClassSchedule.WeekDay.TUESDAY,
            classSchedules = classSchedules,
            hourRange = hourRange
        ){
            selectedDayOfWeek.value = if(selectedDayOfWeek.value != it){
                it
            }else null
        }
        ScheduleDayContainer(
            isExpanded = isWednesdayExpanded,
            modifier = Modifier.weight(if (!isWednesdayExpanded && selectedDayOfWeek.value != null) 0f else 1f),
            weekDay = ClassSchedule.WeekDay.WEDNESDAY,
            classSchedules = classSchedules,
            hourRange = hourRange
        ){
            selectedDayOfWeek.value = if(selectedDayOfWeek.value != it){
                it
            }else null
        }
        ScheduleDayContainer(
            isExpanded = isThursdayExpanded,
            modifier = Modifier.weight(if (!isThursdayExpanded && selectedDayOfWeek.value != null) 0f else 1f),
            weekDay = ClassSchedule.WeekDay.THURSDAY,
            classSchedules = classSchedules,
            hourRange = hourRange
        ){
            selectedDayOfWeek.value = if(selectedDayOfWeek.value != it){
                it
            }else null
        }
        ScheduleDayContainer(
            isExpanded = isFridayExpanded,
            modifier = Modifier.weight(if (!isFridayExpanded && selectedDayOfWeek.value != null) 0f else 1f),
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
fun ScheduleDayContainer(
    modifier: Modifier = Modifier,
    classSchedules: List<ClassSchedule>,
    weekDay: ClassSchedule.WeekDay,
    isExpanded: Boolean = false,
    hourRange: IntRange,
    onClick: (ClassSchedule.WeekDay) -> Unit
) = Box(
    modifier = modifier.clickable(
        interactionSource = MutableInteractionSource(),
        onClick = {
            onClick(weekDay)
        },
        indication = rememberRipple()
    )
) {
    classSchedules.filter {
        it.hour.weekDay == weekDay
    }.forEach {
        ScheduleClassView(
            isExpanded = isExpanded,
            classSchedule = it,
            startHour = hourRange.first
        )
    }
}

@Composable
fun ScheduleClassView(
    isExpanded: Boolean = false,
    startHour: Int,
    classSchedule: ClassSchedule
) = Card(
    modifier = Modifier
        .padding(top = hourHeight.times((classSchedule.hour.start - startHour).toFloat()))
        .height(hourHeight.times(classSchedule.hour.duration.toFloat()))
        .fillMaxWidth(),
    backgroundColor = classSchedule.color
) {

}


fun List<ClassSchedule>.getHourRange(): IntRange {
    var start : Double? = null
    var end : Double? = null

    for (classSchedule in this) {
        if(start?.compareTo(classSchedule.hour.start) == 1){
            start = classSchedule.hour.start
        }

        if(end?.compareTo(classSchedule.hour.end) == -1){
            end = classSchedule.hour.end
        }
    }

    return IntRange(start?.toInt() ?: 0, end?.toInt() ?: 0)
}