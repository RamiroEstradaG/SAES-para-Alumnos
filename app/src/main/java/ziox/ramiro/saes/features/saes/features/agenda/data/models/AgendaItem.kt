package ziox.ramiro.saes.features.saes.features.agenda.data.models

import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ScheduleDayTime
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ShortDate

data class AgendaItem(
    val eventId: String = "",
    val eventName: String = "",
    val date: ShortDate = ShortDate(),
    val calendarId: String = "",
    val scheduleDayTime: ScheduleDayTime = ScheduleDayTime(),
    val description: String? = null,
    val classSchedule: ClassSchedule? = null,
    val eventType: AgendaEventType = AgendaEventType.PERSONAL
){
    fun toJson() = mapOf(
        "eventId" to eventId,
        "eventName" to eventName,
        "date" to date.toDate(),
        "calendarId" to calendarId,
        "hourRange" to scheduleDayTime,
        "description" to description,
        "classSchedule" to classSchedule?.id,
        "eventType" to eventType.name
    )

    override fun toString() = """
        AgendaItem(
            eventId: $eventId
            eventName: $eventName
            date: $date
            calendarId: $calendarId
            scheduleDayTime: $scheduleDayTime
            description: $description
            classSchedule: $classSchedule
            eventType: $eventType
        )
    """.trimIndent()
}



enum class AgendaEventType {
    ACADEMIC, PERSONAL
}