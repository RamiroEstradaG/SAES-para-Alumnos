package ziox.ramiro.saes.features.saes.features.agenda.data.models

import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.HourRange
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ShortDate

data class AgendaItem(
    val eventName: String,
    val date: ShortDate,
    val hourRange: HourRange,
    val description: String? = null,
    val classSchedule: ClassSchedule? = null,
)
