package ziox.ramiro.saes.features.saes.features.grades.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "class_grades")
data class ClassGrades(
    @PrimaryKey
    val className: String,
    @ColumnInfo(name = "p1")
    val p1: Int?,
    @ColumnInfo(name = "p2")
    val p2: Int?,
    @ColumnInfo(name = "p3")
    val p3: Int?,
    @ColumnInfo(name = "extra")
    val extra: Int?,
    @ColumnInfo(name = "final_score")
    val finalScore: Int?
)
