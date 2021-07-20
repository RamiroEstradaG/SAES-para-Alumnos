package ziox.ramiro.saes.features.saes.features.schedule_generator.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.R
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.schedule.data.models.GeneratorClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule_generator.view_models.ScheduleGeneratorViewModel
import ziox.ramiro.saes.ui.components.ResponsePlaceholder
import ziox.ramiro.saes.ui.theme.SAESParaAlumnosTheme

class ScheduleGeneratorActivity: ComponentActivity() {
    private val scheduleGeneratorViewModel: ScheduleGeneratorViewModel by viewModels {
        viewModelFactory { ScheduleGeneratorViewModel(LocalAppDatabase.invoke(this).scheduleGeneratorRepository()) }
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SAESParaAlumnosTheme {
                Scaffold(
                    floatingActionButton = {
                        Column {
                            FloatingActionButton(
                                modifier = Modifier.padding(bottom = 8.dp),
                                onClick = {

                                }
                            ) {
                                Icon(imageVector = Icons.Rounded.Preview, contentDescription = "Schedule generator icon")
                            }
                            FloatingActionButton(
                                onClick = {

                                }
                            ) {
                                Icon(imageVector = Icons.Rounded.Add, contentDescription = "Filter icon")
                            }
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(top = 16.dp)
                    ){
                        Text(
                            modifier = Modifier.padding(horizontal = 32.dp),
                            text = "Generador de horario",
                            style = MaterialTheme.typography.h4
                        )

                        if(scheduleGeneratorViewModel.scheduleItems.value != null){
                            scheduleGeneratorViewModel.scheduleItems.value?.let {
                                if(it.isNotEmpty()){
                                    LazyColumn(
                                        contentPadding = PaddingValues(
                                            bottom = 64.dp
                                        )
                                    ) {
                                        items(it){ item ->
                                            ScheduleGeneratorItem(item, scheduleGeneratorViewModel)
                                        }
                                    }
                                }else{
                                    ResponsePlaceholder(
                                        painter = painterResource(id = R.drawable.logging_off),
                                        text = "No has agregado clases al generador"
                                    )
                                }
                            }
                        }else{
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScheduleGeneratorItem(
    generatorClassSchedule: GeneratorClassSchedule,
    scheduleGeneratorViewModel: ScheduleGeneratorViewModel
) = Card {
    ListItem(
        modifier = Modifier.clickable {
              //scheduleGeneratorViewModel.removeItem(generatorClassSchedule.className)
        },
        text = {
            Text(text = generatorClassSchedule.className)
        }
    )
}