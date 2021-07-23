package ziox.ramiro.saes.features.saes.features.performance.data.models

enum class TriStateBoolean {
    TRUE,
    FALSE,
    UNSET;

    companion object{
        fun fromBoolean(boolean: Boolean?) = if (boolean == null) UNSET else if (boolean) TRUE else FALSE
    }
}