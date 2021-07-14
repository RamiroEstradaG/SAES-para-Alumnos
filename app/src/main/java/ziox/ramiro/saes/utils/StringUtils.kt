package ziox.ramiro.saes.utils

import java.math.BigDecimal
import java.text.NumberFormat


fun String.toProperCase(): String {
    val str = this.lowercase().split(" ").filter {
        it.isNotEmpty()
    }

    return if (str.isEmpty()) {
        ""
    } else {
        var res = ""
        for ((i, arr) in str.withIndex()) {
            res += if (i == str.lastIndex) {
                if (arr.matches(Regex("[ivx]+|(ETS)"))) {
                    arr.uppercase()
                } else {
                    arr.replaceFirstChar{ it.uppercase() }
                }
            } else if (arr.matches(Regex("de|del|y|o|e|por|a|u|para|las|los|la|el|en"))) {
                "$arr "
            } else {
                "${arr.replaceFirstChar{ it.uppercase() }} "
            }
        }
        res
    }
}

fun String.getInitials(): String {
    var siglas = ""
    val rem = this.uppercase().replace(
        Regex("( )(((DE|DEL|Y|O|E|A|U|POR|PARA|LAS|LOS|LA|EL|EN) (DE|DEL|Y|O|E|A|U|POR|PARA|LAS|LOS|LA|EL|EN))|DE|DEL|Y|O|E|A|U|POR|PARA|LAS|LOS|LA|EL|EN)( )"),
        " "
    ).replace(Regex(" \\([^)]*\\)"), "")

    val str = rem.split(" ").filter {
        it.isNotEmpty()
    }

    if(str.isEmpty()) return ""

    if (str.size == 1) {
        siglas = rem.substring(0, kotlin.math.min(this.length, 4))
    } else {
        if (str.last().matches(Regex("[IVX]+")) && str.size == 2) {
            siglas = str.first().substring(0, kotlin.math.min(this.length, 3)) + " " + str.last().uppercase()
        } else {
            for ((i, arr) in str.withIndex()) {
                siglas += if (i == str.lastIndex) {
                    if (arr.matches(Regex("[IVX]+"))) {
                        " ${arr.uppercase()}"
                    } else {
                        arr.first()
                    }
                } else {
                    arr.first()
                }
            }
        }
    }

    return siglas
}


fun Double.toHour()
    = "${toInt().toString().padStart(2,'0')}:${(this%1).times(60).toInt().toString().padStart(2,'0')}"

fun String.isSchoolGroup() = this.matches(Regex("[0-9]{1,3}[a-zA-Z]+[0-9]{1,3}"))

fun Double.toStringPrecision(digits : Int) : String{
    val numberFormat = NumberFormat.getNumberInstance()
    numberFormat.maximumFractionDigits = digits
    return numberFormat.format(this)
}