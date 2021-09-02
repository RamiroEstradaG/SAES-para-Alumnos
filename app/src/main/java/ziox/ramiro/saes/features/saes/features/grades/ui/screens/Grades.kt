package ziox.ramiro.saes.features.saes.features.grades.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.grades.data.repositories.GradesWebViewRepository
import ziox.ramiro.saes.features.saes.features.grades.view_models.GradesViewModel
import ziox.ramiro.saes.features.saes.features.home.ui.components.GradeItem
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.ui.components.ResponsePlaceholder


@Composable
fun Grades(
    gradesViewModel: GradesViewModel = viewModel(
        factory = viewModelFactory { GradesViewModel(GradesWebViewRepository(LocalContext.current)) }
    )
) {
    Column(
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "Calificaciones",
            style = MaterialTheme.typography.h4
        )

        if(gradesViewModel.grades.value != null){
            gradesViewModel.grades.value?.let {
                if (it.isNotEmpty()){
                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 32.dp),
                        contentPadding = PaddingValues(bottom = 64.dp)
                    ) {
                        items(it){ grade ->
                            GradeItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                classGrades = grade
                            )
                        }
                    }
                }else{
                    ResponsePlaceholder(
                        painter = painterResource(id = R.drawable.logging_off),
                        text = "No tienes ninguna materia registrada"
                    )
                }
            }
        }else{
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
    ErrorSnackbar(gradesViewModel.error)
}


