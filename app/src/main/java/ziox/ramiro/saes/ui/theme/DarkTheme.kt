package ziox.ramiro.saes.ui.theme

import androidx.compose.material.darkColors
import androidx.compose.ui.graphics.Color

object DarkTheme : ComposableTheme {
    override val primaryText = Color(0xFFFFFFFF)
    override val secondaryText = Color(0xFFB6B6B6)
    override val hintText = Color(0xFF686868)

    override val success = Color(0xFFA5D6A7)
    override val info = Color(0xFF81D4FA)
    override val warning = Color(0xFFFFE082)
    override val danger = Color(0xFFEF9A9A)

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

    override val toolbar = surfaceElevation.e4
    override val onToolbar = Color.White
    override val divider = Color(0xFF595959)

    override val colors = darkColors(
        primary = primaryColor.s200,
        primaryVariant = primaryColor.s500,
        onPrimary = Color.Black,

        secondary = secondaryColor.s200,
        secondaryVariant = secondaryColor.s500,
        onSecondary = Color.Black,

        background = surfaceElevation.e0,
        onBackground = primaryText,

        surface = surfaceElevation.e1,
        onSurface = primaryText,

        error = Color(0xFFE57373),
        onError = Color.Black
    )
}