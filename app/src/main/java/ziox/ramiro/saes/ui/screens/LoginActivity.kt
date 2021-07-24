package ziox.ramiro.saes.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.data.models.School
import ziox.ramiro.saes.data.models.SelectSchoolContract
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.data.repositories.AuthWebViewRepository
import ziox.ramiro.saes.features.saes.ui.screens.SAESActivity
import ziox.ramiro.saes.ui.components.AsyncButton
import ziox.ramiro.saes.ui.components.CaptchaInput
import ziox.ramiro.saes.ui.components.SchoolButton
import ziox.ramiro.saes.ui.components.TextButton
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.MutableStateWithValidation
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.validate
import ziox.ramiro.saes.view_models.AuthViewModel

class LoginActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        viewModelFactory { AuthViewModel(AuthWebViewRepository(this)) }
    }

    private val schoolUrl = mutableStateOf("")
    private val username = mutableStateOf("")
    private val password = mutableStateOf("")
    private lateinit var userPreferences : UserPreferences

    private val selectSchoolLauncher = registerForActivityResult(SelectSchoolContract()){
        if (it == null) return@registerForActivityResult

        UserPreferences.invoke(this).setPreference(PreferenceKeys.SchoolUrl, it.url)
        authViewModel.fetchCaptcha()
        schoolUrl.value = it.url
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userPreferences = UserPreferences.invoke(this)

        schoolUrl.value = userPreferences.getPreference(PreferenceKeys.SchoolUrl, null) ?: ""
        username.value = userPreferences.getPreference(PreferenceKeys.Boleta, "")
        password.value = userPreferences.getPreference(PreferenceKeys.Password, "")

        setContent {
            if(authViewModel.auth.value?.isLoggedIn == true){
                userPreferences.setPreference(PreferenceKeys.Boleta, username.value)
                userPreferences.setPreference(PreferenceKeys.Password, password.value)

                startActivity(Intent(this@LoginActivity, SAESActivity::class.java))
                finish()
            }else if(authViewModel.isLoggedIn.value == true){
                startActivity(Intent(this@LoginActivity, SAESActivity::class.java))
                finish()
            }

            SAESParaAlumnosTheme {
                Scaffold {
                    if(username.value.isNotBlank() && password.value.isNotBlank() && userPreferences.isAuthDataSaved()){
                        LoginOnlyCaptcha(
                            authViewModel,
                            username,
                            password
                        )
                    }else{
                        Login(
                            authViewModel,
                            selectSchoolLauncher,
                            schoolUrl,
                            username,
                            password
                        )
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
    selectSchoolLauncher: ActivityResultLauncher<Unit>,
    schoolUrl: State<String>,
    username: MutableState<String>,
    password: MutableState<String>
) {
    val focusManager = LocalFocusManager.current

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

    Column(
        modifier = Modifier.padding(
            top = 64.dp,
            start = 32.dp,
            end = 32.dp,
            bottom = 24.dp
        )
    ) {
        Text(
            text = "Iniciar sesión",
            style = MaterialTheme.typography.h4
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
            color = MaterialTheme.colors.error,
            text = usernameValidator.errorState.value ?: "",
            style = MaterialTheme.typography.body2
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
            visualTransformation = if (!passwordVisible.value){
                PasswordVisualTransformation()
            } else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
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
                        imageVector = if (!passwordVisible.value){
                            Icons.Rounded.Visibility
                        }else{
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
            color = MaterialTheme.colors.error,
            text = authViewModel.auth.value?.errorMessage ?: passwordValidator.errorState.value ?: "",
            style = MaterialTheme.typography.body2
        )
        CaptchaInput(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            authViewModel = authViewModel,
            captcha = captcha,
        ){
            if(listOf(usernameValidator, passwordValidator, captcha).validate()){
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
            isLoading = authViewModel.auth.value == null
        ){
            if(listOf(usernameValidator, passwordValidator, captcha).validate()){
                authViewModel.login(
                    username.value,
                    password.value,
                    captcha.mutableState.value
                ).invokeOnCompletion {
                    captcha.mutableState.value = ""
                }
            }
        }
        Box(modifier = Modifier.weight(1f))
        SchoolButton(
            isSmall = true,
            school = School.findSchoolByUrl(schoolUrl.value)
        ){
            selectSchoolLauncher.launch()
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
            style = MaterialTheme.typography.h6
        )
        Text(
            text = userPreferences.getPreference(PreferenceKeys.Boleta, ""),
            style = MaterialTheme.typography.h4
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
            isLoading = authViewModel.auth.value == null
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
            textColor = getCurrentTheme().info
        ) {
            userPreferences.setPreference(PreferenceKeys.OfflineMode, true)
            authViewModel.checkSession()
        }
        TextButton(
            text = "USAR OTRA CUENTA"
        ) {
            username.value = ""
            password.value = ""
            userPreferences.removeAuthData()
        }
    }
}