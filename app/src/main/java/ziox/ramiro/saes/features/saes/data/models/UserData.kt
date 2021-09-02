package ziox.ramiro.saes.features.saes.data.models

import ziox.ramiro.saes.features.saes.features.kardex.data.models.KardexData

data class UserData(
    val uid: String = "",
    val studentId: String = "",
    val school: String = "",
    val career: String = "",
    val generalScore: Double? = null,
    val calendarIds: List<String> = listOf(),
    val kardexData: KardexData = KardexData(),
    val isRegistered: Boolean = false
){
    override fun equals(other: Any?): Boolean {
        return if (other is UserData){
            uid == other.uid
                    && studentId == other.studentId
                    && school == other.school
                    && career == other.career
                    && generalScore == other.generalScore
                    && calendarIds == other.calendarIds
                    && calendarIds.size == other.calendarIds.size
                    && kardexData == other.kardexData
                    && isRegistered == other.isRegistered
        }else{
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + studentId.hashCode()
        result = 31 * result + school.hashCode()
        result = 31 * result + career.hashCode()
        result = 31 * result + (generalScore?.hashCode() ?: 0)
        result = 31 * result + calendarIds.hashCode() + calendarIds.size
        result = 31 * result + kardexData.hashCode()
        result = 31 * result + isRegistered.hashCode()
        return result
    }

    override fun toString() = """
UserData(
    uid: $uid
    studentId: $studentId
    school: $school
    career: $career
    generalScore: $generalScore
    calendarIds: $calendarIds   
    isRegistered: $isRegistered
    kardexData: 
${kardexData.toString().prependIndent()}
)
    """.trimIndent()
}
