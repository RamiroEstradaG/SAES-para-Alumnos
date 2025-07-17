package ziox.ramiro.saes.utils

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

fun String.isUrl() : Boolean{
    return this.matches(Regex("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)"))
}


fun String.getInitials(): String {
    val filteredValue = this.uppercase()
        .replace(
            Regex("( )(((DE|DEL|Y|O|E|A|U|POR|PARA|LAS|LOS|LA|EL|EN) (DE|DEL|Y|O|E|A|U|POR|PARA|LAS|LOS|LA|EL|EN))|DE|DEL|Y|O|E|A|U|POR|PARA|LAS|LOS|LA|EL|EN)( )"),
            " "
        ).replace(Regex(" \\([^)]*\\)"), "")

    val words = filteredValue.split(" ").filter {
        it.isNotEmpty()
    }

    if(words.isEmpty()) return ""

    return if (words.size == 1) {
        filteredValue.substring(0, kotlin.math.min(this.length, 4))
    } else {
        if (words.last().matches(Regex("[IVX]+")) && words.size == 2) {
            words.first().substring(0, kotlin.math.min(this.length, 3)) + " " + words.last().uppercase()
        } else {
            words.mapIndexed { index, word ->
                if (index == words.lastIndex){
                    if (word.matches(Regex("[IVX]+"))) {
                        " ${word.uppercase()}"
                    } else {
                        word.first()
                    }
                }else {
                    word.first()
                }
            }.joinToString("")
        }
    }
}


fun Double.toHour()
    = "${toInt().toString().padStart(2,'0')}:${(this%1).times(60).toInt().toString().padStart(2,'0')}"

fun String.isSchoolGroup() = this.matches(Regex("[0-9]{1,3}[a-zA-Z]+[0-9]{1,3}"))

fun Double.toStringPrecision(digits : Int) : String{
    val numberFormat = NumberFormat.getNumberInstance()
    numberFormat.maximumFractionDigits = digits
    return numberFormat.format(this)
}