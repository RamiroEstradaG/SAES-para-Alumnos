package ziox.ramiro.saes.features.about.ui.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.google.accompanist.imageloading.rememberDrawablePainter
import com.google.android.play.core.review.ReviewManagerFactory
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.data.repositories.BillingGooglePayRepository
import ziox.ramiro.saes.ui.components.BaseButton
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.launchUrl
import ziox.ramiro.saes.view_models.BillingViewModel


class AboutActivity : AppCompatActivity() {
    private val billingViewModel: BillingViewModel by viewModels {
        viewModelFactory { BillingViewModel(BillingGooglePayRepository(this)) }
    }


    @ExperimentalTextApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val showSAESPrivacyPolicy = remember {
                mutableStateOf(false)
            }

            val showLicences = remember {
                mutableStateOf(false)
            }

            val showDonation = remember {
                mutableStateOf(false)
            }

            SAESParaAlumnosTheme {
                Scaffold {
                    Box(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ){
                        Column(
                            modifier = Modifier.padding(
                                vertical = 16.dp,
                                horizontal = 32.dp
                            )
                        ) {
                            AppInfoItem()
                            AboutItem(
                                leading = rememberVectorPainter(image = Icons.Rounded.Warning),
                                text = "La aplicación no está asociada al Instituto Politécnico Nacional (IPN) ni a sus unidades académicas. El desarrollo es independiente y está siendo manejado por estudiantes del IPN.",
                                backgroundColor = getCurrentTheme().danger,
                                contentColor = MaterialTheme.colors.onPrimary
                            )
                            if(billingViewModel.hasDonated.value == false){
                                AboutItem(
                                    leading = rememberVectorPainter(image = Icons.Rounded.Campaign),
                                    text = "Quitar publicidad",
                                    isHighEmphasis = true,
                                    backgroundColor = Color.Red,
                                    contentColor = Color.White
                                ) {
                                    showDonation.value = true
                                }
                            }
                            AboutItem(
                                leading = painterResource(id = R.drawable.ic_octicons_mark_github),
                                text = "Código fuente",
                                isHighEmphasis = true,
                                backgroundColor = Color(0xFF24292e),
                                contentColor = Color.White
                            ) {
                                launchUrl("https://github.com/RamiroEstradaG/SAES-para-Alumnos")
                            }
                            AboutItem(
                                leading = painterResource(id = R.drawable.ic_trello_mark_white),
                                text = "Lista de tareas",
                                isHighEmphasis = true,
                                backgroundColor = Color(0xFF0079BF),
                                contentColor = Color.White
                            ) {
                                launchUrl("https://trello.com/b/bYPns3O2")
                            }
                            AboutItem(
                                leading = rememberVectorPainter(image = Icons.Rounded.Reviews),
                                text = "Escribir una reseña",
                                isHighEmphasis = true,
                                backgroundColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            ) {
                                val manager = ReviewManagerFactory.create(this@AboutActivity)
                                manager.requestReviewFlow().addOnCompleteListener { request ->
                                    if (request.isSuccessful) {
                                        val reviewInfo = request.result
                                        manager.launchReviewFlow(this@AboutActivity, reviewInfo)
                                    }
                                }
                            }
                            AboutItem(leading = rememberVectorPainter(image = Icons.Rounded.BugReport), text = "Reportar un bug") {
                                launchUrl("https://github.com/RamiroEstradaG/SAES-para-Alumnos/issues/new?labels=bug&template=issue.md&title=%5BFECHA+EN+YY-MM-DD%5D%3A+%5BTITULO+DEL+ISSUE%5D")
                            }
                            AboutItem(leading = rememberVectorPainter(image = Icons.Rounded.NewReleases), text = "Solicitar una característica") {
                                launchUrl("https://github.com/RamiroEstradaG/SAES-para-Alumnos/issues/new?labels=feature&template=feature.md&title=%5BFECHA+EN+YY-MM-DD%5D%3A+%5BTITULO+DEL+ISSUE%5D")
                            }
                            AboutItem(leading = rememberVectorPainter(image = Icons.Rounded.Policy), text = "Politica de privacidad de la aplicación") {
                                launchUrl("https://ramiroestradag.github.io/SAES-para-Alumnos/Politica/")
                            }
                            AboutItem(leading = rememberVectorPainter(image = Icons.Rounded.Policy), text = "Politica de privacidad del SAES") {
                                showSAESPrivacyPolicy.value = true
                            }
                            AboutItem(leading = rememberVectorPainter(image = Icons.Rounded.AlternateEmail), text = "Contacto (sólo dudas de la app)") {
                                val email = Intent(Intent.ACTION_SEND)
                                email.putExtra(Intent.EXTRA_EMAIL, arrayOf("ramiroestradag@gmail.com"))
                                email.putExtra(Intent.EXTRA_SUBJECT, "SAES para Alumnos")
                                email.putExtra(Intent.EXTRA_TEXT, "Para dudas del SAES como no poder iniciar sesión, calificaciones, inscripción y resultados de ETS y reinscripciones contactar al departamento de Gestión Escolar de tu escuela.")

                                email.type = "message/rfc822"

                                startActivity(email)
                            }
                            AboutItem(leading = rememberVectorPainter(image = Icons.Rounded.Copyright), text = "Licencias y créditos") {
                                showLicences.value = true
                            }
                        }
                    }

                    if(showDonation.value){
                        AlertDialog(
                            onDismissRequest = {
                                showDonation.value = false
                            },
                            title = {
                                Text(text = "Quitar publicidad")
                            },
                            text = {
                                Column {
                                    Text(text = """
                                        No hay diferencia entre las dos compras.
                                        La compra se realiza una sola vez e incluye:
                                        • Aplicación sin publicidad vinculado a Google Play.
                                        • Apoyo a los desarrolladores.
                                    """.trimIndent())

                                }
                            },
                            buttons = {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    BaseButton(
                                        text = "\$20"
                                    ){
                                        billingViewModel.purchaceProduct("01_saes_donation_20")
                                    }
                                    BaseButton(
                                        text = "\$50"
                                    ){
                                        billingViewModel.purchaceProduct("02_saes_donation_50")
                                    }
                                }
                            }
                        )
                    }

                    if(showSAESPrivacyPolicy.value){
                        AlertDialog(
                            text = {
                                Text(text = "La direccion de Administración Escolar (DAE) le informa que los Datos Personales proporcionados por usted son protegidos, incorporados y tratados en el ''Sistema de Administracion Escolar'' (SAES), el cual fue registrado en el Listado de Sistemas de Datos Personales ante el Instituto Federal de Acceso a la Información Pública (IFAI) (www.ifai.org.mx), con fundamento en los Articulos 20 y 21 de la Ley Federal de Transparencia y Acceso a la Información Pública Gubernamental (LFTAIPG), y demás disposiciones aplicables y podrán ser proporcionados a dependencias del Instituto Politécnico Nacional y autoridades competentes, con la finalidad de coadyuvar al ejercicio de las funciones propias de la Institución, además de otra información prevista en la ley. La Unidad Administrativa Responsable del SAES es la DAE, en la cual podrá ejercer los derechos de acceso y corrección. Sita en la ''Unidad Profesional Adolfo López Mateos'', Av. Instituto Politécnico Nacional No. 1936, Col. Zacatenco, México, D.F., CP. 07738. Lo anterior con base al décimo séptimo Lineamiento de Protección de Datos Personales publicados en el Diario Oficial de la Federación (D.O.F. 30 de septiembre de 2005)")
                            },
                            onDismissRequest = {
                                showSAESPrivacyPolicy.value = false
                            },
                            buttons = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    ziox.ramiro.saes.ui.components.TextButton(
                                        text = "OK",
                                        textColor = getCurrentTheme().info
                                    ){
                                        showSAESPrivacyPolicy.value = false
                                    }
                                }
                            }
                        )
                    }
                    if(showLicences.value){
                        Dialog(
                            onDismissRequest = {
                                showLicences.value = false
                            }
                        ){
                            Card(
                                modifier = Modifier
                                    .height(500.dp)
                                    .fillMaxWidth()
                            ){
                                Column {
                                    Text(
                                        modifier = Modifier.padding(16.dp),
                                        text = "Licencias",
                                        style = MaterialTheme.typography.h5,
                                    )
                                    Column(
                                        Modifier
                                            .padding(horizontal = 16.dp)
                                            .verticalScroll(
                                                rememberScrollState()
                                            )
                                    ) {
                                        LicenceItem("Accompanist Library", "The Android Open Source Project", "2020", "https://github.com/google/accompanist")
                                        LicenceItem("ZXing Android Embedded", "Journey Mobile", "2012-2018", "https://github.com/journeyapps/zxing-android-embedded")
                                        LicenceItem("MPAndroidChart", "Philipp Jahoda", "2020", "https://github.com/PhilJay/MPAndroidChart")
                                        LicenceItem("Retrofit", "Square, Inc", "2013", "https://square.github.io/retrofit/")
                                        LicenceItem("Moshi", "Square, Inc", "2015", "https://github.com/square/moshi")
                                        LicenceItem("Tweet UI", "ahmedrizwan", "2020", "https://github.com/ahmedrizwan/JetpackComposeTwitter")
                                        LicenceItem("Room", "The Android Open Source Project", "2018", "https://developer.android.com/jetpack/androidx/releases/room")
                                        LicenceItem("Google Play Core Library", "The Android Open Source Project", "2018", "https://developer.android.com/reference/com/google/android/play/core/release-notes")
                                        LicenceItem("Material Design Icons", "Google, Inc", "2016", "https://github.com/google/material-design-icons")
                                        LicenceItem("Android Jetpack Compose", "The Android Open Source Project", "2021", "https://developer.android.com/jetpack/androidx/releases/compose")
                                        LicenceItem("Kotlin Programming Language", "Jetbrains s.r.o. and contributors", "2000-2019", "https://github.com/JetBrains/kotlin")
                                        LicenceItem("Sistema de Administración Escolar", "Instituto Politécnico Nacional, Dirección de Administración Escolar", "2008", "https://www.ipn.mx/saes/")
                                    }
                                }

                            }
                        }
                    }
                }
                ErrorSnackbar(billingViewModel.error)
            }
        }
    }
}

