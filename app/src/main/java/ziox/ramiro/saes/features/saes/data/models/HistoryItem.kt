package ziox.ramiro.saes.features.saes.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ziox.ramiro.saes.features.saes.view_models.MenuSection
import java.util.*

@Entity(tableName = "user_history")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "section")
    val section: MenuSection,
    @ColumnInfo(name = "date")
    val date: Date = Date()
)
