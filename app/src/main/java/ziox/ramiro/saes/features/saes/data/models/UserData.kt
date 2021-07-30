package ziox.ramiro.saes.features.saes.data.models

import ziox.ramiro.saes.features.saes.features.kardex.data.models.KardexData

data class UserData(
    val uid: String = "",
    val studentId: String = "",
    val school: String = "",
    val career: String = "",
    val generalScore: Double? = null,
    val calendarIds: List<String> = listOf(),
    val kardexData: KardexData = KardexData(),
    val isRegistered: Boolean = false
)