@ExperimentalTextApi
@Composable
fun LicenceItem(
    itemName: String,
    author: String,
    year: String,
    url: String
) = Box {
    val context = LocalContext.current
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
            append(itemName)
        }
        append('\n')
        append("Copyright © ")
        append("$year ")
        withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)){
            append(author)
        }
        append(". All rights reserved.\n")
        withAnnotation(
            tag = "URL",
            annotation = url
        ){
            withStyle(style = SpanStyle(color = MaterialTheme.colors.primary, textDecoration = TextDecoration.Underline)){
                append(url)
            }
        }
    }
    ClickableText(
        modifier = Modifier.padding(bottom = 8.dp),
        text = annotatedString,
        style = MaterialTheme.typography.body2,
        onClick = {
            val linkUrl = annotatedString
                .getStringAnnotations("URL", it, it)
                .firstOrNull()?.item

            if (linkUrl != null) {
                context.launchUrl(linkUrl)
            }
        }
    )
}

@Composable
fun AppInfoItem() {
    var clickCount = 0
    val context = LocalContext.current

    val appInfo = context.packageManager.getPackageInfo(context.packageName, 0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable {
                if (++clickCount % 10 == 0) {
                    Toast
                        .makeText(context, "stan LOOΠΔ", Toast.LENGTH_SHORT)
                        .show()
                }
            },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape),
                painter = rememberDrawablePainter(
                    drawable = ContextCompat.getDrawable(
                        context,
                        R.mipmap.ic_launcher
                    )
                ),
                contentDescription = "App icon"
            )
            Column(
                modifier = Modifier.padding(start = 16.dp),
            ) {
                Text(
                    text = "Versión",
                    style = MaterialTheme.typography.caption,
                )
                Text(
                    text = appInfo.versionName,
                    style = MaterialTheme.typography.h5,
                )
            }
        }
    }
}

@Composable
fun AboutItem(
    leading: Painter,
    text: String,
    isHighEmphasis : Boolean = false,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = getCurrentTheme().primaryText,
    onClick: (() -> Unit)? = null
) = Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp)
        .clip(MaterialTheme.shapes.medium)
        .clickable(
            interactionSource = MutableInteractionSource(),
            enabled = onClick != null,
            indication = rememberRipple(),
            onClick = {
                onClick?.invoke()
            }
        ),
    backgroundColor = backgroundColor
) {
    Row(
        modifier = Modifier.padding(if (isHighEmphasis) 16.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(if(isHighEmphasis) 32.dp else 24.dp),
            painter = leading,
            contentDescription = "About item icon",
            tint = contentColor
        )
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = text,
            style = if (isHighEmphasis) MaterialTheme.typography.h5 else MaterialTheme.typography.subtitle2,
            color = contentColor
        )
    }
}