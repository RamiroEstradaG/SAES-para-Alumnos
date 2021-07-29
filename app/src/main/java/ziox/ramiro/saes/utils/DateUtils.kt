package ziox.ramiro.saes.utils

import ziox.ramiro.saes.features.saes.features.schedule.data.models.Hour
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

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

fun Date.toLongString() = DateFormat
    .getDateInstance(SimpleDateFormat.LONG, Locale("es","MX"))
    .format(this)

fun Date.toMediumString() = DateFormat
    .getDateInstance(SimpleDateFormat.MEDIUM, Locale("es","MX"))
    .format(this)

fun Date.toShortString() = DateFormat
    .getDateInstance(SimpleDateFormat.SHORT, Locale("es","MX"))
    .format(this)


fun Date.toCalendar(): Calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
    time = this@toCalendar
}

fun String.toDate(format: String): Date? = try{
    SimpleDateFormat(format, Locale.ROOT).parse(this)
}catch (e:Exception){
    null
}


@ExperimentalTime
fun Date.offset(offset: Duration): Date{
    return Calendar.getInstance(TimeZone.getDefault()).apply {
        timeInMillis = this@offset.time + offset.inWholeMilliseconds
    }.time
}

fun String.ddMMMyyyy_toDate() : Date{
    val values = split(" ")

    return if(values.size == 3){
        Calendar.getInstance(TimeZone.getDefault()).apply {
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
        Calendar.getInstance(TimeZone.getDefault()).apply {
            set(Calendar.YEAR, values[2].toInt())
            set(Calendar.MONTH, MES.indexOf(values[0].uppercase()))
            set(Calendar.DAY_OF_MONTH, values[1].toInt())
        }.time
    }else{
        Date()
    }
}

fun String.hhmma_toHour() : Hour? {
    val date = this.toDate("hh:mma")

    return date?.let {
        Hour.fromDate(date)
    }
}