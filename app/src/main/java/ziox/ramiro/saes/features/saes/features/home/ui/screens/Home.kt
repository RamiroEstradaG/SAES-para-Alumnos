package ziox.ramiro.saes.features.saes.features.home.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Feed
import androidx.compose.material.icons.rounded.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.filter
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.features.grades.data.models.ClassGrades
import ziox.ramiro.saes.features.saes.features.grades.data.repositories.GradesWebViewRepository
import ziox.ramiro.saes.features.saes.features.grades.view_models.GradesState
import ziox.ramiro.saes.features.saes.features.grades.view_models.GradesViewModel
import ziox.ramiro.saes.features.saes.features.home.data.repositories.TwitterRetrofitRepository
import ziox.ramiro.saes.features.saes.features.home.ui.components.TwitterItem
import ziox.ramiro.saes.features.saes.features.home.view_models.HomeState
import ziox.ramiro.saes.features.saes.features.home.view_models.HomeViewModel
import ziox.ramiro.saes.features.saes.features.home.ui.components.SmallGradeItem
import ziox.ramiro.saes.features.saes.ui.components.RecentActivityItem
import ziox.ramiro.saes.features.saes.view_models.MenuSection
import ziox.ramiro.saes.features.saes.view_models.SAESViewModel

@ExperimentalMaterialApi
@Composable
fun Home(
    homeViewModel: HomeViewModel = viewModel(
        factory = viewModelFactory { HomeViewModel(TwitterRetrofitRepository()) }
    ),
    gradesViewModel: GradesViewModel = viewModel(
        factory = viewModelFactory { GradesViewModel(GradesWebViewRepository(LocalContext.current)) }
    ),
    saesViewModel: SAESViewModel = viewModel()
) = Column(
    modifier = Modifier
        .verticalScroll(rememberScrollState())
        .padding(top = 32.dp, bottom = 64.dp)
) {
    HomeItem(
        modifier = Modifier.padding(start = 32.dp, end = 32.dp),
        title = "Actividad reciente",
        icon = Icons.Rounded.History
    ){
        Row(
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            RecentActivityItem(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            )
            RecentActivityItem(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            )
            RecentActivityItem(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            )
        }
    }
    when(val state = gradesViewModel.statesAsState().value){
        is GradesState.GradesComplete -> if(state.grades.isNotEmpty()){
            HomeItem(
                modifier = Modifier.padding(top = 32.dp, start = 32.dp, end = 32.dp),
                title = "Calificaciones",
                icon = Icons.Rounded.FactCheck
            ){
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 28.dp)
                ) {
                    items(state.grades.size){ i ->
                        SmallGradeItem(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            classGrades = state.grades[i]
                        ){
                            saesViewModel.changeSection(MenuSection.GRADES)
                        }
                    }
                }
            }
        }
        null -> gradesViewModel.fetchGrades()
    }
    HomeItem(
        modifier = Modifier.padding(top = 32.dp, start = 32.dp, end = 32.dp),
        title = "Noticias",
        icon = Icons.Rounded.Feed
    ){
        when(val state = homeViewModel.states.filter { it is HomeState.TweetsLoading || it is HomeState.TweetsComplete }.collectAsState(
            initial = null
        ).value){
            is HomeState.TweetsLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is HomeState.TweetsComplete -> Column {
                state.tweets.forEach {
                    TwitterItem(
                        modifier = Modifier.padding(
                            horizontal = 32.dp,
                            vertical = 6.dp
                        ),
                        tweet = it
                    )
                }
            }
            else -> homeViewModel.fetchTweets()
        }
    }
}

@Preview
@Composable
fun HomeItem(
    modifier: Modifier = Modifier,
    title: String = "Title Long",
    icon: ImageVector = Icons.Rounded.History,
    content: @Composable () -> Unit = {}
) = Column {
    Row(
        modifier = modifier.padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(end = 8.dp),
            imageVector = icon,
            contentDescription = "Section icon"
        )
        Text(
            text = title,
            style = MaterialTheme.typography.h5
        )
    }
    content()
}