package ziox.ramiro.saes.features.saes.features.ets_calendar.data.models

import ziox.ramiro.saes.features.saes.features.schedule.data.models.Hour
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ShortDate

data class ETSCalendarItem(
    val id: String,
    val className: String,
    val date: ShortDate,
    val hour: Hour,
    val building: String,
    val classroom: String
)
