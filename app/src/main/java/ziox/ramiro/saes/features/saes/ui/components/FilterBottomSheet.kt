package ziox.ramiro.saes.features.saes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
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
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100))
            .padding(vertical = 8.dp)
            .align(Alignment.CenterHorizontally)
            .size(32.dp, 4.dp)
            .background(Color.LightGray)
    ) {}
    Text(
        modifier = Modifier.padding(start = 32.dp,bottom = 16.dp, top = 8.dp),
        text = "Filtros",
        style = MaterialTheme.typography.h4
    )
    when(val state = filterViewModel.filterState.collectAsState(initial = null).value){
        is FilterState.FilterComplete -> LazyColumn(
            contentPadding = PaddingValues(
                start = 32.dp,
                end = 32.dp,
                top = 16.dp,
                bottom = 64.dp
            )
        ) {
            var isBreak = false
            val filtered = state.filterFields.filter {
                if (!it.isSelected){
                    isBreak = true
                    true
                }else{
                    !isBreak
                }
            }
            items(filtered.size){ i ->
                val item = filtered[i]

                when(item){
                    is SelectFilterField -> SelectFilter(item, filterViewModel)
                }
            }
        }
        null -> filterViewModel.getFilterFields()
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
    val selectedIndex = remember {
        mutableStateOf(selectFilterField.selectedIndex)
    }

    Text(
        modifier = Modifier.padding(bottom = 8.dp),
        text = selectFilterField.fieldName,
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
                            textColor = if (i != selectedIndex.value) infoColor else MaterialTheme.colors.onPrimary,
                            backgroundColor = if (i == selectedIndex.value) infoColor else null
                        ){
                            filterViewModel.selectFilterField(selectFilterField.itemId, i + selectFilterField.indexOffset)
                            selectedIndex.value = i
                        }
                    }
                })
            }

            flexbox
        }
    )
}