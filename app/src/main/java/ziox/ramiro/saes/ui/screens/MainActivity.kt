package ziox.ramiro.saes.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.data.repositories.AuthWebViewRepository
import ziox.ramiro.saes.features.saes.ui.screens.SAESActivity
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.utils.*
import ziox.ramiro.saes.view_models.AuthState
import ziox.ramiro.saes.view_models.AuthViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        viewModelFactory { AuthViewModel(AuthWebViewRepository(this)) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listenToAuthStates()

        setContent {
            SAESParaAlumnosTheme {
                Scaffold {
                    SplashScreen()
                }
            }
        }
    }

    private fun listenToAuthStates() = lifecycleScope.launch {
        authViewModel.states.collect {
            if(it is AuthState.SessionCheckComplete){
                 if(it.isNotLoggedIn){
                     startActivity(
                         android.content.Intent(
                             this@MainActivity,
                             LoginActivity::class.java
                         )
                     )
                     finish()
                 }else{
                     startActivity(
                         android.content.Intent(
                             this@MainActivity,
                             SAESActivity::class.java
                         )
                     )
                     finish()
                 }
            }
        }
    }
}


