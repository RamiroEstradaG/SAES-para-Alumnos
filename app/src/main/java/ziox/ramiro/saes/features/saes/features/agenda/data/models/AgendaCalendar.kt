package ziox.ramiro.saes.features.saes.features.agenda.data.models

data class AgendaCalendar(
    val calendarId: String = "",
    val name: String = "",
    val admins: List<String> = listOf()
){
    override fun toString() = """
        AgendaCalendar(
            calendarId: $calendarId
            name: $name
            admins: $admins
        )
    """.trimIndent()
}
