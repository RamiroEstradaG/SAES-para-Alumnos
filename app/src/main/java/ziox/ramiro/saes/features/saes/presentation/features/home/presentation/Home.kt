package ziox.ramiro.saes.features.saes.presentation.features.home.presentation

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.filter
import ziox.ramiro.saes.data.models.viewModelFactory
import ziox.ramiro.saes.features.saes.presentation.features.home.data.repositories.TwitterRetrofitRepository
import ziox.ramiro.saes.features.saes.presentation.features.home.ui.components.TwitterItem
import ziox.ramiro.saes.features.saes.presentation.features.home.view_models.HomeState
import ziox.ramiro.saes.features.saes.presentation.features.home.view_models.HomeViewModel
import ziox.ramiro.saes.features.saes.ui.components.SmallGradeItem
import ziox.ramiro.saes.features.saes.ui.components.RecentActivityItem

@ExperimentalMaterialApi
@Composable
fun Home(
    homeViewModel: HomeViewModel = viewModel(
        factory = viewModelFactory { HomeViewModel(TwitterRetrofitRepository()) }
    )
) = Column(
    modifier = Modifier.padding(bottom = 64.dp).verticalScroll(rememberScrollState())
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
    HomeItem(
        modifier = Modifier.padding(top = 32.dp, start = 32.dp, end = 32.dp),
        title = "Calificaciones",
        icon = Icons.Rounded.FactCheck
    ){
        LazyRow(
            contentPadding = PaddingValues(horizontal = 28.dp)
        ) {
            items(15){
                SmallGradeItem(
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
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
                            vertical = 4.dp
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
    title: String = "Title",
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