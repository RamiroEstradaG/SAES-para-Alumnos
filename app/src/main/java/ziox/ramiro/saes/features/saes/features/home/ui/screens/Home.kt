package ziox.ramiro.saes.features.saes.features.home.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Feed
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import ziox.ramiro.saes.features.saes.features.home.data.repositories.TwitterRetrofitRepository
import ziox.ramiro.saes.features.saes.features.home.ui.components.RecentActivityItem
import ziox.ramiro.saes.features.saes.features.home.ui.components.SmallGradeItem
import ziox.ramiro.saes.features.saes.features.home.ui.components.TweetItem
import ziox.ramiro.saes.features.saes.features.home.view_models.HomeViewModel
import ziox.ramiro.saes.features.saes.features.kardex.data.repositories.KardexWebViewRepository
import ziox.ramiro.saes.features.saes.features.kardex.view_models.KardexViewModel
import ziox.ramiro.saes.features.saes.features.performance.ui.screens.KardexChart
import ziox.ramiro.saes.features.saes.features.schedule.data.models.getCurrentClass
import ziox.ramiro.saes.features.saes.features.schedule.data.models.getNextClass
import ziox.ramiro.saes.features.saes.features.schedule.data.repositories.ScheduleWebViewRepository
import ziox.ramiro.saes.features.saes.features.schedule.view_models.ScheduleViewModel
import ziox.ramiro.saes.features.saes.view_models.MenuSection
import ziox.ramiro.saes.features.saes.view_models.SAESViewModel
import ziox.ramiro.saes.ui.components.ErrorSnackbar
import ziox.ramiro.saes.utils.updateWidgets

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Home(
    context: Context = LocalContext.current,
    homeViewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            HomeViewModel(
                LocalAppDatabase.invoke(context).historyRepository(),
                TwitterRetrofitRepository()
            )
        }
    ),
    gradesViewModel: GradesViewModel = viewModel(
        factory = viewModelFactory { GradesViewModel(GradesWebViewRepository(context)) }
    ),
    kardexViewModel: KardexViewModel = viewModel(
        factory = viewModelFactory { KardexViewModel(KardexWebViewRepository(context)) }
    ),
    saesViewModel: SAESViewModel = viewModel(),
    etsViewModel: ETSViewModel = viewModel(
        factory = viewModelFactory { ETSViewModel(ETSWebViewRepository(context)) }
    ),
    scheduleViewModel: ScheduleViewModel = viewModel(
        factory = viewModelFactory { ScheduleViewModel(ScheduleWebViewRepository(context),LocalAppDatabase.invoke(context).customScheduleGeneratorRepository()) }
    )
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(top = 32.dp, bottom = 64.dp)
    ) {
        homeViewModel.historyItems.value?.let {
            if(it.isNotEmpty()){
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
        }


        AnimatedVisibility(visible = scheduleViewModel.scheduleList.isNotEmpty()) {
            LocalContext.current.updateWidgets()
            val currentClass = scheduleViewModel.scheduleList.getCurrentClass()

            if(currentClass != null){
                HomeItem(
                    modifier = Modifier.padding(top = 32.dp, start = 32.dp, end = 32.dp),
                    title = "Clase en curso",
                    icon = Icons.Outlined.Schedule
                ){
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(currentClass.color.toULong())
                        )
                    ) {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = currentClass.className,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = scheduleViewModel.scheduleList.isNotEmpty()) {
            val nextClass = scheduleViewModel.scheduleList.getNextClass()

            if(nextClass != null){
                HomeItem(
                    modifier = Modifier.padding(top = 32.dp, start = 32.dp, end = 32.dp),
                    title = "Siguiente clase",
                    icon = Icons.Rounded.Update
                ){
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(nextClass.color.toULong())
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp),
                                text = nextClass.className,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Text(
                                modifier = Modifier.padding(16.dp),
                                text = nextClass.scheduleDayTime.start.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = gradesViewModel.grades.value != null) {
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
        }


        AnimatedVisibility(visible = etsViewModel.scores.value != null) {
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
        }


        AnimatedVisibility(visible = kardexViewModel.kardexData.value != null) {
            kardexViewModel.kardexData.value?.let {
                HomeItem(
                    modifier = Modifier.padding(top = 32.dp, start = 32.dp, end = 32.dp),
                    title = "Rendimiento",
                    icon = Icons.Rounded.Insights
                ){
                    Box(
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        KardexChart(it)
                    }
                }
            }
        }

        AnimatedVisibility(visible = homeViewModel.tweets.value != null) {
            homeViewModel.tweets.value?.let {
                HomeItem(
                    modifier = Modifier.padding(top = 32.dp, start = 32.dp, end = 32.dp),
                    title = "Noticias",
                    icon = Icons.Rounded.Feed
                ){
                    Column(
                        modifier = Modifier.padding(
                            start = 32.dp,
                            end = 32.dp,
                            bottom = 16.dp
                        )
                    ) {
                        it.forEach {
                            TweetItem(it)
                        }
                    }
                }
            }
        }
    }
    ErrorSnackbar(homeViewModel.error)
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
            style = MaterialTheme.typography.headlineMedium
        )
    }
    content()
}