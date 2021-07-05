package ziox.ramiro.saes.data.models

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import ziox.ramiro.saes.presentation.SelectSchoolActivity

class SelectSchoolContract : ActivityResultContract<Unit, School?>() {
    companion object {
        const val RESULT = "select_school_result"
    }

    override fun createIntent(context: Context, input: Unit?)
        = Intent(context, SelectSchoolActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): School? {
        if(resultCode != Activity.RESULT_OK) return null

        return intent?.getParcelableExtra(RESULT)
    }

}