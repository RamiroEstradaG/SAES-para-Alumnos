package ziox.ramiro.saes.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.LocalContext
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController


@Composable
fun getCurrentTheme(darkTheme: Boolean = isSystemInDarkTheme()) = if (darkTheme) DarkTheme else LightTheme

@Composable
fun glanceCurrentTheme() = if (LocalContext.current.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) ==  Configuration.UI_MODE_NIGHT_YES) DarkTheme else LightTheme

@Composable
fun SAESParaAlumnosTheme(
    statusBarColor: Color = Color.Transparent,
    content: @Composable (SystemUiController) -> Unit
) {
    val theme = getCurrentTheme()

    MaterialTheme(
        colors = theme.colors,
        typography = Typography(theme),
        shapes = Shapes
    ){
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

@Composable
fun GlanceTheme(
    content: @Composable () -> Unit
) {
    val theme = glanceCurrentTheme()

    MaterialTheme(
        colors = theme.colors,
        typography = Typography(theme),
        shapes = Shapes
    ){
        content()
    }
}