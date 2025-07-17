package ziox.ramiro.saes.utils

import ziox.ramiro.saes.features.saes.features.schedule.data.models.Hour
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

val ES_MX_LOCALE: Locale = Locale.forLanguageTag("es")

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

fun Date.toLongString(): String = DateFormat
    .getDateInstance(SimpleDateFormat.LONG, ES_MX_LOCALE)
    .format(this)

fun Date.toLongStringAndHour() = DateFormat
    .getDateInstance(SimpleDateFormat.LONG, ES_MX_LOCALE)
    .format(this) + " ${Hour.fromDate(this)}"

fun Date.toMediumString(): String = DateFormat
    .getDateInstance(SimpleDateFormat.MEDIUM, ES_MX_LOCALE)
    .format(this)

fun Date.toShortString(): String = DateFormat
    .getDateInstance(SimpleDateFormat.SHORT, ES_MX_LOCALE)
    .format(this)


fun Date.toCalendar(): Calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
    time = this@toCalendar
}

fun String.toDate(format: String): Date? = try {
    SimpleDateFormat(format, Locale.ROOT).parse(this)
} catch (_: Exception) {
    null
}


@ExperimentalTime
fun Date.offset(offset: Duration): Date {
    return Calendar.getInstance(TimeZone.getDefault()).apply {
        timeInMillis = this@offset.time + offset.inWholeMilliseconds
    }.time
}

fun String.ddMMMyyyy_toDate(): Date {
    val values = split(" ")

    return if (values.size == 3) {
        Calendar.getInstance(TimeZone.getDefault()).apply {
            set(Calendar.YEAR, values[2].toInt())
            set(Calendar.MONTH, MES.indexOf(values[1].uppercase()))
            set(Calendar.DAY_OF_MONTH, values[0].toInt())
        }.time
    } else {
        Date()
    }
}


fun String.MMMddyyyy_toDate(): Date {
    val values = split(" ")

    return if (values.size == 3) {
        Calendar.getInstance(TimeZone.getDefault()).apply {
            set(Calendar.YEAR, values[2].toInt())
            set(Calendar.MONTH, MES.indexOf(values[0].uppercase()))
            set(Calendar.DAY_OF_MONTH, values[1].toInt())
        }.time
    } else {
        Date()
    }
}

fun String.hhmma_toHour(): Hour? {
    val date = this.toDate("hh:mma")

    return date?.let {
        Hour.fromDate(date)
    }
}