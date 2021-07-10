package ziox.ramiro.saes.features.saes.ui.components

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.flow.filter
import ziox.ramiro.saes.features.saes.features.ets.view_models.ETSState
import ziox.ramiro.saes.features.saes.features.ets.view_models.ETSViewModel
import ziox.ramiro.saes.features.saes.features.profile.view_models.ProfileViewModel
import ziox.ramiro.saes.features.saes.view_models.MenuSection
import ziox.ramiro.saes.features.saes.view_models.SAESViewModel
import ziox.ramiro.saes.ui.theme.getCurrentTheme

fun getFragmentManager(context: Context): FragmentManager?{
    return when (context) {
        is AppCompatActivity -> context.supportFragmentManager
        is ContextThemeWrapper -> getFragmentManager(context.baseContext)
        else -> null
    }
}


@Composable
fun BottomAppBar(
    saesViewModel : SAESViewModel,
    profileViewModel: ProfileViewModel,
    etsViewModel: ETSViewModel
){
    val fragmentManager = getFragmentManager(LocalContext.current)!!
    val selectedItemMenu = saesViewModel.currentSection.collectAsState(initial = SAESViewModel.SECTION_INITIAL)
    val etsState = etsViewModel.filterStates(ETSState.ScoresComplete::class, ETSState.ETSComplete::class)

    androidx.compose.material.BottomAppBar(
        backgroundColor = getCurrentTheme().toolbar
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                BottomSheetDrawerModal(
                    profileViewModel, saesViewModel
                ).show(fragmentManager, "menu")
            }) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = "Menu",
                    tint = getCurrentTheme().onToolbar
                )
            }
            IconButton(onClick = {
                saesViewModel.changeSection(MenuSection.HOME)
            }) {
                Icon(
                    modifier = Modifier.size(if (selectedItemMenu.value == MenuSection.HOME) 32.dp else 24.dp),
                    imageVector = Icons.Rounded.Home,
                    contentDescription = "Home",
                    tint = if (selectedItemMenu.value == MenuSection.HOME) {
                        MaterialTheme.colors.secondary
                    } else getCurrentTheme().onToolbar
                )
            }
            IconButton(onClick = {
                saesViewModel.changeSection(MenuSection.SCHEDULE)
            }) {
                Icon(
                    modifier = Modifier.size(if (selectedItemMenu.value == MenuSection.SCHEDULE) 32.dp else 24.dp),
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = "Schedule",
                    tint = if (selectedItemMenu.value == MenuSection.SCHEDULE) {
                        MaterialTheme.colors.secondary
                    } else getCurrentTheme().onToolbar
                )
            }

            if(!(etsState.value as? ETSState.ETSComplete)?.etsList.isNullOrEmpty() || !(etsState.value as? ETSState.ScoresComplete)?.scores.isNullOrEmpty()){
                IconButton(onClick = {
                    saesViewModel.changeSection(MenuSection.ETS)
                }) {
                    Text(
                        text = "ETS",
                        color = if (selectedItemMenu.value == MenuSection.ETS) {
                            MaterialTheme.colors.secondary
                        } else getCurrentTheme().onToolbar,
                        style = MaterialTheme.typography.h5,
                        fontSize = if (selectedItemMenu.value == MenuSection.ETS) 18.sp else 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }else{
                IconButton(onClick = {
                    saesViewModel.changeSection(MenuSection.GRADES)
                }) {
                    Icon(
                        modifier = Modifier.size(if (selectedItemMenu.value == MenuSection.GRADES) 32.dp else 24.dp),
                        imageVector = Icons.Rounded.FactCheck,
                        contentDescription = "Grades",
                        tint = if (selectedItemMenu.value == MenuSection.GRADES) {
                            MaterialTheme.colors.secondary
                        } else getCurrentTheme().onToolbar
                    )
                }
            }
            IconButton(onClick = {
                saesViewModel.changeSection(MenuSection.PROFILE)
            }) {
                Icon(
                    modifier = Modifier.size(if (selectedItemMenu.value == MenuSection.PROFILE) 32.dp else 24.dp),
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Profile",
                    tint = if (selectedItemMenu.value == MenuSection.PROFILE) {
                        MaterialTheme.colors.secondary
                    } else getCurrentTheme().onToolbar
                )
            }
        }
    }
}