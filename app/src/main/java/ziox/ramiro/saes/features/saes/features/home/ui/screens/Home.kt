package ziox.ramiro.saes.features.saes.features.home.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Feed
import androidx.compose.material.icons.rounded.History
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.features.ets.data.repositories.ETSWebViewRepository
import ziox.ramiro.saes.features.saes.features.ets.view_models.ETSViewModel
import ziox.ramiro.saes.features.saes.features.grades.data.repositories.GradesWebViewRepository
import ziox.ramiro.saes.features.saes.features.grades.view_models.GradesViewModel
import ziox.ramiro.saes.features.saes.features.home.data.repositories.TwitterAPIRepository
import ziox.ramiro.saes.features.saes.features.home.ui.components.RecentActivityItem
import ziox.ramiro.saes.features.saes.features.home.ui.components.SmallGradeItem
import ziox.ramiro.saes.features.saes.features.home.ui.components.TwitterItem
import ziox.ramiro.saes.features.saes.features.home.view_models.HomeViewModel
import ziox.ramiro.saes.features.saes.view_models.MenuSection
import ziox.ramiro.saes.features.saes.view_models.SAESViewModel
import ziox.ramiro.saes.utils.isNetworkAvailable

@ExperimentalMaterialApi
@Composable
fun Home(
    homeViewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            HomeViewModel(
                TwitterAPIRepository(),
                LocalAppDatabase.invoke(LocalContext.current).historyRepository()
            )
        }
    ),
    gradesViewModel: GradesViewModel = viewModel(
        factory = viewModelFactory { GradesViewModel(GradesWebViewRepository(LocalContext.current)) }
    ),
    saesViewModel: SAESViewModel = viewModel(),
    etsViewModel: ETSViewModel = viewModel(
        factory = viewModelFactory { ETSViewModel(ETSWebViewRepository(LocalContext.current)) }
    ),
) = Column(
    modifier = Modifier
        .verticalScroll(rememberScrollState())
        .padding(top = 32.dp, bottom = 64.dp)
) {
    homeViewModel.historyItems.value?.let {
        HomeItem(
            modifier = Modifier.padding(horizontal = 32.dp),
            title = "Actividad reciente",
            icon = Icons.Rounded.History
        ){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                RecentActivityItem(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    historyItem = it.getOrNull(0)
                ){
                    saesViewModel.changeSection(it.getOrNull(0)!!.section)
                }
                RecentActivityItem(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    historyItem = it.getOrNull(1)
                ){
                    saesViewModel.changeSection(it.getOrNull(1)!!.section)
                }
                RecentActivityItem(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    historyItem = it.getOrNull(2)
                ){
                    saesViewModel.changeSection(it.getOrNull(2)!!.section)
                }
            }
        }
    }

    gradesViewModel.grades.value?.let {
        if(it.isNotEmpty()){
            HomeItem(
                modifier = Modifier.padding(top = 32.dp, start = 32.dp, end = 32.dp),
                title = "Calificaciones",
                icon = Icons.Rounded.FactCheck
            ){
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 28.dp)
                ) {
                    items(it){ grade ->
                        SmallGradeItem(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            classGrades = grade
                        ){
                            saesViewModel.changeSection(MenuSection.GRADES)
                        }
                    }
                }
            }
        }
    }

    etsViewModel.scores.value?.let {
        if(it.isNotEmpty()){
            HomeItem(
                modifier = Modifier.padding(top = 32.dp, start = 32.dp, end = 32.dp),
                title = "ETS",
                icon = Icons.Rounded.FactCheck
            ){
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 28.dp)
                ) {
                    items(it){ score ->
                        SmallGradeItem(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            etsScore = score
                        ){
                            saesViewModel.changeSection(MenuSection.GRADES)
                        }
                    }
                }
            }
        }
    }

    if(LocalContext.current.isNetworkAvailable()){
        HomeItem(
            modifier = Modifier.padding(top = 32.dp, start = 32.dp, end = 32.dp),
            title = "Noticias",
            icon = Icons.Rounded.Feed
        ){
            if(homeViewModel.schoolTweets.value != null){
                Column {
                    homeViewModel.schoolTweets.value?.forEach {
                        TwitterItem(
                            modifier = Modifier.padding(
                                horizontal = 32.dp,
                                vertical = 6.dp
                            ),
                            tweet = it
                        )
                    }
                }
            }else{
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}


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