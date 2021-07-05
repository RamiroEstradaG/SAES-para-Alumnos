package ziox.ramiro.saes.features.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.AuthRepository
import ziox.ramiro.saes.data.AuthWebViewRepository
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.presentation.features.home.presentation.Home
import ziox.ramiro.saes.presentation.MainActivity
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.view_models.AuthEvent
import ziox.ramiro.saes.view_models.AuthViewModel

class SAESActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        viewModelFactory { AuthViewModel(AuthWebViewRepository(this)) }
    }

    private val selectedMenuItem = mutableStateOf(MenuSection.HOME)

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


@Composable
fun PageController(
    selectedItemMenu: MutableState<MenuSection> = mutableStateOf(MenuSection.HOME)
) = Crossfade(targetState = selectedItemMenu.value) {
    when(it){
        MenuSection.HOME -> Home()
        else -> {
            Box{}
        }
    }
}


@Preview
@Composable
fun BottomAppBar(
    selectedItemMenu: MutableState<MenuSection> = mutableStateOf(MenuSection.HOME)
){
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = "Menu"
                )
            }
            IconButton(onClick = {
                selectedItemMenu.value = MenuSection.HOME
            }) {
                Icon(
                    modifier = Modifier.size(if (selectedItemMenu.value == MenuSection.HOME) 32.dp else 24.dp),
                    imageVector = Icons.Rounded.Home,
                    contentDescription = "Home",
                    tint = if (selectedItemMenu.value == MenuSection.HOME){
                        MaterialTheme.colors.secondary
                    }else MaterialTheme.colors.onPrimary
                )
            }
            IconButton(onClick = {
                selectedItemMenu.value = MenuSection.SCHEDULE
            }) {
                Icon(
                    modifier = Modifier.size(if (selectedItemMenu.value == MenuSection.SCHEDULE) 32.dp else 24.dp),
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = "Schedule",
                    tint = if (selectedItemMenu.value == MenuSection.SCHEDULE){
                        MaterialTheme.colors.secondary
                    }else MaterialTheme.colors.onPrimary
                )
            }
            IconButton(onClick = {
                selectedItemMenu.value = MenuSection.GRADES
            }) {
                Icon(
                    modifier = Modifier.size(if (selectedItemMenu.value == MenuSection.GRADES) 32.dp else 24.dp),
                    imageVector = Icons.Rounded.FactCheck,
                    contentDescription = "Grades",
                    tint = if (selectedItemMenu.value == MenuSection.GRADES){
                        MaterialTheme.colors.secondary
                    }else MaterialTheme.colors.onPrimary
                )
            }
            IconButton(onClick = {
                selectedItemMenu.value = MenuSection.PROFILE
            }) {
                Icon(
                    modifier = Modifier.size(if (selectedItemMenu.value == MenuSection.PROFILE) 32.dp else 24.dp),
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Profile",
                    tint = if (selectedItemMenu.value == MenuSection.PROFILE){
                        MaterialTheme.colors.secondary
                    }else MaterialTheme.colors.onPrimary
                )
            }
        }
    }
}