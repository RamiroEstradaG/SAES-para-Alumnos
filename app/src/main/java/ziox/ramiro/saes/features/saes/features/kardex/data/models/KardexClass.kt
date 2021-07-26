package ziox.ramiro.saes.features.saes.features.kardex.data.models

import java.util.*

data class KardexClass(
    val id: String = "",
    val name: String = "",
    val date: Date = Date(),
    val period: String = "",
    val evaluationType: EvaluationType = EvaluationType.ORDINARY,
    val score: Int? = null
)

enum class EvaluationType{
    EXTRAORDINARY,
    ORDINARY,
    ETS;

    companion object {
        fun fromSAES(value: String) = when(value){
            "ORD" -> ORDINARY
            "EXT" -> EXTRAORDINARY
            "ETS" -> ETS
            else -> ORDINARY
        }
    }
}