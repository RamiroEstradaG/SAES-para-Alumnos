package ziox.ramiro.saes.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.lifecycle.ViewModelProvider
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.data.repositories.AuthWebViewRepository
import ziox.ramiro.saes.features.saes.ui.screens.SAESActivity
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.view_models.AuthViewModel

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userPreferences = UserPreferences.invoke(this)

        userPreferences.setPreference(PreferenceKeys.OfflineMode, false)

        authViewModel = ViewModelProvider(
            this,
            viewModelFactory { AuthViewModel(AuthWebViewRepository(this)) }
        ).get(AuthViewModel::class.java)

        if(userPreferences.getPreference(PreferenceKeys.SchoolUrl, null) == null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        setContent {
            SAESParaAlumnosTheme {
                authViewModel.isLoggedIn.value?.let {
                    if(it){
                        startActivity(Intent(this@MainActivity, SAESActivity::class.java))
                    }else{
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    }
                    finish()
                }

                Scaffold {
                    SplashScreen()
                }
            }
        }
    }
}


