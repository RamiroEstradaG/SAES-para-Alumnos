package ziox.ramiro.saes.features.saes.features.grades.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.data.data_providers.ScrapException
import ziox.ramiro.saes.features.saes.data.repositories.StorageRepository
import ziox.ramiro.saes.features.saes.features.grades.data.models.ClassGrades
import ziox.ramiro.saes.features.saes.features.grades.data.repositories.GradesRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class GradesViewModel @Inject constructor(
    private val gradesRepository: GradesRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {
    val grades = mutableStateOf<List<ClassGrades>?>(null)
    val error = MutableStateFlow<String?>(null)
    val scrapError = MutableStateFlow<ScrapException?>(null)

    init {
        fetchGrades()
        error.dismissAfterTimeout()
    }

    private fun fetchGrades() = viewModelScope.launch {
        grades.value = null

        kotlin.runCatching {
            gradesRepository.getMyGrades()
        }.onSuccess {
            grades.value = it
        }.onFailure {
            if(it is ScrapException) {
                scrapError.value = it
            } else {
                error.value = "Error al obtener las calificaciones"
            }
        }
    }

    fun uploadSourceCode() = viewModelScope.launch {
        val error = scrapError.value
        scrapError.value = null
        if(error == null) return@launch

        val sourceCode = error.sourceCode ?: return@launch

        runCatching {
            storageRepository.uploadFile(
                content = sourceCode,
                filePath = "grades_errors",
                fileName = "${Date().time}.html"
            )
        }
    }
}