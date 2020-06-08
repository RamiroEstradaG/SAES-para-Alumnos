package ziox.ramiro.saes.utils

/**
 * Creado por Ramiro el 12/4/2018 a las 7:47 PM para SAESv2.
 */
data class ClaseData (
    var id: String,
    var diaIndex: Int,
    var materia: String,
    var horaInicio: Double,
    var horaFinal: Double,
    var color: String,
    var grupo: String,
    var profesor : String,
    var edificio : String,
    var salon: String,
    var isUserCustomClase : Boolean = false
)