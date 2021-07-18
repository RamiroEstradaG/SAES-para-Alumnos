package ziox.ramiro.saes.features.saes.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.twitter.sdk.android.core.Twitter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.repositories.AuthWebViewRepository
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.agenda.ui.screens.Agenda
import ziox.ramiro.saes.features.saes.features.ets.data.repositories.ETSWebViewRepository
import ziox.ramiro.saes.features.saes.features.ets.ui.screens.ETS
import ziox.ramiro.saes.features.saes.features.ets.view_models.ETSViewModel
import ziox.ramiro.saes.features.saes.features.ets_calendar.ui.screens.ETSCalendar
import ziox.ramiro.saes.features.saes.features.grades.ui.screens.Grades
import ziox.ramiro.saes.features.saes.features.home.ui.screens.Home
import ziox.ramiro.saes.features.saes.features.kardex.ui.screens.Kardex
import ziox.ramiro.saes.features.saes.features.occupancy.ui.screens.Occupancy
import ziox.ramiro.saes.features.saes.features.profile.data.repositories.ProfileWebViewRepository
import ziox.ramiro.saes.features.saes.features.profile.ui.screens.Profile
import ziox.ramiro.saes.features.saes.features.profile.view_models.ProfileViewModel
import ziox.ramiro.saes.features.saes.features.re_registration_appointment.ui.screens.ReRegistrationAppointment
import ziox.ramiro.saes.features.saes.features.schedule.ui.screens.Schedule
import ziox.ramiro.saes.features.saes.ui.components.BottomAppBar
import ziox.ramiro.saes.features.saes.ui.components.BottomSheetDrawerModal
import ziox.ramiro.saes.features.saes.view_models.MenuSection
import ziox.ramiro.saes.features.saes.view_models.SAESViewModel
import ziox.ramiro.saes.ui.screens.MainActivity
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.view_models.AuthEvent
import ziox.ramiro.saes.view_models.AuthState
import ziox.ramiro.saes.view_models.AuthViewModel

class SAESActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        viewModelFactory { AuthViewModel(AuthWebViewRepository(this)) }
    }

    private lateinit var profileViewModel : ProfileViewModel

    private val etsViewModel : ETSViewModel by viewModels {
        viewModelFactory { ETSViewModel(ETSWebViewRepository(this)) }
    }

    private val saesViewModel : SAESViewModel by viewModels{
        viewModelFactory { SAESViewModel(LocalAppDatabase.invoke(this).historyRepository()) }
    }

    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        profileViewModel = ViewModelProvider(
            this,
            viewModelFactory { ProfileViewModel(ProfileWebViewRepository(this)) }
        ).get(ProfileViewModel::class.java)

        Twitter.initialize(this)

        listenToAuthStates()
        listenToAuthEvents()
        listenToNavigationStates()

        setContent {
            SAESParaAlumnosTheme { uiController ->
                val selectedMenuItem = saesViewModel.currentSection.collectAsState(initial = SAESViewModel.SECTION_INITIAL)

                when(selectedMenuItem.value){
                    MenuSection.PROFILE -> uiController.setStatusBarColor(MaterialTheme.colors.surface)
                    else -> uiController.setStatusBarColor(Color.Transparent)
                }

                Scaffold(
                    bottomBar = {
                        BottomAppBar(
                            saesViewModel,
                            etsViewModel
                        ){
                            BottomSheetDrawerModal(
                                profileViewModel, saesViewModel, authViewModel
                            ).show(supportFragmentManager, "menu")
                        }
                    }
                ) {
                    Crossfade(targetState = selectedMenuItem.value) {
                        when(it){
                            MenuSection.HOME -> Home()
                            MenuSection.GRADES -> Grades()
                            MenuSection.SCHEDULE -> Schedule()
                            MenuSection.PROFILE -> Profile()
                            MenuSection.ETS -> ETS()
                            MenuSection.KARDEX -> Kardex()
                            MenuSection.ETS_CALENDAR -> ETSCalendar()
                            MenuSection.RE_REGISTRATION_APPOINTMENT -> ReRegistrationAppointment()
                            MenuSection.OCCUPANCY -> Occupancy()
                            MenuSection.AGENDA -> Agenda()
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if(saesViewModel.canGoBack()){
            saesViewModel.goBack()
        }else{
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        authViewModel.checkSession()
    }

    private fun listenToAuthEvents() = lifecycleScope.launch {
        authViewModel.events.collect {
            if(it is AuthEvent.LogoutSuccess){
                startActivity(Intent(this@SAESActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun listenToAuthStates() = lifecycleScope.launch {
        authViewModel.states.collect {
            if(it is AuthState.SessionCheckComplete){
                if (!it.isLoggedIn){
                    startActivity(Intent(this@SAESActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun listenToNavigationStates() = lifecycleScope.launch {
        saesViewModel.currentSection.collect {
            authViewModel.checkSession()
        }
    }
}

