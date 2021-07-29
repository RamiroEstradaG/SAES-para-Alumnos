package ziox.ramiro.saes.features.saes.features.ets.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ets_scores")
data class ETSScore(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "ets_period")
    val etsPeriod: String,
    @ColumnInfo(name = "class_name")
    val className: String,
    @ColumnInfo(name = "grade")
    val grade: Int?
)
