package ziox.ramiro.saes.features.saes.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.features.saes.data.models.FilterField
import ziox.ramiro.saes.features.saes.data.models.FilterViewModel
import ziox.ramiro.saes.features.saes.data.models.RadioGroupFilterField
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
        if (filterViewModel.filterFields.value == null){
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp)
            )
        }
    }
    if (filterViewModel.filterFields.value != null){
        filterViewModel.filterFields.value?.let {
            FilterGroup(
                scrollState = scrollState,
                filterFields = it,
                filterViewModel = filterViewModel
            )
        }
    }else{
        FilterGroup(
            scrollState = scrollState,
            filterFields = filterViewModel.filterFieldsComplete.value,
            filterViewModel = filterViewModel
        )
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
            if (!field.isActive){
                isBreak = true
                true
            }else{
                !isBreak
            }
        }

        filtered.forEach { field ->
            when(field){
                is SelectFilterField -> SelectFilter(field, filterViewModel)
                is RadioGroupFilterField -> RadioGroupFilter(field, filterViewModel)
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

    Text(
        text = if(selectFilterField.items.isNotEmpty()) selectFilterField.fieldName else "",
        style = MaterialTheme.typography.subtitle2
    )
    FlexView(
        content = selectFilterField.items.mapIndexed { i, value ->
            {
                OutlineButton(
                    modifier = Modifier.padding(end = 8.dp, top = 8.dp),
                    text = value,
                    borderColor = infoColor,
                    textColor = if (i != selectFilterField.selectedIndex) infoColor else MaterialTheme.colors.onPrimary,
                    backgroundColor = if (i == selectFilterField.selectedIndex) infoColor else null
                ){
                    if (filterViewModel.filterFields.value != null){
                        filterViewModel.selectSelect(selectFilterField.itemId, i + selectFilterField.indexOffset)
                    }
                }
            }
        }
    )
}

@Composable
fun RadioGroupFilter(
    selectFilterField: RadioGroupFilterField,
    filterViewModel: FilterViewModel
) = Column(
    modifier = Modifier.padding(bottom = 16.dp)
) {
    val infoColor = getCurrentTheme().info

    Text(
        text = if(selectFilterField.items.isNotEmpty()) selectFilterField.fieldName else "",
        style = MaterialTheme.typography.subtitle2
    )
    FlexView(
        content = selectFilterField.items.mapIndexed { i, value ->
            {
                OutlineButton(
                    modifier = Modifier.padding(end = 8.dp, top = 8.dp),
                    text = value.second,
                    borderColor = infoColor,
                    textColor = if (i != selectFilterField.selectedIndex) infoColor else MaterialTheme.colors.onPrimary,
                    backgroundColor = if (i == selectFilterField.selectedIndex) infoColor else null
                ){
                    if (filterViewModel.filterFields.value != null){
                        filterViewModel.selectRadioGroup(value.first)
                    }
                }
            }
        }
    )
}