package ziox.ramiro.saes.utils

import android.webkit.JavascriptInterface
import org.json.JSONObject

class UtilsJavascriptInterface {
    @JavascriptInterface
    fun analiseColumns(cols: Array<String>, firstRow: Array<String>): String {
        val mondayIndex = cols.indexOfFirst {
            it.trim().matches(Regex("lunes|lun", RegexOption.IGNORE_CASE))
        }
        val fridayIndex = cols.indexOfFirst {
            it.trim().matches(Regex("viernes|vie", RegexOption.IGNORE_CASE))
        }
        val buildingIndex = cols.indexOfFirst {
            it.trim().matches(Regex("edificio(s)?", RegexOption.IGNORE_CASE))
        }
        val classroomIndex = cols.indexOfFirst {
            it.trim().matches(Regex("sal[oó]n(es)?", RegexOption.IGNORE_CASE))
        }
        val teacherIndex = cols.indexOfFirst {
            it.trim()
                .matches(Regex("profesor(es)?|maestro(s)?", RegexOption.IGNORE_CASE))
        }
        val subjectIndex = cols.zip(firstRow).withIndex().indexOfFirst {
            (it.value.second.trim().length >= 7 && it.index != teacherIndex) || it.value.first.trim()
                .matches(Regex("materia", RegexOption.IGNORE_CASE))
        }
        val groupIndex = cols.zip(firstRow).withIndex().indexOfFirst {
            (it.value.second.trim()
                .isSchoolGroup() && it.index !in mondayIndex..fridayIndex && it.index != buildingIndex && it.index != classroomIndex && it.index != teacherIndex && it.index != subjectIndex)
                    || it.value.first.trim().matches(Regex("grupo", RegexOption.IGNORE_CASE))
        }

        return JSONObject(
            mapOf(
                "mondayIndex" to mondayIndex,
                "fridayIndex" to fridayIndex,
                "groupIndex" to groupIndex,
                "subjectIndex" to subjectIndex,
                "teacherIndex" to teacherIndex,
                "buildingIndex" to buildingIndex,
                "classroomIndex" to classroomIndex
            )
        ).toString()
    }

    @JavascriptInterface
    fun getRecentETSType(etsTypes: Array<String>): Int {
        var greatestIndex: Int? = null
        var greatestValue: Double = Double.MIN_VALUE

        for ((i, type) in etsTypes.withIndex()) {
            val values = type.split("20")

            if (values.size == 2) {
                val currentValue = values[1].toInt() + (MES.indexOf(values[0].uppercase()) / 12.0)

                if (currentValue > greatestValue) {
                    greatestIndex = i
                    greatestValue = currentValue
                }
            }
        }

        return greatestIndex ?: 0
    }
}