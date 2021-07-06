package ziox.ramiro.saes.features.saes.features.grades.view_models

import ziox.ramiro.saes.data.models.ViewModelEvent
import ziox.ramiro.saes.data.models.ViewModelState
import ziox.ramiro.saes.features.saes.features.grades.data.models.ClassGrades

sealed class GradesState : ViewModelState {
    class GradesLoading : GradesState()
    class GradesComplete(val grades: List<ClassGrades>) : GradesState()
}

sealed class GradesEvent : ViewModelEvent {
    class Error(val message: String) : GradesEvent()
}
