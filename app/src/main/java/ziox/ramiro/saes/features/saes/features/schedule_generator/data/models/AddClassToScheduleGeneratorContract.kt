package ziox.ramiro.saes.features.saes.features.schedule_generator.data.models

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import ziox.ramiro.saes.features.saes.features.schedule.data.models.ClassScheduleCollection
import ziox.ramiro.saes.features.saes.features.schedule_generator.ui.screens.AddClassToScheduleGeneratorActivity

class AddClassToScheduleGeneratorContract: ActivityResultContract<Unit, ClassScheduleCollection?>() {
    companion object{
        const val INTENT_EXTRA_RESULT_ADD_CLASS = "intent_extra_result_add_class"
    }

    override fun createIntent(context: Context, input: Unit?) = Intent(context, AddClassToScheduleGeneratorActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): ClassScheduleCollection? {
        if (resultCode != Activity.RESULT_OK) return null

        return intent?.getParcelableExtra(INTENT_EXTRA_RESULT_ADD_CLASS)
    }
}