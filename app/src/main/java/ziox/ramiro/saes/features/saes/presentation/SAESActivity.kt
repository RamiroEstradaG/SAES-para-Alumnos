package ziox.ramiro.saes.features.saes.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.AuthWebViewRepository
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.grades.presentation.Grades
import ziox.ramiro.saes.features.saes.features.home.presentation.Home
import ziox.ramiro.saes.features.saes.features.schedule.presentation.Schedule
import ziox.ramiro.saes.features.saes.ui.components.BottomAppBar
import ziox.ramiro.saes.presentation.MainActivity
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.view_models.AuthEvent
import ziox.ramiro.saes.view_models.AuthViewModel

class SAESActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        viewModelFactory { AuthViewModel(AuthWebViewRepository(this)) }
    }

    private val selectedMenuItem = mutableStateOf(MenuSection.SCHEDULE)

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listenToAuthEvents()

        setContent {
            SAESParaAlumnosTheme {
                Scaffold(
                    bottomBar = {
                        BottomAppBar(selectedMenuItem)
                    }
                ) {
                    PageController(selectedMenuItem)
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

enum class MenuSection{
    HOME,
    SCHEDULE,
    GRADES,
    PROFILE
}


@ExperimentalMaterialApi
@Composable
fun PageController(
    selectedItemMenu: MutableState<MenuSection> = mutableStateOf(MenuSection.HOME)
) = Crossfade(targetState = selectedItemMenu.value) {
    when(it){
        MenuSection.HOME -> Home()
        MenuSection.GRADES -> Grades()
        MenuSection.SCHEDULE -> Schedule()
        else -> {
            Box{}
        }
    }
}