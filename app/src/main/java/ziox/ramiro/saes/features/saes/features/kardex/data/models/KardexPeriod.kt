package ziox.ramiro.saes.features.saes.features.kardex.data.models

data class KardexPeriod(
    val periodName: String,
    val kardexClasses: List<KardexClass>
){
    val average : Double
        get() {
            val scores = kardexClasses.mapNotNull {
                it.score
            }

            return scores.sum().div(scores.size.toDouble())
        }
}