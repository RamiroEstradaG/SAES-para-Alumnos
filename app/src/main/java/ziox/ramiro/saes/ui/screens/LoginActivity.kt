package ziox.ramiro.saes.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import ziox.ramiro.saes.data.models.School
import ziox.ramiro.saes.data.models.SelectSchoolContract
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.data.repositories.AuthWebViewRepository
import ziox.ramiro.saes.features.saes.data.repositories.StorageFirebaseRepository
import ziox.ramiro.saes.features.saes.ui.screens.SAESActivity
import ziox.ramiro.saes.ui.components.AsyncButton
import ziox.ramiro.saes.ui.components.CaptchaInput
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.components.SchoolButton
import ziox.ramiro.saes.ui.components.TextButton
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.utils.MutableStateWithValidation
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.launchUrl
import ziox.ramiro.saes.utils.validate
import ziox.ramiro.saes.view_models.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        viewModelFactory { AuthViewModel(AuthWebViewRepository(this), StorageFirebaseRepository(), true) }
    }

    var isAuthDataSaved: Boolean = false
    private val schoolUrl = MutableStateFlow("")

    @Inject lateinit var userPreferences : UserPreferences

    private val selectSchoolLauncher = registerForActivityResult(SelectSchoolContract()){
        if (it == null) return@registerForActivityResult

        UserPreferences.invoke(this).setPreference(PreferenceKeys.SchoolUrl, it.url)
        schoolUrl.value = it.url
        authViewModel.fetchCaptcha()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isAuthDataSaved = userPreferences.authData.value.isAuthDataSaved()
        schoolUrl.value = userPreferences.getPreference(PreferenceKeys.SchoolUrl, null) ?: ""

        setContent {
            val username = remember {
                mutableStateOf(userPreferences.getPreference(PreferenceKeys.Boleta, ""))
            }
            val password = remember {
                mutableStateOf(userPreferences.getPreference(PreferenceKeys.Password, ""))
            }

            if(authViewModel.auth.value?.isLoggedIn == true){
                userPreferences.setAuthData(username.value, password.value)

                startActivity(Intent(this@LoginActivity, SAESActivity::class.java))
                finish()
            }else if(authViewModel.isLoggedIn.value == true){
                startActivity(Intent(this@LoginActivity, SAESActivity::class.java))
                finish()
            }

            SAESParaAlumnosTheme {
                Scaffold { paddingValues ->
                    Crossfade(
                        modifier = Modifier.padding(paddingValues),
                        targetState = username.value.isNotBlank() && password.value.isNotBlank() && isAuthDataSaved) {
                        if(it){
                            LoginOnlyCaptcha(
                                authViewModel,
                                username,
                                password
                            )
                        }else{
                            Login(
                                authViewModel,
                                selectSchoolLauncher,
                                schoolUrl.collectAsState(),
                                username,
                                password
                            )
                        }
                    }

                    ErrorSnackbar(authViewModel.error)
                    ErrorSnackbar(authViewModel.scrapError.map { it?.let { "Error en la página de inicio de sesión" } }) {
                        authViewModel.uploadSourceCode(false)
                    }
                    ErrorSnackbar(authViewModel.captchaScrapError.map { it?.let { "Error al obtener el captcha" } }) {
                        authViewModel.uploadSourceCode(true)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (userPreferences.getPreference(PreferenceKeys.SchoolUrl, null) == null){
            selectSchoolLauncher.launch(Unit)
        }
    }
}


@Composable
fun Login(
    authViewModel: AuthViewModel,
    selectSchoolLauncher: ActivityResultLauncher<Unit?>,
    schoolUrl: State<String>,
    username: MutableState<String>,
    password: MutableState<String>
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val userPreferences = UserPreferences.invoke(context)

    val passwordVisible = remember {
        mutableStateOf(false)
    }

    val usernameValidator = MutableStateWithValidation(username, remember {
        mutableStateOf(null)
    }){
        if(it.isEmpty()) "El campo está vacío."
        else null
    }

    val passwordValidator = MutableStateWithValidation(password, remember {
        mutableStateOf(null)
    }){
        if(it.isEmpty()) "El campo está vacío."
        else null
    }

    val captcha = MutableStateWithValidation(remember {
        mutableStateOf("")
    }, remember {
        mutableStateOf(null)
    }){
        if(it.isEmpty()) "El campo está vacío."
        else null
    }

    val isFirebaseServicesEnabled = remember {
        mutableStateOf(userPreferences.getPreference(PreferenceKeys.IsFirebaseEnabled, false))
    }

    val showFirebaseDialog = remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(
                    top = 64.dp,
                    start = 32.dp,
                    end = 32.dp,
                    bottom = 16.dp
                )
            ) {
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.headlineLarge
                )
                OutlinedTextField(
                    modifier = Modifier
                        .padding(top = 32.dp)
                        .fillMaxWidth(),
                    value = username.component1(),
                    label = {
                        Text(text = "Boleta")
                    },
                    singleLine = true,
                    onValueChange = username.component2(),
                    isError = !usernameValidator.errorState.value.isNullOrBlank(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    )
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    color = MaterialTheme.colorScheme.error,
                    text = usernameValidator.errorState.value ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth(),
                    value = password.component1(),
                    label = {
                        Text(text = "Contraseña")
                    },
                    singleLine = true,
                    onValueChange = password.component2(),
                    visualTransformation = if (!passwordVisible.value) {
                        PasswordVisualTransformation()
                    } else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                passwordVisible.value = !passwordVisible.value
                            }
                        ) {
                            Icon(
                                imageVector = if (!passwordVisible.value) {
                                    Icons.Rounded.Visibility
                                } else {
                                    Icons.Rounded.VisibilityOff
                                },
                                contentDescription = "Visibility"
                            )
                        }
                    },
                    isError = !passwordValidator.errorState.value.isNullOrBlank(),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    )
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    color = MaterialTheme.colorScheme.error,
                    text = authViewModel.auth.value?.errorMessage
                        ?: passwordValidator.errorState.value ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFirebaseServicesEnabled.component1(),
                        onCheckedChange = { isChecked ->
                            if(isChecked){
                                showFirebaseDialog.value = true
                            }else{
                                isFirebaseServicesEnabled.component2().invoke(false)
                                userPreferences.setPreference(PreferenceKeys.IsFirebaseEnabled, false)
                            }
                        }
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = "Habilitar los servicios personalizados"
                    )
                }
                CaptchaInput(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    authViewModel = authViewModel,
                    captcha = captcha,
                ) {
                    if (listOf(usernameValidator, passwordValidator, captcha).validate()) {
                        authViewModel.login(
                            username.value,
                            password.value,
                            captcha.mutableState.value
                        ).invokeOnCompletion {
                            captcha.mutableState.value = ""
                        }
                    }
                }
                AsyncButton(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth(),
                    isHighEmphasis = true,
                    text = "Iniciar sesión",
                    isLoading = authViewModel.auth.value == null || authViewModel.auth.value?.isLoggedIn == true
                ) {
                    if (listOf(usernameValidator, passwordValidator, captcha).validate()) {
                        authViewModel.login(
                            username.value,
                            password.value,
                            captcha.mutableState.value
                        ).invokeOnCompletion {
                            captcha.mutableState.value = ""
                        }
                    }
                }
            }
        }
        SchoolButton(
            modifier = Modifier.padding(
                start = 32.dp,
                end = 32.dp,
                bottom = 24.dp
            ),
            isSmall = true,
            school = School.findSchoolByUrl(schoolUrl.value)
        ) {
            selectSchoolLauncher.launch(Unit)
        }


        if (showFirebaseDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    showFirebaseDialog.value = false
                },
                title = @Composable {
                    Text(
                        text = "Servicios personalizados",
                    )
                },
                text = @Composable {
                    Column(
                        modifier = Modifier
                            .heightIn(0.dp, 300.dp)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = """
                                    Son características de la aplicación que utilizan servicios basados en la nube para funcionar.
                                    Al activar esta opción estás de acuerdo en enviar la siguiente información a servidores que no son propiedad del Instituto Politécnico Nacional (IPN).
                                    • Número de boleta.
                                    • Unidad académica.
                                    • Nombre de la carrera.
                                    • Promedio global.
                                    • Kárdex.
                                    
                                    Al no activar esta opción se desactivarán las siguientes características:
                                    • Comparación de tu promedio con el de tu carrera, unidad académica y el IPN en la sección "Rendimiento escolar".
                                    • Agenda personal
                                    
                                    Puedes ver y eliminar tus datos almacenados en la nube en cualquier momento en el apartado "Configuración" al iniciar sesión.
                                    Al activar esta característica aceptas nuestra política de privacidad.
                                """.trimIndent()
                        )
                        TextButton(
                            text = "Ver política de privacidad"
                        ){
                            context.launchUrl("https://ramiroestradag.github.io/SAES-para-Alumnos/privacy_policy")
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        text = "Aceptar",
                    ) {
                        showFirebaseDialog.value = false
                        isFirebaseServicesEnabled.value = true
                        userPreferences.setPreference(PreferenceKeys.IsFirebaseEnabled, true)
                    }
                },
                dismissButton = {
                    TextButton(
                        text = "Cancelar",
                        textColor = MaterialTheme.colorScheme.error
                    ) {
                        showFirebaseDialog.value = false
                    }
                }
            )
        }
    }
}


