package ziox.ramiro.saes.features.saes.features.profile.data.models

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult


class QRCodeScannerContract : ActivityResultContract<Unit, String?>() {
    override fun createIntent(context: Context, input: Unit) : Intent
        = IntentIntegrator(context as Activity)
            .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
            .setBarcodeImageEnabled(true)
            .setBeepEnabled(false)
            .setOrientationLocked(false)
            .setPrompt("Escanea el QR de tu credencial")
            .createScanIntent()

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if(resultCode != RESULT_OK) return null

        val result : IntentResult? = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, intent)

        return result?.contents
    }
}