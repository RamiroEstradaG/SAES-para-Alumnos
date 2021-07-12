package ziox.ramiro.saes.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class UserPreferences private constructor(context: Context){
    companion object {
        @Volatile private var instance: UserPreferences? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: UserPreferences(context).also { instance = it }
        }
    }

    val sharedPreferences : SharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    inline fun <reified T>setPreference(preferenceKeys : PreferenceKeys<T>, value : T){
        sharedPreferences.edit().let {
            when(T::class){
                Int::class -> it.putInt(preferenceKeys.key, value as Int)
                Long::class -> it.putLong(preferenceKeys.key, value as Long)
                Float::class -> it.putFloat(preferenceKeys.key, value as Float)
                String::class -> it.putString(preferenceKeys.key, value as String)
                Boolean::class -> it.putBoolean(preferenceKeys.key, value as Boolean)
                else -> it
            }
        }.commit().also {
            Log.d("SharedPreferenceSET ${preferenceKeys::class.simpleName}", if(it) "$value" else "Not set")
        }
    }

    inline fun <reified T>getPreference(preferenceKeys : PreferenceKeys<T>, default: T) : T{
        return sharedPreferences.let {
            when(T::class) {
                Int::class -> sharedPreferences.getInt(preferenceKeys.key, Int.MIN_VALUE) as T
                Long::class -> sharedPreferences.getLong(preferenceKeys.key, Long.MIN_VALUE) as T
                Float::class -> sharedPreferences.getFloat(preferenceKeys.key, Float.NaN) as T
                String::class -> sharedPreferences.getString(preferenceKeys.key, "null") as T
                Boolean::class -> sharedPreferences.getBoolean(preferenceKeys.key, default as Boolean) as T
                else -> null
            }.let {
                Log.d("SharedPreferenceGET ${preferenceKeys::class.simpleName}", it?.toString() ?: "Not set")
                when (it) {
                    Int.MIN_VALUE -> null
                    Long.MIN_VALUE -> null
                    Float.NaN -> null
                    "null" -> null
                    else -> it
                }
            }
        } ?: default
    }

    fun isAuthDataSaved() : Boolean
            = getPreference(PreferenceKeys.Boleta, "").isNotBlank()
            && getPreference(PreferenceKeys.Password, "").isNotBlank()

    fun removeAuthData(){
        removePreference(PreferenceKeys.Boleta)
        removePreference(PreferenceKeys.Password)
    }

    inline fun <reified T>removePreference(preferenceKeys: PreferenceKeys<T>){
        sharedPreferences.edit().remove(preferenceKeys.key).apply()
    }
}


sealed class PreferenceKeys<T>(val key: String) {
    object SchoolUrl: PreferenceKeys<String?>("new_url_escuela")
    object Boleta: PreferenceKeys<String>("boleta")
    object Password: PreferenceKeys<String>("pass")
    object QrUrl: PreferenceKeys<String>("qr_url")
    object OfflineMode: PreferenceKeys<Boolean>("offline_mode")
}