@Composable
fun LoginOnlyCaptcha(
    authViewModel: AuthViewModel,
    username: MutableState<String>,
    password: MutableState<String>
) {
    val context = LocalContext.current
    val userPreferences = UserPreferences.invoke(context)

    val captcha = MutableStateWithValidation(remember {
        mutableStateOf("")
    }, remember {
        mutableStateOf(null)
    }){
        if(it.isEmpty()) "El campo está vacío."
        else null
    }

    Column(
        modifier = Modifier.padding(
            top = 64.dp,
            start = 32.dp,
            end = 32.dp,
            bottom = 24.dp
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Iniciar sesión como",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = userPreferences.getPreference(PreferenceKeys.Boleta, ""),
            style = MaterialTheme.typography.headlineLarge
        )
        Box(
            modifier = Modifier.weight(1f)
        )
        CaptchaInput(
            modifier = Modifier
                .padding(top = 16.dp, bottom = 64.dp)
                .fillMaxWidth(),
            captchaWidth = 170.dp,
            authViewModel = authViewModel,
            captcha = captcha,
            overrideError = authViewModel.auth.value?.errorMessage
        ){
            if(captcha.validate()){
                authViewModel.login(
                    username.value,
                    password.value,
                    captcha.mutableState.value
                ).invokeOnCompletion {
                    captcha.mutableState.value = ""
                }
            }
        }
        AsyncButton(
            modifier = Modifier
                .padding(top = 24.dp, bottom = 16.dp)
                .fillMaxWidth(),
            isHighEmphasis = true,
            text = "Iniciar sesión",
            isLoading = authViewModel.auth.value == null || authViewModel.auth.value?.isLoggedIn == true
        ){
            if(captcha.validate()){
                authViewModel.login(
                    username.value,
                    password.value,
                    captcha.mutableState.value
                ).invokeOnCompletion {
                    captcha.mutableState.value = ""
                }
            }
        }
        TextButton(
            text = "MODO OFFLINE",
            textColor = MaterialTheme.colorScheme.secondary
        ) {
            userPreferences.setPreference(PreferenceKeys.OfflineMode, true)
            authViewModel.checkSession()
        }
        TextButton(
            text = "USAR OTRA CUENTA"
        ) {
            username.value = ""
            password.value = ""
            if(context is LoginActivity){
                context.isAuthDataSaved = false
            }
            userPreferences.removeAuthData()
        }
    }
}