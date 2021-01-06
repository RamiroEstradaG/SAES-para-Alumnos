package ziox.ramiro.saes.utils

val MES = arrayOf(
    "ENE",
    "FEB",
    "MAR",
    "ABR",
    "MAY",
    "JUN",
    "JUL",
    "AGO",
    "SEP",
    "OCT",
    "NOV",
    "DIC"
)

val MES_COMPLETO = arrayOf(
    "Enero",
    "Febrero",
    "Marzo",
    "Abril",
    "Mayo",
    "Junio",
    "Julio",
    "Agosto",
    "Septiembre",
    "Octubre",
    "Noviembre",
    "Diciembre"
)

var EDGE_INSET_BOTTOM : Int = -1
var EDGE_INSET_TOP : Int = -1

enum class ValType{
    INT,
    STRING,
    FLOAT,
    BOOLEAN,
    LONG
}