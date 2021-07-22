package ziox.ramiro.saes.features.saes.features.kardex.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject
import ziox.ramiro.saes.utils.MMMddyyyy_toDate
import ziox.ramiro.saes.utils.toProperCase


data class KardexData(
    val generalScore: Double?,
    val kardexPeriods: List<KardexPeriod>
){
    fun generalScoreAt(periodIndex: Int): Double{
        val untilPeriod = ArrayList<KardexClass>().apply {
            for (i in 0..periodIndex){
                addAll(kardexPeriods[i].kardexClasses)
            }
        }.mapNotNull { it.score }

        return untilPeriod.sum().div(untilPeriod.size.toDouble())
    }
}


@Entity(tableName = "kardex")
data class KardexDataRoom(
    @PrimaryKey
    val userId: String,
    @ColumnInfo(name = "json_data")
    val data: JSONObject
) {
    fun toKardexData() : KardexData{
        val data = data.getJSONObject("data")
        val periods = data.getJSONArray("periods")

        return KardexData(
            data.getString("generalScore").toDoubleOrNull(),
            List(periods.length()){ i ->
                val period = periods[i] as JSONObject
                val periodClasses = period.getJSONArray("classes")

                KardexPeriod(
                    period.getString("periodName").toProperCase(),
                    List(periodClasses.length()){ e ->
                        val kardexClass = periodClasses[e] as JSONObject
                        KardexClass(
                            kardexClass.getString("id"),
                            kardexClass.getString("name").toProperCase(),
                            kardexClass.getString("date").MMMddyyyy_toDate(),
                            kardexClass.getString("period"),
                            EvaluationType.fromSAES(kardexClass.getString("evaluationType")),
                            kardexClass.getString("score").toIntOrNull()
                        )
                    }
                )
            }
        )
    }
}