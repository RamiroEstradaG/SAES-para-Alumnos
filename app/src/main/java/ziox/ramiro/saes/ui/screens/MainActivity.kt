package ziox.ramiro.saes.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material.Scaffold
import androidx.lifecycle.ViewModelProvider
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.data.repositories.AuthWebViewRepository
import ziox.ramiro.saes.features.saes.ui.screens.SAESActivity
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.isUrl
import ziox.ramiro.saes.view_models.AuthViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userPreferences = UserPreferences.invoke(this)

        Twitter.initialize(TwitterConfig.Builder(this)
            .logger(DefaultLogger(Log.INFO))
            .twitterAuthConfig(TwitterAuthConfig(
                getString(R.string.com_twitter_sdk_android_CONSUMER_KEY),
                getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET)
            ))
            .debug(true)
            .build())

        handleIntent(userPreferences)

        userPreferences.setPreference(PreferenceKeys.OfflineMode, false)
        AppCompatDelegate.setDefaultNightMode(when(userPreferences.getPreference(PreferenceKeys.DefaultNightMode, null)){
            1 -> AppCompatDelegate.MODE_NIGHT_NO
            2 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        })

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
                        Uri.parse(intent.getStringExtra(SAESActivity.INTENT_EXTRA_REDIRECT))
                    )
                )
            }
        }
    }
}


