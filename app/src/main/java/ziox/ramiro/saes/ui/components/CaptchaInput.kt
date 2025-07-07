package ziox.ramiro.saes.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.request.ImageRequest
import com.google.accompanist.coil.rememberCoilPainter
import ziox.ramiro.saes.utils.MutableStateWithValidation
import ziox.ramiro.saes.view_models.AuthViewModel

@Composable
fun CaptchaInput(
    modifier: Modifier = Modifier,
    captchaWidth: Dp = 120.dp,
    authViewModel: AuthViewModel,
    captcha: MutableStateWithValidation<String>,
    overrideError: String? = null,
    onDone: () -> Unit = {}
) = Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    val focusManager = LocalFocusManager.current

    if (authViewModel.captcha.value != null){
        authViewModel.captcha.value?.let {
            Image(
                modifier = Modifier
                    .size(captchaWidth, captchaWidth.div(2f))
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
                painter = rememberCoilPainter(
                    fadeIn = true,
                    request = ImageRequest
                        .Builder(LocalContext.current)
                        .data(it.url)
                        .headers(it.headers).build(),
                ),
                contentDescription = "Captcha"
            )
        }
    }else{
        Box(
            modifier = Modifier
                .size(captchaWidth, captchaWidth.div(2f))
                .clip(MaterialTheme.shapes.medium),
        ) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }

    OutlinedTextField(
        modifier = Modifier
            .padding(top = 8.dp)
            .width(150.dp),
        value = captcha.mutableState.component1(),
        label = {
            Text(text = "Captcha")
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Send
        ),
        onValueChange = {
            captcha.mutableState.value = it.uppercase()
        },
        isError = !captcha.errorState.value.isNullOrBlank(),
        keyboardActions = KeyboardActions(
            onSend = {
                onDone()
                focusManager.clearFocus(true)
            }
        )
    )
    Text(
        color = MaterialTheme.colorScheme.error,
        text = overrideError ?: captcha.errorState.value ?: "",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
}