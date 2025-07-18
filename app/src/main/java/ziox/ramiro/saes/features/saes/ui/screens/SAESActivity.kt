package ziox.ramiro.saes.features.saes.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import ziox.ramiro.saes.R
import ziox.ramiro.saes.features.saes.features.agenda.ui.screens.Agenda
import ziox.ramiro.saes.features.saes.features.ets.ui.screens.ETS
import ziox.ramiro.saes.features.saes.features.ets.view_models.ETSViewModel
import ziox.ramiro.saes.features.saes.features.ets_calendar.ui.screens.ETSCalendar
import ziox.ramiro.saes.features.saes.features.grades.ui.screens.Grades
import ziox.ramiro.saes.features.saes.features.grades.view_models.GradesViewModel
import ziox.ramiro.saes.features.saes.features.home.ui.screens.Home
import ziox.ramiro.saes.features.saes.features.kardex.ui.screens.Kardex
import ziox.ramiro.saes.features.saes.features.occupancy.ui.screens.Occupancy
import ziox.ramiro.saes.features.saes.features.performance.ui.screens.Performance
import ziox.ramiro.saes.features.saes.features.profile.ui.screens.Profile
import ziox.ramiro.saes.features.saes.features.profile.view_models.ProfileViewModel
import ziox.ramiro.saes.features.saes.features.re_registration_appointment.ui.screens.ReRegistrationAppointment
import ziox.ramiro.saes.features.saes.features.schedule.ui.screens.Schedule
import ziox.ramiro.saes.features.saes.features.school_schedule.ui.screens.SchoolSchedule
import ziox.ramiro.saes.features.saes.ui.components.BottomAppBar
import ziox.ramiro.saes.features.saes.ui.components.BottomSheetDrawerModal
import ziox.ramiro.saes.features.saes.view_models.MenuSection
import ziox.ramiro.saes.features.saes.view_models.SAESViewModel
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.screens.MainActivity
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.view_models.AuthViewModel
import ziox.ramiro.saes.view_models.BillingViewModel

@AndroidEntryPoint
class SAESActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    private val profileViewModel: ProfileViewModel by viewModels()

    private val etsViewModel: ETSViewModel by viewModels()

    private val saesViewModel: SAESViewModel by viewModels()

    private val billingViewModel: BillingViewModel by viewModels()

    private val gradesViewModel: GradesViewModel by viewModels()

    companion object {
        const val INTENT_EXTRA_REDIRECT = "redirect"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val initialSection = try {
            MenuSection.valueOf(intent.getStringExtra(INTENT_EXTRA_REDIRECT)!!)
        } catch (_: Exception) {
            SAESViewModel.SECTION_INITIAL.name
        }

        listenToNavigationStates()

        onBackPressedDispatcher.addCallback(this) {
            if (saesViewModel.canGoBack()) {
                saesViewModel.goBack()
            } else {
                finish()
            }
        }

        setContent {
            SAESParaAlumnosTheme { uiController ->
                val selectedMenuItem =
                    saesViewModel.currentSection.collectAsState(initial = initialSection)

                val statusBarColor = when (selectedMenuItem.value) {
                    MenuSection.PROFILE -> MaterialTheme.colorScheme.surface
                    else -> Color.Transparent
                }

                val hasDonated = billingViewModel.hasDonated.collectAsState(initial = false)

                uiController.setStatusBarColor(statusBarColor)

                if (authViewModel.isLoggedIn.value == false) {
                    startActivity(Intent(this@SAESActivity, MainActivity::class.java))
                    finish()
                }

                Scaffold(
                    bottomBar = {
                        BottomAppBar(
                            saesViewModel,
                            etsViewModel
                        ) {
                            BottomSheetDrawerModal().show(supportFragmentManager, "menu")
                        }
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        if (!hasDonated.value) {
                            AndroidView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .background(statusBarColor),
                                factory = {
                                    AdView(it).apply {
                                        setAdSize(AdSize.BANNER)
                                        adUnitId = getString(R.string.banner_ad_key)
                                        loadAd(AdRequest.Builder().build())
                                    }
                                }
                            )
                        }
                        Crossfade(targetState = selectedMenuItem.value) {
                            when (it) {
                                MenuSection.HOME -> Home()
                                MenuSection.GRADES -> Grades(gradesViewModel)
                                MenuSection.SCHEDULE -> Schedule()
                                MenuSection.PROFILE -> Profile()
                                MenuSection.ETS -> ETS()
                                MenuSection.KARDEX -> Kardex()
                                MenuSection.ETS_CALENDAR -> ETSCalendar()
                                MenuSection.RE_REGISTRATION_APPOINTMENT -> ReRegistrationAppointment()
                                MenuSection.OCCUPANCY -> Occupancy()
                                MenuSection.AGENDA -> Agenda()
                                MenuSection.SCHOOL_SCHEDULE -> SchoolSchedule()
                                MenuSection.PERFORMANCE -> Performance()
                            }
                        }
                    }
                }
                ErrorSnackbar(
                    errorState = listOf(
                        authViewModel.error,
                        profileViewModel.error
                    ).merge()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        authViewModel.checkSession()
        billingViewModel.refetch()
    }

    private fun listenToNavigationStates() = lifecycleScope.launch {
        saesViewModel.currentSection.collect {
            authViewModel.checkSession()
        }
    }
}
