package ziox.ramiro.saes.databases

import androidx.room.*

const val RECENT_ACTIVITY_TABLE_NAME = "recent_activity"

@Entity(tableName = RECENT_ACTIVITY_TABLE_NAME)
data class RecentActivity(
    @PrimaryKey val sectionId: String,
    @ColumnInfo(name = "section_name") val sectionName: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB, name = "section_icon") val sectionIcon: ByteArray,
    @ColumnInfo(name = "access_date") val accessDate : Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RecentActivity

        if (sectionName != other.sectionName) return false
        if (sectionId != other.sectionId) return false
        if (!sectionIcon.contentEquals(other.sectionIcon)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sectionName.hashCode()
        result = 31 * result + sectionId.hashCode()
        result = 31 * result + sectionIcon.contentHashCode()
        return result
    }
}

@Dao
interface RecentActivityDao {
    @Query("SELECT * FROM $RECENT_ACTIVITY_TABLE_NAME ORDER BY access_date DESC")
    fun getAll() : List<RecentActivity>

    @Query("SELECT * FROM $RECENT_ACTIVITY_TABLE_NAME ORDER BY access_date DESC LIMIT :size")
    fun getLast(size: Int) : List<RecentActivity>

    @Insert
    fun insert(recentActivity: RecentActivity)

    @Query("UPDATE $RECENT_ACTIVITY_TABLE_NAME SET access_date = :date, section_icon = :icon WHERE sectionId = :id")
    fun update(id : String, date : Long, icon: ByteArray)

    @Delete
    fun delete(recentActivity: RecentActivity)
}