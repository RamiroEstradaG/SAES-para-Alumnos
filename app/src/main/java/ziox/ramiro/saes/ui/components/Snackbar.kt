package ziox.ramiro.saes.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.ui.theme.getCurrentTheme

@ExperimentalMaterialApi
@Preview
@Composable
fun ErrorSnackbar(
    text: String = "Snackbar"
) = Snackbar(
    backgroundColor = getCurrentTheme().danger,
    shape = RoundedCornerShape(16.dp)
) {
    ListItem(
        icon = {
            Icon(
                modifier = Modifier
                    .size(36.dp),
                imageVector = Icons.Rounded.Warning,
                contentDescription = "Icono",
            )
        },
        text = {
            Text(text = "Error")
        },
        secondaryText = {
            Text(text = text)
        }
    )
}