package ziox.ramiro.saes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
@Composable
fun getCurrentTheme(darkTheme: Boolean = isSystemInDarkTheme()) = if (darkTheme)
    DarkTheme(LocalContext.current)
else LightTheme(LocalContext.current)

@Composable
fun SAESParaAlumnosTheme(
    statusBarColor: Color = Color.Transparent,
    content: @Composable (SystemUiController) -> Unit
) {
    val theme = getCurrentTheme()

    MaterialTheme(
        colorScheme = theme.colors,
        typography = Typography(theme),
        shapes = Shapes
    ) {
        val uiController = rememberSystemUiController()

        uiController.setStatusBarColor(
            statusBarColor,
            !isSystemInDarkTheme()
        )

        uiController.setNavigationBarColor(
            theme.toolbar
        )

        content(uiController)
    }
}