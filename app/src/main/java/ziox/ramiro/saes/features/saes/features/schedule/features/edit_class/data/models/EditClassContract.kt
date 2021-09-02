package ziox.ramiro.saes.features.saes.features.schedule.features.edit_class.data.models

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.data.models.CustomClassSchedule
import ziox.ramiro.saes.features.saes.features.schedule.features.edit_class.ui.screens.EditClassActivity

class EditClassContract : ActivityResultContract<ClassSchedule, CustomClassSchedule?>() {
    companion object{
        const val EditClassInput = "edit_class_input"
        const val EditClassOutput = "edit_class_output"
    }

    override fun createIntent(context: Context, input: ClassSchedule) = Intent(context, EditClassActivity::class.java).apply {
        putExtra(EditClassInput, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): CustomClassSchedule? {
        if(resultCode != RESULT_OK) return null

        return intent?.getParcelableExtra(EditClassOutput)
    }
}