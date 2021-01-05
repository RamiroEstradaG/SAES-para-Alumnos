package ziox.ramiro.saes.utils

import android.content.Context

fun <T>setPreference(context: Context?, key : String, value : T){
    val pref = context?.getSharedPreferences("preferences", Context.MODE_PRIVATE)?.edit()
    when(value){
        is Int -> pref?.putInt(key, value)
        is Long -> pref?.putLong(key, value)
        is Float -> pref?.putFloat(key, value)
        is String -> pref?.putString(key, value)
        is Boolean -> pref?.putBoolean(key, value)
    }
    pref?.apply()
}

fun getPreference(context: Context?, type: ValType, key : String) : Any?{
    val pref = context?.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    return when(type){
        ValType.INT -> pref?.getInt(key, 0)
        ValType.LONG -> pref?.getLong(key, 0)
        ValType.FLOAT -> pref?.getFloat(key, 0f)
        ValType.STRING -> pref?.getString(key, "")
        ValType.BOOLEAN -> pref?.getBoolean(key, false)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T>getPreference(context: Context?, key : String, default: T) : T{
    val pref = context?.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    return when (default) {
        is Int -> pref?.getInt(key, default) as? T
        is Long -> pref?.getLong(key, default) as? T
        is Float -> pref?.getFloat(key, default) as? T
        is String -> pref?.getString(key, default) as? T
        is Boolean -> pref?.getBoolean(key, default) as? T
        else -> null
    } ?:default
}

fun removePreference(context: Context?, key: String){
    context?.getSharedPreferences("preferences", Context.MODE_PRIVATE)?.edit()?.remove(key)?.apply()
}


fun getBoleta(context: Context?) = getPreference(context, "boleta", "")

fun getUrl(context: Context?) : String = getPreference(context, ValType.STRING, "new_url_escuela") as String? ?: ""

fun getSchoolName(context: Context?) = getPreference(
    context,
    "name_escuela",
    "Instituto Politecnico Nacional"
)

fun getCareerName(context: Context?) = getPreference(context, "carrera", "Sin definir")

/**
 * Si se pueden compartir o no datos sobre el rendimiento escolar de un alumno
 *  0: No se ha elegido una opcion
 *  1: Compartir datos
 * -1: No volver a mostrar
 */
fun isShareStatsEnable(context: Context?) = getPreference(context,"share_stats_enable", 0)

fun setShareStatsEnable(context: Context?, value : Int) = setPreference(context, "share_stats_enable", value)