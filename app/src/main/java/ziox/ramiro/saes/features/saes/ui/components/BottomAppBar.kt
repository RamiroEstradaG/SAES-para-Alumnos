package ziox.ramiro.saes.features.saes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.features.saes.presentation.MenuSection
import ziox.ramiro.saes.ui.theme.getCurrentTheme

@Preview
@Composable
fun BottomAppBar(
    selectedItemMenu: MutableState<MenuSection> = mutableStateOf(MenuSection.HOME)
){
    androidx.compose.material.BottomAppBar(
        backgroundColor = getCurrentTheme().toolbar
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = "Menu",
                    tint = getCurrentTheme().onToolbar
                )
            }
            IconButton(onClick = {
                selectedItemMenu.value = MenuSection.HOME
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
                selectedItemMenu.value = MenuSection.SCHEDULE
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
            IconButton(onClick = {
                selectedItemMenu.value = MenuSection.GRADES
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
            IconButton(onClick = {
                selectedItemMenu.value = MenuSection.PROFILE
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