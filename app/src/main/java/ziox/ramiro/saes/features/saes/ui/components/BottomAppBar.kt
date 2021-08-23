package ziox.ramiro.saes.features.saes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ziox.ramiro.saes.features.saes.features.ets.view_models.ETSViewModel
import ziox.ramiro.saes.features.saes.view_models.MenuSection
import ziox.ramiro.saes.features.saes.view_models.SAESViewModel
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.ui.theme.secondaryColor

@Composable
fun BottomAppBar(
    saesViewModel : SAESViewModel,
    etsViewModel : ETSViewModel,
    onMenuIconClick: () -> Unit = {}
){


    androidx.compose.material.BottomAppBar(
        backgroundColor = getCurrentTheme().toolbar
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuIconClick) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = "Menu",
                    tint = getCurrentTheme().onToolbar
                )
            }
            AppBarIconButton(section = MenuSection.HOME)
            AppBarIconButton(section = MenuSection.SCHEDULE)

            if(!etsViewModel.availableETS.value.isNullOrEmpty() || !etsViewModel.scores.value.isNullOrEmpty()){
                AppBarIconButton(section = MenuSection.ETS)
            }else{
                AppBarIconButton(section = MenuSection.GRADES)
            }

            AppBarIconButton(section = MenuSection.PROFILE)
        }
    }
}

@Composable
fun AppBarIconButton(
    saesViewModel: SAESViewModel = viewModel(),
    section: MenuSection
){
    val selectedItemMenu = saesViewModel.currentSection.collectAsState(initial = SAESViewModel.SECTION_INITIAL)

    IconButton(onClick = {
        saesViewModel.changeSection(section)
    }) {
        Icon(
            modifier = Modifier.size(if (selectedItemMenu.value == section) 32.dp else 24.dp),
            imageVector = section.icon,
            contentDescription = section.sectionName,
            tint = if (selectedItemMenu.value == section) {
                secondaryColor.s100
            } else getCurrentTheme().onToolbar
        )
    }
}