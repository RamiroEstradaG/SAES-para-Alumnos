package ziox.ramiro.saes.utils

import android.content.Context
import java.text.NumberFormat

fun Double.toStringPrecision(digits : Int) : String{
    val numberFormat = NumberFormat.getNumberInstance()
    numberFormat.maximumFractionDigits = digits
    return numberFormat.format(this)
}

fun Double.toHourString(): String{
    return "${this.toInt().toString().padStart(2, '0')}:${((this%1.0)*60).toInt().toString().padStart(2, '0')}"
}

fun Double.decimal() = this%1

fun Array<out Number?>.mean() : Double{
    val tmp = this.filterNotNull()
    if(this.isEmpty()) return 0.0

    if(this.first() !is Number) throw NumberFormatException()
    var mean = 0.0

    for(n in tmp){
        mean += n.toDouble()
    }

    return mean.div(tmp.size)
}

@Suppress("unused")
fun pixelToDp(px : Float, context: Context?) : Float{
    return px / (context?.resources?.displayMetrics?.density ?: 1f)
}

fun dpToPixel(context: Context?, dp : Int) = context?.resources?.displayMetrics?.density!!.times(dp).toInt()