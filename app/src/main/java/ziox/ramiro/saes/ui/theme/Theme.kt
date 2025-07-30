package ziox.ramiro.saes.ui.theme

import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import ziox.ramiro.saes.utils.activity

@Composable
fun getCurrentTheme(darkTheme: Boolean = isSystemInDarkTheme()) = if (darkTheme)
    DarkTheme(LocalContext.current)
else LightTheme(LocalContext.current)

@Composable
fun SAESParaAlumnosTheme(
    statusBarColor: Color = Color.Transparent,
    content: @Composable () -> Unit
) {
    val theme = getCurrentTheme()
    val activity = LocalContext.current.activity

    LaunchedEffect(activity) {
        activity?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                statusBarColor.toArgb(),
                statusBarColor.toArgb()
            )
        )
    }

    MaterialTheme(
        colorScheme = theme.colors,
        typography = Typography(theme),
        shapes = Shapes
    ) {
        content()
    }
}