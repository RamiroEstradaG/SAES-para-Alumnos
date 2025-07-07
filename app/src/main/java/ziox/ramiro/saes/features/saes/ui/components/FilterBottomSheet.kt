package ziox.ramiro.saes.features.saes.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ziox.ramiro.saes.features.saes.data.models.FilterField
import ziox.ramiro.saes.features.saes.data.models.FilterViewModel
import ziox.ramiro.saes.features.saes.data.models.RadioGroupFilterField
import ziox.ramiro.saes.features.saes.data.models.SelectFilterField


@Composable
fun FilterBottomSheet(
    filterViewModel: FilterViewModel
) = Column(
    modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
) {
    val scrollState = rememberScrollState()

//    Box(
//        modifier = Modifier
//            .clip(RoundedCornerShape(100))
//            .padding(vertical = 8.dp)
//            .align(Alignment.CenterHorizontally)
//            .size(32.dp, 4.dp)
//            .background(getCurrentTheme().divider)
//    ) {}
    Row(
        modifier = Modifier.padding(start = 32.dp, end = 32.dp, bottom = 16.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = "Filtros",
            style = MaterialTheme.typography.headlineLarge
        )
        if (filterViewModel.filterFields.value == null) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp)
            )
        }
    }
    if (filterViewModel.filterFields.value != null) {
        filterViewModel.filterFields.value?.let {
            FilterGroup(
                scrollState = scrollState,
                filterFields = it,
                filterViewModel = filterViewModel
            )
        }
    } else {
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
            if (!field.isActive) {
                isBreak = true
                true
            } else {
                !isBreak
            }
        }

        filtered.forEach { field ->
            when (field) {
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
    val infoColor = MaterialTheme.colorScheme.primary

    Text(
        text = if (selectFilterField.items.isNotEmpty()) selectFilterField.fieldName else "",
        style = MaterialTheme.typography.titleMedium
    )
    FlexView(
        content = selectFilterField.items.mapIndexed { i, value ->
            {
                FilterChip(
                    selected = i == selectFilterField.selectedIndex,
                    label = {
                        Text(value)
                    },
                    onClick = {
                        if (filterViewModel.filterFields.value != null) {
                            filterViewModel.selectSelect(
                                selectFilterField.itemId,
                                i + selectFilterField.indexOffset
                            )
                        }
                    },
                    leadingIcon =
                        if (i == selectFilterField.selectedIndex) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = "Localized Description",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                                )
                            }
                        } else {
                            null
                        },
                )
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
    val infoColor = MaterialTheme.colorScheme.primary

    Text(
        text = if (selectFilterField.items.isNotEmpty()) selectFilterField.fieldName else "",
        style = MaterialTheme.typography.titleMedium
    )
    FlexView(
        content = selectFilterField.items.mapIndexed { i, value ->
            {
                FilterChip(
                    selected = i == selectFilterField.selectedIndex,
                    label = {
                        Text(value.second)
                    },
                    onClick = {
                        if (filterViewModel.filterFields.value != null) {
                            filterViewModel.selectRadioGroup(value.first)
                        }
                    },
                    leadingIcon =
                        if (i == selectFilterField.selectedIndex) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = "Localized Description",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                                )
                            }
                        } else {
                            null
                        },

                    )
            }
        }
    )
}