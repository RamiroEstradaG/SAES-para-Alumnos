package ziox.ramiro.saes.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.data.models.ValidatorState
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.ui.theme.getCurrentTheme

@Composable
inline fun <reified T>SAESTextField(
    modifier: Modifier = Modifier,
    state: ValidatorState<T>,
    label: String = "",
    hint: String = "",
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    noinline leading: @Composable (() -> Unit)? = null,
    noinline trailing: @Composable (() -> Unit)? = null,
    noinline onClick: (() -> Unit)? = null
) = Column(
    modifier = modifier,
) {
    val error = state.error.collectAsState().value

    Box {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.stringState().value,
            onValueChange = {
                if(it is T){
                    state.value = it
                }
            },
            isError = error != null,
            label = {
                Text(text = label)
            },
            readOnly = readOnly,
            leadingIcon = leading,
            keyboardOptions = keyboardOptions
        )
        if(onClick != null){
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(MaterialTheme.shapes.small)
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable {
                        onClick.invoke()
                    }
            )
        }
        if (trailing != null){
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .height(56.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                trailing()
            }
        }
    }
    Text(
        modifier = Modifier.padding(start = 8.dp),
        text = error ?: hint,
        style = MaterialTheme.typography.caption,
        color = if (error != null) MaterialTheme.colors.error else getCurrentTheme().secondaryText
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GMARTextFieldPreview(){
    SAESParaAlumnosTheme {
        SAESTextField(
            state = ValidatorState(""),
            trailing = {
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Rounded.Clear, contentDescription = null)
                }
            }
        )
    }
}