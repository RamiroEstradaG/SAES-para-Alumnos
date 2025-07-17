package ziox.ramiro.saes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ziox.ramiro.saes.R

val Nunito = FontFamily(
        Font(
                R.font.nunito_extralight,
                style = FontStyle.Normal,
                weight = FontWeight.ExtraLight
        ),
        Font(
                R.font.nunito_extralightitalic,
                style = FontStyle.Italic,
                weight = FontWeight.ExtraLight
        ),
        Font(
                R.font.nunito_light,
                style = FontStyle.Normal,
                weight = FontWeight.Light
        ),
        Font(
                R.font.nunito_lightitalic,
                style = FontStyle.Italic,
                weight = FontWeight.Light
        ),
        Font(
                R.font.nunito_regular,
                style = FontStyle.Normal,
                weight = FontWeight.W400
        ),
        Font(
                R.font.nunito_italic,
                style = FontStyle.Italic,
                weight = FontWeight.W400
        ),
        Font(
                R.font.nunito_semibold,
                style = FontStyle.Normal,
                weight = FontWeight.SemiBold
        ),
        Font(
                R.font.nunito_semibolditalic,
                style = FontStyle.Italic,
                weight = FontWeight.SemiBold
        ),
        Font(
                R.font.nunito_bold,
                style = FontStyle.Normal,
                weight = FontWeight.Bold
        ),
        Font(
                R.font.nunito_bolditalic,
                style = FontStyle.Italic,
                weight = FontWeight.Bold
        ),
        Font(
                R.font.nunito_extrabold,
                style = FontStyle.Normal,
                weight = FontWeight.ExtraBold
        ),
        Font(
                R.font.nunito_extrabolditalic,
                style = FontStyle.Italic,
                weight = FontWeight.ExtraBold
        ),
        Font(
                R.font.nunito_black,
                style = FontStyle.Normal,
                weight = FontWeight.Black
        ),
        Font(
                R.font.nunito_blackitalic,
                style = FontStyle.Italic,
                weight = FontWeight.Black
        ),
)

val OpenSans = FontFamily(
        Font(
                R.font.opensans_light,
                style = FontStyle.Normal,
                weight = FontWeight.Light
        ),
        Font(
                R.font.opensans_lightitalic,
                style = FontStyle.Italic,
                weight = FontWeight.Light
        ),
        Font(
                R.font.opensans_regular,
                style = FontStyle.Normal,
                weight = FontWeight.W400
        ),
        Font(
                R.font.opensans_italic,
                style = FontStyle.Italic,
                weight = FontWeight.W400
        ),
        Font(
                R.font.opensans_semibold,
                style = FontStyle.Normal,
                weight = FontWeight.SemiBold
        ),
        Font(
                R.font.opensans_semibolditalic,
                style = FontStyle.Italic,
                weight = FontWeight.SemiBold
        ),
        Font(
                R.font.opensans_bold,
                style = FontStyle.Normal,
                weight = FontWeight.Bold
        ),
        Font(
                R.font.opensans_bolditalic,
                style = FontStyle.Italic,
                weight = FontWeight.Bold
        ),
        Font(
                R.font.opensans_extrabold,
                style = FontStyle.Normal,
                weight = FontWeight.ExtraBold
        ),
        Font(
                R.font.opensans_extrabolditalic,
                style = FontStyle.Italic,
                weight = FontWeight.ExtraBold
        ),
)

fun Typography(theme: ComposableTheme) = Typography(
        displayLarge = TextStyle(
                fontFamily = OpenSans,
                color = theme.primaryText,
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold
        ),
        displayMedium = TextStyle(
                fontFamily = OpenSans,
                color = theme.primaryText,
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold
        ),
        displaySmall = TextStyle(
                fontFamily = OpenSans,
                color = theme.primaryText,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
        ),
        headlineLarge = TextStyle(
                fontFamily = OpenSans,
                color = theme.primaryText,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
        ),
        headlineMedium = TextStyle(
                fontFamily = OpenSans,
                color = theme.primaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
        ),
        headlineSmall = TextStyle(
                fontFamily = OpenSans,
                color = theme.primaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal
        ),
        titleLarge = TextStyle(
                fontFamily = OpenSans,
                color = theme.secondaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
        ),
        titleMedium = TextStyle(
                fontFamily = OpenSans,
                color = theme.secondaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
        ),
        bodyLarge = TextStyle(
                fontFamily = Nunito,
                color = theme.secondaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
        ),
        bodyMedium = TextStyle(
                fontFamily = Nunito,
                color = theme.secondaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
        ),
        labelLarge = TextStyle(
                fontFamily = Nunito,
                color = theme.primaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
        ),
        labelMedium = TextStyle(
                fontFamily = Nunito,
                color = theme.secondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.15.sp
        ),
        labelSmall = TextStyle(
                fontFamily = Nunito,
                color = theme.primaryText,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
        )
)