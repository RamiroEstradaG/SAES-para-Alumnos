package ziox.ramiro.saes.utils

import java.text.SimpleDateFormat
import java.util.*

fun Date.toCalendar() : Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar
}

fun Calendar.format() : String = SimpleDateFormat("EEEE, d 'de' MMMM'\n'hh:mm a", Locale("es", "MX")).format(this.time)