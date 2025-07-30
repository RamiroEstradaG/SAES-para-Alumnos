package ziox.ramiro.saes.ui.screens

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import ziox.ramiro.saes.data.models.School
import ziox.ramiro.saes.data.models.SelectSchoolContract
import ziox.ramiro.saes.data.models.highSchools
import ziox.ramiro.saes.data.models.universities
import ziox.ramiro.saes.ui.components.SchoolButton
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme

@AndroidEntryPoint
class SelectSchoolActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SAESParaAlumnosTheme {
                SchoolSelector()
            }
        }
    }
}

enum class CurrentSchoolSelection(val title: String, val list: List<School>) {
    HIGH_SCHOOL("Medio Superior", highSchools),
    UNIVERSITY("Superior", universities)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolSelector(
    modifier: Modifier = Modifier
) = Scaffold { paddingValues ->
    val context = LocalContext.current

    val currentSelection = remember {
        mutableStateOf(CurrentSchoolSelection.UNIVERSITY)
    }

    Column(
        modifier = modifier
            .padding(paddingValues)
            .padding(
                start = 32.dp,
                end = 32.dp,
                top = 48.dp
            )
    ) {
        Box(
            Modifier.verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                currentSelection.value.list.forEach { school ->
                    SchoolButton(
                        modifier = Modifier.padding(top = 8.dp),
                        school = school
                    ) {
                        if (context is Activity) {
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

    PrimaryTabRow(
        modifier = Modifier.padding(paddingValues),
        selectedTabIndex = CurrentSchoolSelection.entries.reversed().toTypedArray().indexOf(currentSelection.value)
    ) {
        CurrentSchoolSelection.entries.reversed().forEachIndexed { index, tab ->
            Tab(
                selected = currentSelection.value == tab,
                onClick = {
                    currentSelection.value = tab
                },
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 16.dp),
                    text = tab.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview
@Composable
fun SelectSchoolPreview() = SAESParaAlumnosTheme {
    Scaffold {
        SchoolSelector(
            modifier = Modifier.padding(it)
        )
    }
}