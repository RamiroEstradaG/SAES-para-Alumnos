package ziox.ramiro.saes.ui.theme

import androidx.compose.material.Typography
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


fun Typography(theme: ComposableTheme) = Typography(
        h1 = TextStyle(
                fontFamily = Nunito,
                color = theme.primaryText,
                fontSize = 96.sp,
                fontWeight = FontWeight.Normal
        ),
        h2 = TextStyle(
                fontFamily = Nunito,
                color = theme.primaryText,
                fontSize = 60.sp,
                fontWeight = FontWeight.Normal
        ),
        h3 = TextStyle(
                fontFamily = Nunito,
                color = theme.primaryText,
                fontSize = 48.sp,
                fontWeight = FontWeight.Normal
        ),
        h4 = TextStyle(
                fontFamily = Nunito,
                color = theme.primaryText,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
        ),
        h5 = TextStyle(
                fontFamily = Nunito,
                color = theme.primaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
        ),
        h6 = TextStyle(
                fontFamily = Nunito,
                color = theme.primaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal
        ),
        subtitle1 = TextStyle(
                fontFamily = Nunito,
                color = theme.secondaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
        ),
        subtitle2 = TextStyle(
                fontFamily = Nunito,
                color = theme.secondaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
        ),
        body1 = TextStyle(
                fontFamily = Nunito,
                color = theme.secondaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
        ),
        body2 = TextStyle(
                fontFamily = Nunito,
                color = theme.secondaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
        ),
        button = TextStyle(
                fontFamily = Nunito,
                color = theme.colors.onPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
        ),
        caption = TextStyle(
                fontFamily = Nunito,
                color = theme.secondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.15.sp
        ),
        overline = TextStyle(
                fontFamily = Nunito,
                color = theme.primaryText,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
        ),
        defaultFontFamily = Nunito
)