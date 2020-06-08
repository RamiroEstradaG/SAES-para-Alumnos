package ziox.ramiro.saes.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.dialog_encuesta.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.utils.setPreference

class EncuestaDialog : DialogFragment() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog_NoActionBar_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.dialog_encuesta, container, false)

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)

        rootView.button.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            registerEvent(
                "ENCUESTA",
                "AGREE",
                "AGREE_DEPRECATION",
                FirebaseAnalytics.Event.SELECT_CONTENT
            )

            setPreference(activity, "deprecation", true)
        }

        return rootView
    }

    private fun registerEvent(
        itemId: String,
        itemName: String,
        contentType: String,
        event: String
    ) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
        firebaseAnalytics.logEvent(event, bundle)
        setPreference(context, "encuesta", true)
        Toast.makeText(context, "¡Gracias por tu comprensión!", Toast.LENGTH_SHORT).show()
        this.dismiss()
    }
}