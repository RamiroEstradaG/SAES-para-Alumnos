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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
import ziox.ramiro.saes.utils.*
import ziox.ramiro.saes.view_models.AuthEvent
import ziox.ramiro.saes.view_models.AuthState
import ziox.ramiro.saes.view_models.AuthViewModel

class LoginActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        viewModelFactory { AuthViewModel(AuthWebViewRepository(this)) }
    }

    private val schoolUrl = MutableStateFlow("")
    private val username = mutableStateOf("")
    private val password = mutableStateOf("")

    private val selectSchoolLauncher = registerForActivityResult(SelectSchoolContract()){
        if (it == null) return@registerForActivityResult

        setPreference(SharedPreferenceKeys.SCHOOL_URL, it.url)
        authViewModel.fetchCaptcha()
        schoolUrl.value = it.url
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        schoolUrl.value = getPreference(SharedPreferenceKeys.SCHOOL_URL, "")
        username.value = getPreference(SharedPreferenceKeys.BOLETA, "")
        password.value = getPreference(SharedPreferenceKeys.PASSWORD, "")

        listenToAuthStates()
        listenToAuthEvents()

        setContent {
            SAESParaAlumnosTheme {
                Scaffold {
                    if(username.value.isNotBlank() && password.value.isNotBlank() && isAuthDataSaved()){
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
            }
        }
    }

    private fun listenToAuthStates() = lifecycleScope.launch {
        authViewModel.states.collect {
            when(it){
                is AuthState.CaptchaComplete -> if(it.captcha.isLoggedIn){
                    startActivity(Intent(this@LoginActivity, SAESActivity::class.java))
                    finish()
                }
                is AuthState.SessionCheckComplete -> if(it.isLoggedIn){
                    startActivity(Intent(this@LoginActivity, SAESActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun listenToAuthEvents() = lifecycleScope.launch {
        authViewModel.events.collect {
            when(it){
                is AuthEvent.Error -> {
                    println(it.message)
                }
                is AuthEvent.LoginComplete -> if(!it.auth.isNotLoggedIn){
                    setPreference(SharedPreferenceKeys.BOLETA, username.value)
                    setPreference(SharedPreferenceKeys.PASSWORD, password.value)

                    startActivity(Intent(this@LoginActivity, SAESActivity::class.java))
                    finish()
                }
                else -> {}
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (getPreference(SharedPreferenceKeys.SCHOOL_URL, "").isBlank()){
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
    val captcha = remember {
        mutableStateOf("")
    }

    val passwordVisible = remember {
        mutableStateOf(false)
    }

    val usernameValidator = validateField(username){
        if(it.isEmpty()) "El campo está vacío."
        else null
    }

    val passwordValidator = validateField(password){
        if(it.isEmpty()) "El campo está vacío."
        else null
    }

    val captchaValidator = validateField(captcha){
        if(it.isEmpty()) "El campo está vacío."
        else null
    }

    val loginErrorState = authViewModel.events
        .filterIsInstance<AuthEvent.LoginComplete>().map {
            val message = it.auth.message
            if (message.isNotBlank()) message
            else null
        }.collectAsState(initial = null)

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
            isError = usernameValidator.isError,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Password
            ),
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            color = MaterialTheme.colors.error,
            text = usernameValidator.errorMessage,
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
                keyboardType = KeyboardType.Password
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
            isError = passwordValidator.isError
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            color = MaterialTheme.colors.error,
            text = loginErrorState.value ?: passwordValidator.errorMessage,
            style = MaterialTheme.typography.body2
        )
        CaptchaInput(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            authViewModel = authViewModel,
            captcha = captcha,
            validationResult = captchaValidator
        )
        AsyncButton(
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth(),
            isHighEmphasis = true,
            text = "Iniciar sesión",
            isLoadingState = authViewModel.events.filter {
                it is AuthEvent.LoadingLogin || it is AuthEvent.LoginComplete
            }.map {
                when (it) {
                    is AuthEvent.LoadingLogin -> true
                    is AuthEvent.LoginComplete -> false
                    else -> null
                }
            }.collectAsState(initial = false)
        ){
            if(listOf(usernameValidator, passwordValidator, captchaValidator).areAllValid()){
                authViewModel.login(username.value, password.value, captcha.value)
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

    val captcha = remember {
        mutableStateOf("")
    }

    val captchaValidator = validateField(captcha){
        if(it.isEmpty()) "El campo está vacío."
        else null
    }

    val loginErrorState = authViewModel.events
        .filterIsInstance<AuthEvent.LoginComplete>().map {
            val message = it.auth.message
            if (message.isNotBlank()) message
            else null
        }.collectAsState(initial = null)

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
            text = context.getPreference(SharedPreferenceKeys.BOLETA, ""),
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
            validationResult = captchaValidator,
            overrideError = loginErrorState
        )
        AsyncButton(
            modifier = Modifier
                .padding(top = 24.dp, bottom = 16.dp)
                .fillMaxWidth(),
            isHighEmphasis = true,
            text = "Iniciar sesión",
            isLoadingState = authViewModel.events.filter {
                it is AuthEvent.LoadingLogin || it is AuthEvent.LoginComplete
            }.map {
                when (it) {
                    is AuthEvent.LoadingLogin -> true
                    is AuthEvent.LoginComplete -> false
                    else -> null
                }
            }.collectAsState(initial = false)
        ){
            if(!captchaValidator.isError){
                authViewModel.login(
                    username.value,
                    password.value,
                    captcha.value
                )
            }
        }
        TextButton(
            text = "MODO OFFLINE",
            textColor = getCurrentTheme().info
        ) {
            context.setPreference(SharedPreferenceKeys.OFFLINE_MODE, true)
            authViewModel.checkSession()
        }
        TextButton(
            text = "USAR OTRA CUENTA"
        ) {
            username.value = ""
            password.value = ""
            context.removeAuthData()
        }
    }
}