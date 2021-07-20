package ziox.ramiro.saes.data.models

data class Auth(
    val isLoggedIn: Boolean,
    val errorMessage: String?
){
    companion object {
        val Empty = Auth(
            false,
            null
        )
    }
}