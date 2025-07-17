package ziox.ramiro.saes.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.google.android.gms.ads.MobileAds
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.data.repositories.AuthWebViewRepository
import ziox.ramiro.saes.features.saes.data.repositories.StorageFirebaseRepository
import ziox.ramiro.saes.features.saes.ui.screens.SAESActivity
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.isUrl
import ziox.ramiro.saes.view_models.AuthViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    companion object {
        @Volatile var context: Context? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initServices()
        context = this

        val userPreferences = UserPreferences.invoke(this)
        handleIntent(userPreferences)

        userPreferences.setPreference(PreferenceKeys.OfflineMode, false)

        AppCompatDelegate.setDefaultNightMode(
            userPreferences.getPreference(PreferenceKeys.DefaultNightMode, null) ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )

        if(userPreferences.getPreference(PreferenceKeys.SchoolUrl, null) == null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        setContent {
            SAESParaAlumnosTheme {
                LaunchedEffect(authViewModel.isLoggedIn){
                    snapshotFlow { authViewModel.isLoggedIn.value }.collect {
                        it?.let {
                            if(it){
                                startActivity(Intent(this@MainActivity, SAESActivity::class.java))
                            }else{
                                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            }
                            finish()
                        }
                    }
                }

                Scaffold { paddingValues ->
                    SplashScreen(
                        modifier = Modifier.padding(paddingValues)
                    )
                    ErrorSnackbar(authViewModel.error)
                }
            }
        }
    }

    private fun initServices(){
        Firebase.remoteConfig.apply {
            setConfigSettingsAsync(remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            })
        }.fetchAndActivate()
        MobileAds.initialize(this)
    }

    private fun handleIntent(userPreferences: UserPreferences){
        if (intent.action == Intent.ACTION_VIEW) {
            if (intent.data?.host?.matches(Regex("www\\.saes\\.[a-z]+\\.ipn\\.mx")) == true) {
                val url = "${intent.data?.scheme}://${intent.data?.host}/"
                userPreferences.setPreference(PreferenceKeys.SchoolUrl, url)
            }
        }
        if(intent.hasExtra(SAESActivity.INTENT_EXTRA_REDIRECT)){

            if(intent.getStringExtra(SAESActivity.INTENT_EXTRA_REDIRECT)!!.isUrl()){
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        intent.getStringExtra(SAESActivity.INTENT_EXTRA_REDIRECT)?.toUri()
                    )
                )
            }
        }
    }
}


