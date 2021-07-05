package ziox.ramiro.saes.utils

import android.content.Context

enum class SharedPreferenceKeys(val key: String){
    SCHOOL_URL("new_url_escuela")
}

fun <T>Context.setPreference(preferenceKeys : SharedPreferenceKeys, value : T){
    val pref = getSharedPreferences("preferences", Context.MODE_PRIVATE).edit()
    when(value){
        is Int -> pref.putInt(preferenceKeys.key, value)
        is Long -> pref.putLong(preferenceKeys.key, value)
        is Float -> pref.putFloat(preferenceKeys.key, value)
        is String -> pref.putString(preferenceKeys.key, value)
        is Boolean -> pref.putBoolean(preferenceKeys.key, value)
    }
    pref.apply()
}

@Suppress("UNCHECKED_CAST")
fun <T>Context.getPreference(preferenceKeys : SharedPreferenceKeys, default: T) : T{
    val pref = getSharedPreferences("preferences", Context.MODE_PRIVATE)
    return when (default) {
        is Int -> pref.getInt(preferenceKeys.key, default) as? T
        is Long -> pref.getLong(preferenceKeys.key, default) as? T
        is Float -> pref.getFloat(preferenceKeys.key, default) as? T
        is String -> pref.getString(preferenceKeys.key, default) as? T
        is Boolean -> pref.getBoolean(preferenceKeys.key, default) as? T
        else -> null
    } ?:default
}


fun Context.removePreference(preferenceKeys: SharedPreferenceKeys){
    getSharedPreferences("preferences", Context.MODE_PRIVATE).edit().remove(preferenceKeys.key).apply()
}