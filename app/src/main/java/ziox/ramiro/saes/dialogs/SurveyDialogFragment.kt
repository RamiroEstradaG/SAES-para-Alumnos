package ziox.ramiro.saes.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import ziox.ramiro.saes.databinding.DialogFragmentSurveyBinding
import ziox.ramiro.saes.utils.setPreference

class SurveyDialogFragment : DialogFragment() {
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
    ): View {
        val rootView = DialogFragmentSurveyBinding.inflate(inflater, container, false)

        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)

        rootView.acceptButton.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            setPreference(activity, "deprecation", true)
            registerEvent()
        }

        return rootView.root
    }

    private fun registerEvent() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "ENCUESTA")
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "AGREE")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "AGREE_DEPRECATION")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        setPreference(context, "encuesta", true)
        Toast.makeText(context, "¡Gracias por tu comprensión!", Toast.LENGTH_SHORT).show()
        this.dismiss()
    }
}