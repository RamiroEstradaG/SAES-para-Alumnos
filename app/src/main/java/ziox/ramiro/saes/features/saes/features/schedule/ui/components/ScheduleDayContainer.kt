package ziox.ramiro.saes.features.saes.features.schedule.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ripple

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.WeekDay
import ziox.ramiro.saes.features.saes.features.schedule.ui.screens.getHourHeight

@Composable
fun ScheduleDayContainer(
    modifier: Modifier = Modifier,
    classSchedules: List<ClassSchedule>,
    weekDay: WeekDay,
    selectedDayOfWeek: MutableState<WeekDay?> = mutableStateOf(null),
    hourRange: IntRange,
    canEdit: Boolean,
    onClick: (WeekDay) -> Unit
) = Box(
    modifier = modifier
        .height(
            classSchedules
                .getHourHeight()
                .times(hourRange.last - hourRange.first)
        )
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            onClick = {
                onClick(weekDay)
            },
            indication = ripple()
        )
) {
    classSchedules.filter {
        it.scheduleDayTime.weekDay == weekDay
    }.forEach {
        ScheduleClassView(
            isExpanded = selectedDayOfWeek.value == weekDay,
            classSchedule = it,
            startHour = hourRange.first,
            hourHeight = classSchedules.getHourHeight(),
            canEdit = canEdit
        )
    }
}