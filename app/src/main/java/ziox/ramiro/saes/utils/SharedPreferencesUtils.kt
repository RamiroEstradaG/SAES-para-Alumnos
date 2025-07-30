package ziox.ramiro.saes.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow

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
        sharedPreferences.edit(commit = true) {
            when (T::class) {
                Int::class -> putInt(preferenceKeys.key, value as Int)
                Long::class -> putLong(preferenceKeys.key, value as Long)
                Float::class -> putFloat(preferenceKeys.key, value as Float)
                String::class -> putString(preferenceKeys.key, value as String)
                Boolean::class -> putBoolean(preferenceKeys.key, value as Boolean)
                else -> this
            }
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

    fun setAuthData(username: String, password: String){
        setPreference(PreferenceKeys.Boleta, username)
        setPreference(PreferenceKeys.Password, password)
        authData.value = AuthData(username, password)
    }

    val authData = MutableStateFlow(AuthData(
        getPreference(PreferenceKeys.Boleta, ""),
        getPreference(PreferenceKeys.Password, "")
    ))

    fun removeAuthData(){
        removePreference(PreferenceKeys.Boleta)
        removePreference(PreferenceKeys.Password)
        authData.value = AuthData("", "")
    }

    inline fun <reified T>removePreference(preferenceKeys: PreferenceKeys<T>){
        sharedPreferences.edit { remove(preferenceKeys.key) }
    }
}


data class AuthData(
    val username: String,
    val password: String
){
    fun isAuthDataSaved() : Boolean = username.isNotBlank() && password.isNotBlank()
}

sealed class PreferenceKeys<T>(val key: String) {
    object SchoolUrl: PreferenceKeys<String?>("new_url_escuela")
    object Boleta: PreferenceKeys<String>("boleta")
    object Password: PreferenceKeys<String>("pass")
    object QrUrl: PreferenceKeys<String>("qr_url")
    object OfflineMode: PreferenceKeys<Boolean>("offline_mode")
    object ScheduleWidgetLeveling: PreferenceKeys<Int>("widget_nivel")
    object DefaultNightMode: PreferenceKeys<Int?>("dark_mode")
    object IsFirebaseEnabled: PreferenceKeys<Boolean>("IsFirebaseEnabled")
}