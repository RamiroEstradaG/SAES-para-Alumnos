package ziox.ramiro.saes.features.saes.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.last
import ziox.ramiro.saes.features.saes.data.models.FilterField
import ziox.ramiro.saes.features.saes.data.models.FilterState
import ziox.ramiro.saes.features.saes.data.models.FilterViewModel
import ziox.ramiro.saes.features.saes.data.models.SelectFilterField
import ziox.ramiro.saes.ui.components.OutlineButton
import ziox.ramiro.saes.ui.theme.getCurrentTheme


@Composable
fun FilterBottomSheet(
    filterViewModel: FilterViewModel
) = Column(
    modifier = Modifier
        .fillMaxSize()
) {
    val filterCompleteStates = filterViewModel.fieldFilterStates.filter { it is FilterState.FilterComplete }.collectAsState(
        initial = null
    )
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100))
            .padding(vertical = 8.dp)
            .align(Alignment.CenterHorizontally)
            .size(32.dp, 4.dp)
            .background(getCurrentTheme().divider)
    ) {}
    Row(
        modifier = Modifier.padding(start = 32.dp, end = 32.dp,bottom = 16.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = "Filtros",
            style = MaterialTheme.typography.h4
        )
        when(filterViewModel.fieldFilterStatesAsState().value){
            is FilterState.FilterLoading -> CircularProgressIndicator(
                modifier = Modifier.size(24.dp)
            )
            else -> Box{}
        }
    }

    when(val state = filterViewModel.fieldFilterStatesAsState().value){
        is FilterState.FilterComplete -> FilterGroup(
            scrollState = scrollState,
            filterFields = state.filterFields,
            filterViewModel = filterViewModel
        )
        is FilterState.FilterLoading -> when(val completeStates = filterCompleteStates.value) {
            is FilterState.FilterComplete -> FilterGroup(
                scrollState = scrollState,
                filterFields = completeStates.filterFields,
                filterViewModel = filterViewModel
            )
        }
        null -> filterViewModel.getFilterFields()
    }
}

@Composable
fun FilterGroup(
    scrollState: ScrollState,
    filterFields: List<FilterField>,
    filterViewModel: FilterViewModel
) = Box(
    Modifier.verticalScroll(scrollState)
) {
    Column(
        Modifier
            .padding(
                start = 32.dp,
                end = 32.dp,
                top = 16.dp,
                bottom = 64.dp
            )

    ) {
        var isBreak = false
        val filtered = filterFields.filter { field ->
            if (!field.isSelected){
                isBreak = true
                true
            }else{
                !isBreak
            }
        }

        filtered.forEach { field ->
            when(field){
                is SelectFilterField -> SelectFilter(field, filterViewModel)
            }
        }
    }
}


@Composable
fun SelectFilter(
    selectFilterField: SelectFilterField,
    filterViewModel: FilterViewModel
) = Column(
    modifier = Modifier.padding(bottom = 16.dp)
) {
    val infoColor = getCurrentTheme().info
    val filterState = filterViewModel.fieldFilterStatesAsState()

    Text(
        text = if(selectFilterField.items.isNotEmpty()) selectFilterField.fieldName else "",
        style = MaterialTheme.typography.subtitle2
    )
    AndroidView(
        factory = {
            val flexbox = FlexboxLayout(it)
            flexbox.flexDirection = FlexDirection.ROW
            flexbox.flexWrap = FlexWrap.WRAP

            selectFilterField.items.forEachIndexed { i, value ->
                flexbox.addView(ComposeView(it).apply {
                    setContent {
                        OutlineButton(
                            modifier = Modifier.padding(end = 8.dp, top = 8.dp),
                            text = value,
                            borderColor = infoColor,
                            textColor = if (i != selectFilterField.selectedIndex) infoColor else MaterialTheme.colors.onPrimary,
                            backgroundColor = if (i == selectFilterField.selectedIndex) infoColor else null
                        ){
                            if(filterState.value !is FilterState.FilterLoading){
                                filterViewModel.selectFilterField(selectFilterField.itemId, i + selectFilterField.indexOffset)
                            }
                        }
                    }
                })
            }

            flexbox
        }
    )
}