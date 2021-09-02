package ziox.ramiro.saes.features.saes.features.occupancy.data.models

data class ClassOccupancy(
    val classId: String,
    val group: String,
    val className: String,
    val semester: Int,
    val maximumQuota: Int,
    val currentlySignedUp: Int
)
