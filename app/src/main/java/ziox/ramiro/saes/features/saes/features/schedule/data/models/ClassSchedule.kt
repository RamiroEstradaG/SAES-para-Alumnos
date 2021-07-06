package ziox.ramiro.saes.features.saes.features.schedule.data.models

import androidx.compose.ui.graphics.Color

data class ClassSchedule(
    val classId: String,
    val color: Color,
    val hour: Hour
){
    data class Hour(
        val start: Double,
        val end: Double,
        val weekDay: WeekDay
    ){
        val duration: Double = end - start

        constructor(rangeString: String, weekDay: WeekDay) : this(
            splitHours(rangeString)?.first ?: 0.0,
            splitHours(rangeString)?.second ?: 0.0,
            weekDay
        )

        companion object {
            private fun splitHours(hour: String): Pair<Double, Double>?{
                val horas = Regex("[0-9]+:[0-9]+-[0-9]+:[0-9]+").find(hour.replace(" ", ""))?.value?.split("-")

                return if(horas?.size == 2){
                    val hora1 = horas[0].split(":")
                    val hora2 = horas[1].split(":")
                    Pair(hora1[0].toDouble()+(hora1[1].toDouble()/60.0), hora2[0].toDouble()+(hora2[1].toDouble()/60.0))
                }else{
                    null
                }
            }
        }
    }

    enum class WeekDay(val dayName: String){
        MONDAY("Lunes"),
        TUESDAY("Martes"),
        WEDNESDAY("Miércoles"),
        THURSDAY("Jueves"),
        FRIDAY("Viernes")
    }
}


