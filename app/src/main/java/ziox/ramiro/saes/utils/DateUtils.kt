package ziox.ramiro.saes.utils

import java.text.SimpleDateFormat
import java.util.*

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

fun String.toDate(format: String) = SimpleDateFormat(format, Locale.ROOT).parse(this)

fun String.ddMMMyyyy_toDate() : Date{
    val values = split(" ")

    return if(values.size == 3){
        Calendar.getInstance().apply {
            set(Calendar.YEAR, values[2].toInt())
            set(Calendar.MONTH, MES.indexOf(values[1].uppercase()))
            set(Calendar.DAY_OF_MONTH, values[0].toInt())
        }.time
    }else{
        Date()
    }
}


fun String.MMMddyyyy_toDate() : Date{
    val values = split(" ")

    return if(values.size == 3){
        Calendar.getInstance().apply {
            set(Calendar.YEAR, values[2].toInt())
            set(Calendar.MONTH, MES.indexOf(values[0].uppercase()))
            set(Calendar.DAY_OF_MONTH, values[1].toInt())
        }.time
    }else{
        Date()
    }
}