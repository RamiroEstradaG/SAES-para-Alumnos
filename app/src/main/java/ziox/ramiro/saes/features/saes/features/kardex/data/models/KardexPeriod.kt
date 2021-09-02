package ziox.ramiro.saes.features.saes.features.kardex.data.models

data class KardexPeriod(
    val periodName: String = "",
    val kardexClasses: List<KardexClass> = listOf()
){
    val average : Double
        get() {
            val scores = kardexClasses.mapNotNull {
                it.score
            }

            return scores.sum().div(scores.size.toDouble())
        }

    override fun toString() = """
KardexPeriod(
    periodName: $periodName
    kardexClasses:
${kardexClasses.joinToString("\n") { it.toString().prependIndent() }}
)
    """.trimIndent()
}