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
            if(sharedPreferences.contains(preferenceKeys.key)){
                when(T::class) {
                    Int::class -> sharedPreferences.getInt(preferenceKeys.key, 0) as T
                    Long::class -> sharedPreferences.getLong(preferenceKeys.key, 0L) as T
                    Float::class -> sharedPreferences.getFloat(preferenceKeys.key, 0f) as T
                    String::class -> sharedPreferences.getString(preferenceKeys.key, "") as T
                    Boolean::class -> sharedPreferences.getBoolean(preferenceKeys.key, false) as T
                    else -> null
                }
            }else{
                null
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
    object ScheduleWidgetLeveling: PreferenceKeys<Int>("widget_nivel")
    object PerformanceSaveDataPermission: PreferenceKeys<Boolean?>("save_performance_data")
    object DefaultNightMode: PreferenceKeys<Int?>("dark_mode")
}