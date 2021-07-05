package ziox.ramiro.saes.ui.theme

import androidx.compose.material.darkColors
import androidx.compose.ui.graphics.Color

object DarkTheme : ComposableTheme {
    override val primaryText = Color(0xFFFFFFFF)
    override val secondaryText = Color(0xFFBFBFBF)
    override val dangerColor = Color.Red
    override val surfaceElevation = object : ColorElevation {
        override val e0 = Color(0xFF121212)
        override val e1 = Color(0xFF1e1e1e)
        override val e2 = Color(0xFF232323)
        override val e3 = Color(0xFF252525)
        override val e4 = Color(0xFF272727)
        override val e6 = Color(0xFF2c2c2c)
        override val e8 = Color(0xFF2e2e2e)
        override val e12 = Color(0xFF333333)
        override val e16 = Color(0xFF363636)
        override val e24 = Color(0xFF383838)
    }
    override val toolbarColor = surfaceElevation.e3
    override val colors = darkColors(
        primary = primaryColor.s500,
        primaryVariant = primaryColor.s700,
        secondary = secondaryColor.s500,
        secondaryVariant = secondaryColor.s700,
        background = Color(0xFFEDF0F3),
        onBackground = primaryText,
        surface = Color.White,
        onSurface = primaryText,
        error = Color(0xFFB00020),
        onError = Color.White,
        onPrimary = Color.White,
        onSecondary = Color.White
    )
}