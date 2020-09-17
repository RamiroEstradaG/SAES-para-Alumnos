package ziox.ramiro.saes.utils

import java.util.*

fun <K, V>Map<K, V>.getKeyOfValue(value: Any, default: K) : K{
    for(m in this){
        if(m.value == value){
            return m.key
        }
    }

    return default
}

fun mesToInt(mes: String): Int{
    for((i, v) in MES.withIndex()){
        if(v == mes.toUpperCase(Locale.ROOT)) return i
    }
    return 0
}

fun <T>List<T>.joinToSentence() : String{
    return if (this.size > 1){
        "${this.subList(0, this.lastIndex).joinToString(", ")} y ${this.last()}"
    }else{
        this.joinToString(", ")
    }
}
