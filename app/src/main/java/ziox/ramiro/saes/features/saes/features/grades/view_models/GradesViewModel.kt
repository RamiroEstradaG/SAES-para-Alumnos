package ziox.ramiro.saes.features.saes.features.grades.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ziox.ramiro.saes.features.saes.features.grades.data.models.ClassGrades
import ziox.ramiro.saes.features.saes.features.grades.data.repositories.GradesRepository
import ziox.ramiro.saes.utils.dismissAfterTimeout
import javax.inject.Inject

@HiltViewModel
class GradesViewModel @Inject constructor(
    private val gradesRepository: GradesRepository
) : ViewModel() {
    val grades = mutableStateOf<List<ClassGrades>?>(null)
    val error = MutableStateFlow<String?>(null)

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
            error.value = "Error al obtener las calificaciones"
        }
    }
}