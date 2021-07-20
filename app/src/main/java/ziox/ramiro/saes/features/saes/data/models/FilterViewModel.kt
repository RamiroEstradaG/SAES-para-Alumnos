package ziox.ramiro.saes.features.saes.data.models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import org.json.JSONArray
import org.json.JSONObject

abstract class FilterViewModel: ViewModel() {
    val filterFields = mutableStateOf<List<FilterField>?>(null)
    val filterFieldsComplete = mutableStateOf<List<FilterField>>(listOf())
    val filterError = mutableStateOf<String?>(null)

    abstract fun getFilterFields(): Any
    abstract fun selectSelect(itemId: String, newIndex: Int?): Any
    abstract fun selectRadioGroup(fieldId: String): Any
}

data class SelectFilterField(
    override val itemId: String,
    override val fieldName: String,
    val selectedIndex: Int?,
    val indexOffset: Int,
    val items: List<String>
): FilterField {
    override val isActive: Boolean = selectedIndex != null

    companion object{
        fun fromJson(jsonObject: JSONObject): SelectFilterField{
            val options = jsonObject.getJSONArray("options")

            return SelectFilterField(
                jsonObject.getString("id"),
                jsonObject.getString("name"),
                jsonObject.getString("selectedIndex").toIntOrNull(),
                jsonObject.getInt("offset"),
                List(options.length()){ e ->
                    options[e].toString()
                }
            )
        }
    }
}

data class RadioGroupFilterField(
    override val itemId: String,
    override val fieldName: String,
    val selectedIndex: Int?,
    val items: List<Pair<String,String>>
): FilterField{
    override val isActive: Boolean = true

    companion object{
        fun fromJson(jsonObject: JSONObject): RadioGroupFilterField{
            val ids = jsonObject.getJSONArray("ids").map { it.toString() }
            val names = jsonObject.getJSONArray("options").map { it.toString() }

            return RadioGroupFilterField(
                "",
                jsonObject.getString("name"),
                jsonObject.getInt("selectedIndex"),
                ids.zip(names)
            )
        }
    }
}

fun <T>JSONArray.map(block: (Any) -> T): List<T> = List(this.length()){
    block(this.get(it))
}

interface FilterField {
    val itemId: String
    val isActive: Boolean
    val fieldName: String
}


interface FilterRepository{
    suspend fun getFilters(): List<FilterField>
    suspend fun selectSelect(fieldId: String, newIndex: Int?): List<FilterField>
    suspend fun selectRadioGroup(fieldId: String): List<FilterField>
}