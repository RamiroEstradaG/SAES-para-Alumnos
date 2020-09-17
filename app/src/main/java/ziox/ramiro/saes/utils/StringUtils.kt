package ziox.ramiro.saes.utils

import android.content.Context
import android.util.Log
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

@Suppress("unused")
object HashUtils {
    private const val HEX_CHARS = "0123456789abcdef"

    fun sha512(input: String) = hashString("SHA-512", input)

    fun sha256(input: String) = hashString("SHA-256", input)

    fun sha1(input: String) = hashString("SHA-1", input)

    private fun hashString(type: String, input: String): String {
        val bytes = MessageDigest
            .getInstance(type)
            .digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }

        return result.toString()
    }
}

fun generateRandomString(size: Int) : String{
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
    val random = Random()
    var res = ""
    for (i in 0 until size){
        res += chars[random.nextInt().absoluteValue%chars.length]
    }
    return res
}

fun String.isUrl() : Boolean{
    return this.matches(Regex("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)"))
}

fun String.toProperCase(): String {
    val str = this.toLowerCase(Locale.ROOT).split(" ").filter {
        it.isNotEmpty()
    }

    return if (str.isEmpty()) {
        ""
    } else {
        var res = ""
        for ((i, arr) in str.withIndex()) {
            res += if (i == str.lastIndex) {
                if (arr.matches(Regex("[ivx]+"))) {
                    arr.toUpperCase(Locale.ROOT)
                } else {
                    arr.capitalize(Locale.ROOT)
                }
            } else if (arr.matches(Regex("de|del|y|o|e|por|a|u|para|las|los|la|el|en"))) {
                "$arr "
            } else {
                "${arr.capitalize(Locale.ROOT)} "
            }
        }
        res
    }
}

fun String.getInitials(): String {
    var siglas = ""
    val rem = this.toUpperCase(Locale.ROOT).replace(Regex("( )(((DE|DEL|Y|O|E|A|U|POR|PARA|LAS|LOS|LA|EL|EN) (DE|DEL|Y|O|E|A|U|POR|PARA|LAS|LOS|LA|EL|EN))|DE|DEL|Y|O|E|A|U|POR|PARA|LAS|LOS|LA|EL|EN)( )"),
        " ").replace(Regex(" \\([^)]*\\)"), "")

    val str = rem.split(" ").filter {
        it.isNotEmpty()
    }

    if(str.isEmpty()) return ""

    if (str.size == 1) {
        siglas = rem.substring(0, kotlin.math.min(this.length, 4))
    } else {
        if (str.last().matches(Regex("[IVX]+")) && str.size == 2) {
            siglas = str.first().substring(0, kotlin.math.min(this.length, 3)) + " " + str.last().toUpperCase(Locale.ROOT)
        } else {
            for ((i, arr) in str.withIndex()) {
                siglas += if (i == str.lastIndex) {
                    if (arr.matches(Regex("[IVX]+"))) {
                        " ${arr.toUpperCase(Locale.ROOT)}"
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

fun String.toDate() : Date {
    val str2 = this.replace(". ","")
    val format = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a.", Locale.US)

    try {
        return format.parse(str2)?: Date()
    } catch (e: Exception) {
        Log.e("AppException", e.toString())
    }
    return Date()
}


fun dividirHoras(hora: String) : Pair<Double, Double>?{
    val horas = Regex("[0-9]+:[0-9]+-[0-9]+:[0-9]+").find(hora.replace(" ", ""))?.value?.split("-")

    return if(horas?.size == 2){
        val hora1 = horas[0].split(":")
        val hora2 = horas[1].split(":")
        Pair(hora1[0].toDouble()+(hora1[1].toDouble()/60.0), hora2[0].toDouble()+(hora2[1].toDouble()/60.0))
    }else{
        null
    }
}

fun String.toDateString() : String{
    val str2 = this.replace(". ","")

    val format = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a.", Locale.US)

    try {
        val date = format.parse(str2)
        try {
            val strFormat = SimpleDateFormat("EEEE, d 'de' MMMM 'del' yyyy 'a las' HH:mm", Locale.US)
            return strFormat.format(date?: Date())
        } catch (e: Exception) {
            Log.e("AppException", e.toString())
        }
    } catch (e: Exception) {
        Log.e("AppException", e.toString())
    }
    return ""
}

fun hourToDouble(hora: String) : Double{
    val horaValue = Regex("[0-9]+:[0-9]+").find(hora)?.value?.split(":") ?: return -1.0

    return horaValue[0].toDouble()+(horaValue[1].toDouble()/60.0)
}

fun getHashUserId(context: Context?) = HashUtils.sha256(getBoleta(context)+getNameEscuela(context))