package ziox.ramiro.saes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable


@Composable
fun getCurrentTheme(darkTheme: Boolean = isSystemInDarkTheme()) = if (darkTheme) DarkTheme else LightTheme

@Composable
fun SAESParaAlumnosTheme(content: @Composable () -> Unit) {
    val theme = getCurrentTheme()

    MaterialTheme(
        colors = theme.colors,
        typography = Typography(theme),
        content = content,
        shapes = Shapes
    )
}