package ziox.ramiro.saes.features.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.Button
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.AuthRepository
import ziox.ramiro.saes.data.AuthWebViewRepository
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.presentation.MainActivity
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.view_models.AuthEvent
import ziox.ramiro.saes.view_models.AuthViewModel

class SAESActivity : ComponentActivity() {
    val authViewModel: AuthViewModel by viewModels {
        viewModelFactory { AuthViewModel(AuthWebViewRepository(this)) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listenToAuthEvents()

        setContent {
            SAESParaAlumnosTheme {
                Button(onClick = {
                    authViewModel.logout()
                }) {

                }
            }
        }
    }

    private fun listenToAuthEvents() = lifecycleScope.launch {
        authViewModel.events.collect {
            if(it is AuthEvent.LogoutSuccess){
                startActivity(Intent(this@SAESActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}

