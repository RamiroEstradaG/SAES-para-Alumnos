package ziox.ramiro.saes.features.saes.features.agenda.data.models

import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.HourRange
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ShortDate

data class AgendaItem(
    val eventId: String,
    val eventName: String,
    val date: ShortDate,
    val calendarId: String,
    val admins: List<String>,
    val hourRange: HourRange,
    val description: String? = null,
    val classSchedule: ClassSchedule? = null,
    val eventType: AgendaEventType = AgendaEventType.PERSONAL
){
    fun toJson() = mapOf(
        "eventId" to eventId,
        "eventName" to eventName,
        "date" to date.toDate(),
        "calendarId" to calendarId,
        "admins" to admins,
        "hourRange" to hourRange,
        "description" to description,
        "classSchedule" to classSchedule?.id,
        "eventType" to eventType.name
    )
}



enum class AgendaEventType {
    ACADEMIC, PERSONAL
}