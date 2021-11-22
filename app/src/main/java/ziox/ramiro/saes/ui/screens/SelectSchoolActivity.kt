package ziox.ramiro.saes.ui.screens

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import ziox.ramiro.saes.data.models.School
import ziox.ramiro.saes.data.models.SelectSchoolContract
import ziox.ramiro.saes.data.models.highSchools
import ziox.ramiro.saes.data.models.universities
import ziox.ramiro.saes.ui.components.SchoolButton
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme
import ziox.ramiro.saes.ui.theme.getCurrentTheme

@AndroidEntryPoint
class SelectSchoolActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SAESParaAlumnosTheme {
                Scaffold {
                    SchoolSelector()
                }
            }
        }
    }
}

enum class CurrentSchoolSelection(val title: String, val list: List<School>) {
    HIGH_SCHOOL("Medio Superior", highSchools),
    UNIVERSITY("Superior", universities)
}


@Composable
fun SchoolSelector(
    modifier: Modifier = Modifier
) = Column(
    modifier = modifier.padding(
        start = 32.dp,
        end = 32.dp,
        top = 32.dp
    )
) {
    val context = LocalContext.current

    val currentSelection = remember {
        mutableStateOf(CurrentSchoolSelection.UNIVERSITY)
    }
    Row (
        verticalAlignment = Alignment.Top
    ) {
        Text(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .weight(1f),
            text = currentSelection.value.title,
            style = MaterialTheme.typography.h4
        )
        IconButton(
            onClick = {
                if(currentSelection.value == CurrentSchoolSelection.HIGH_SCHOOL){
                    currentSelection.value = CurrentSchoolSelection.UNIVERSITY
                }else if (currentSelection.value == CurrentSchoolSelection.UNIVERSITY){
                    currentSelection.value = CurrentSchoolSelection.HIGH_SCHOOL
                }
            }
        ){
            Icon(
                modifier = Modifier.size(40.dp),
                imageVector = Icons.Rounded.KeyboardArrowRight,
                tint = getCurrentTheme().primaryText,
                contentDescription = "Right"
            )
        }
    }
    Box(
        Modifier.verticalScroll(rememberScrollState())
    ){
        Column(
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            currentSelection.value.list.forEach { school ->
                SchoolButton(
                    modifier = Modifier.padding(top = 8.dp),
                    school = school
                ){
                    if(context is Activity){
                        context.setResult(RESULT_OK, context.intent.apply {
                            putExtra(SelectSchoolContract.RESULT, school)
                        })
                        context.finish()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SelectSchoolPreview() = SAESParaAlumnosTheme {
    Scaffold {
        SchoolSelector()
    }
}