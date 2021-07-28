package ziox.ramiro.saes.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.flow.MutableStateFlow
import ziox.ramiro.saes.data.models.School
import ziox.ramiro.saes.data.models.SelectSchoolContract
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.data.repositories.AuthWebViewRepository
import ziox.ramiro.saes.features.saes.ui.screens.SAESActivity
import ziox.ramiro.saes.ui.components.*
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.MutableStateWithValidation
import ziox.ramiro.saes.utils.PreferenceKeys
import ziox.ramiro.saes.utils.UserPreferences
import ziox.ramiro.saes.utils.validate
import ziox.ramiro.saes.view_models.AuthViewModel

class LoginActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        viewModelFactory { AuthViewModel(AuthWebViewRepository(this), true) }
    }

    var isAuthDataSaved: Boolean = false
    private val schoolUrl = MutableStateFlow("")
    private lateinit var userPreferences : UserPreferences

    private val selectSchoolLauncher = registerForActivityResult(SelectSchoolContract()){
        if (it == null) return@registerForActivityResult

        UserPreferences.invoke(this).setPreference(PreferenceKeys.SchoolUrl, it.url)
        schoolUrl.value = it.url
        authViewModel.fetchCaptcha()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userPreferences = UserPreferences.invoke(this)
        isAuthDataSaved = userPreferences.authData.value.isAuthDataSaved()

        setContent {
            val username = remember {
                mutableStateOf(userPreferences.getPreference(PreferenceKeys.Boleta, ""))
            }
            val password = remember {
                mutableStateOf(userPreferences.getPreference(PreferenceKeys.Password, ""))
            }

            schoolUrl.value = userPreferences.getPreference(PreferenceKeys.SchoolUrl, null) ?: ""

            if(authViewModel.auth.value?.isLoggedIn == true){
                userPreferences.setAuthData(username.value, password.value)

                startActivity(Intent(this@LoginActivity, SAESActivity::class.java))
                finish()
            }else if(authViewModel.isLoggedIn.value == true){
                startActivity(Intent(this@LoginActivity, SAESActivity::class.java))
                finish()
            }

            SAESParaAlumnosTheme {
                Scaffold {
                    if(username.value.isNotBlank() && password.value.isNotBlank() && isAuthDataSaved){
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
                    ErrorSnackbar(authViewModel.error)
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
                    isLoading = authViewModel.auth.value == null || authViewModel.auth.value?.isLoggedIn == true
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
            if(context is LoginActivity){
                context.isAuthDataSaved = false
            }
            userPreferences.removeAuthData()
        }
    }
}