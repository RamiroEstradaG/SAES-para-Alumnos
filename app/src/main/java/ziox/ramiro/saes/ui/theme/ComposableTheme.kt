package ziox.ramiro.saes.ui.theme

import androidx.compose.material.*
import androidx.compose.ui.graphics.Color

val primaryColor = object : ColorShade {
    override val s50 = Color(0xFFE2F4FA)
    override val s100 = Color(0xFFB7E2F2)
    override val s200 = Color(0xFF8CCFE9)
    override val s300 = Color(0xFF68BDDF)
    override val s400 = Color(0xFF68BDDF)
    override val s500 = Color(0xFF49A2D3)
    override val s600 = Color(0xFF4394C5)
    override val s700 = Color(0xFF3B82B2)
    override val s800 = Color(0xFF36719D)
    override val s900 = Color(0xFF25537E)
}

val secondaryColor = object : ColorShade {
    override val s50 = Color(0xFFF9EAE8)
    override val s100 = Color(0xFFFBCFBE)
    override val s200 = Color(0xFFF79369)
    override val s300 = Color(0xFFF79369)
    override val s400 = Color(0xFFF67D48)
    override val s500 = Color(0xFFF56928)
    override val s600 = Color(0xFFEA6324)
    override val s700 = Color(0xFFDC5C20)
    override val s800 = Color(0xFFCE551D)
    override val s900 = Color(0xFFB54918)
}

interface ComposableTheme {
    val colors: Colors
    val primaryText: Color
    val secondaryText: Color
    val toolbarColor: Color
    val surfaceElevation: ColorElevation
}

interface ColorShade {
    val s50 : Color
    val s100 : Color
    val s200 : Color
    val s300 : Color
    val s400 : Color
    val s500 : Color
    val s600 : Color
    val s700 : Color
    val s800 : Color
    val s900 : Color
}

interface ColorElevation {
    val e0: Color
    val e1: Color
    val e2: Color
    val e3: Color
    val e4: Color
    val e6: Color
    val e8: Color
    val e12: Color
    val e16: Color
    val e24: Color
}