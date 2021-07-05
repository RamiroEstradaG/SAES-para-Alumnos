package ziox.ramiro.saes.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.request.ImageRequest
import com.google.accompanist.coil.rememberCoilPainter
import kotlinx.coroutines.flow.filter
import ziox.ramiro.saes.utils.ValidationResult
import ziox.ramiro.saes.view_models.AuthState
import ziox.ramiro.saes.view_models.AuthViewModel

@Composable
fun CaptchaInput(
    modifier: Modifier = Modifier,
    captchaWidth: Dp = 120.dp,
    authViewModel: AuthViewModel,
    captcha: MutableState<String>,
    validationResult: ValidationResult
) = Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    when(val state = authViewModel.states.filter {
        it is AuthState.LoadingCaptcha || it is AuthState.CaptchaComplete
    }.collectAsState(initial = null).value){
        is AuthState.LoadingCaptcha -> Box(
            modifier = Modifier.size(captchaWidth, captchaWidth.div(2f)),
        ) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        is AuthState.CaptchaComplete -> Image(
            modifier = Modifier
                .size(captchaWidth, captchaWidth.div(2f))
                .clip(MaterialTheme.shapes.small),
            contentScale = ContentScale.Crop,
            painter = rememberCoilPainter(
                fadeIn = true,
                request = ImageRequest
                    .Builder(LocalContext.current)
                    .data(state.captcha.url)
                    .headers(state.captcha.headers).build(),
            ),
            contentDescription = "Captcha"
        )
        else -> authViewModel.fetchCaptcha()
    }
    OutlinedTextField(
        modifier = Modifier
            .padding(top = 8.dp)
            .width(150.dp),
        value = captcha.component1(),
        label = {
            Text(text = "Captcha")
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        onValueChange = captcha.component2(),
        isError = validationResult.isError,
    )
    Text(
        color = MaterialTheme.colors.error,
        text = validationResult.errorMessage,
        style = MaterialTheme.typography.body2,
        textAlign = TextAlign.Center
    )
}