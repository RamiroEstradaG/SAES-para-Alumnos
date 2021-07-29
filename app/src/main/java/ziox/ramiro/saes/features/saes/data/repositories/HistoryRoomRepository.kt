package ziox.ramiro.saes.features.saes.data.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ziox.ramiro.saes.features.saes.data.models.HistoryItem

@Dao
interface HistoryRoomRepository {
    @Query("SELECT DISTINCT * FROM user_history ORDER BY date DESC LIMIT 3")
    fun getLastThree(): List<HistoryItem>

    @Query("SELECT * FROM user_history ORDER BY date DESC LIMIT 1")
    fun getLastItem(): HistoryItem?

    @Insert
    fun addItem(item: HistoryItem)
}