package ziox.ramiro.saes.features.saes.data.models

data class UserData(
    val id: String = "",
    val school: String = "",
    val career: String = "",
    val generalScore: Double? = null,
    val calendarIds: List<String> = listOf(),
    val kardex: String = "{}",
)
