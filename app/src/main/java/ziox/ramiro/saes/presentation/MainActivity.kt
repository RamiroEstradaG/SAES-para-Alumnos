package ziox.ramiro.saes.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.request.ImageRequest
import com.google.accompanist.coil.rememberCoilPainter
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ChevronRight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import ziox.ramiro.saes.data.AuthWebViewRepository
import ziox.ramiro.saes.data.models.School
import ziox.ramiro.saes.data.models.SelectSchoolContract
import ziox.ramiro.saes.data.models.universities
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.ui.components.AsyncButton
import ziox.ramiro.saes.ui.components.CaptchaInput
import ziox.ramiro.saes.ui.components.SchoolButton
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.ui.theme.getCurrentTheme
import ziox.ramiro.saes.utils.*
import ziox.ramiro.saes.view_models.AuthState
import ziox.ramiro.saes.view_models.AuthViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        viewModelFactory { AuthViewModel(AuthWebViewRepository(this)) }
    }

    private val schoolUrl = MutableStateFlow("")

    private val selectSchoolLauncher = registerForActivityResult(SelectSchoolContract()){
        if (it == null) return@registerForActivityResult

        setPreference(SharedPreferenceKeys.SCHOOL_URL, it.url)
        authViewModel.fetchCaptcha()
        schoolUrl.value = it.url
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        schoolUrl.value = getPreference(SharedPreferenceKeys.SCHOOL_URL, "")

        setContent {
            SAESParaAlumnosTheme {
                Scaffold {
                    Login(
                        authViewModel,
                        selectSchoolLauncher,
                        schoolUrl.collectAsState()
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (getPreference(SharedPreferenceKeys.SCHOOL_URL, "").isBlank()){
            selectSchoolLauncher.launch()
        }
    }
}


@Composable
fun Login(
    authViewModel: AuthViewModel,
    selectSchoolLauncher: ActivityResultLauncher<Unit>,
    schoolUrl: State<String>
) {
    val username = remember {
        mutableStateOf("")
    }
    val password = remember {
        mutableStateOf("")
    }
    val captcha = remember {
        mutableStateOf("")
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

    val loginErrorState = remember {
        authViewModel.states
            .filterIsInstance<AuthState.LoginComplete>().map {
                print(it.auth)
                val message = it.auth.message
                if (message.isNotBlank()) message
                else null
            }
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
            isError = usernameValidator.isError,
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
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Password
            ),
            isError = passwordValidator.isError
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            color = MaterialTheme.colors.error,
            text = loginErrorState.collectAsState(initial = null).value ?: passwordValidator.errorMessage,
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
            isLoadingState = authViewModel.states.filter {
                it is AuthState.LoadingLogin || it is AuthState.LoadingCaptcha
            }.map {
                when (it) {
                    is AuthState.LoadingLogin -> true
                    is AuthState.LoginComplete -> false
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
