package ziox.ramiro.saes.features.saes.features.schedule.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
    isClassActionsEnabled: Boolean,
    onClick: (WeekDay) -> Unit
) = Box(
    modifier = modifier
        .height(
            classSchedules
                .getHourHeight()
                .times(hourRange.last - hourRange.first)
        )
        .clickable(
            interactionSource = MutableInteractionSource(),
            onClick = {
                onClick(weekDay)
            },
            indication = rememberRipple()
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
            isClassActionsEnabled = isClassActionsEnabled
        )
    }
}