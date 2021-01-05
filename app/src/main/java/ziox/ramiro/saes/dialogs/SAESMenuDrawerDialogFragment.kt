package ziox.ramiro.saes.dialogs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.MainActivity
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databinding.DialogFragmentSaesMenuDrawerBinding
import ziox.ramiro.saes.utils.getBoleta
import ziox.ramiro.saes.utils.getPreference
import ziox.ramiro.saes.utils.isNetworkAvailable
import java.util.*

/**
 * Creado por Ramiro el 10/14/2018 a las 4:49 PM para SAESv2.
 */
class SAESMenuDrawerDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = DialogFragmentSaesMenuDrawerBinding.inflate(inflater, container, false)
        try {
            val res = resources.getIdentifier(
                getPreference(
                    activity,
                    "name_escuela",
                    "Instituto Politecnico Nacional"
                ).toLowerCase(Locale.ROOT).replace(" ", ""), "drawable", activity?.packageName
            )
            rootView.header.schoolLogoImageView.setImageResource(
                if (res > 0) {
                    res
                } else {
                    R.drawable.ic_logopoli
                }
            )
        } catch (e: Exception) {
            Log.e(this.javaClass.canonicalName, e.toString())
        }
        rootView.header.studentIdTextView.text = getBoleta(activity)
        rootView.header.studentNameTextView.text = getPreference(activity, "nombre", "")

        if (activity?.isNetworkAvailable() == false) {
            rootView.header.schooledCalendarButton.visibility = View.GONE
            rootView.header.notSchooledCalendarButton.visibility = View.GONE
        }

        rootView.header.schooledCalendarButton.setOnClickListener(this)
        rootView.header.notSchooledCalendarButton.setOnClickListener(this)
        rootView.header.logOutButton.setOnClickListener(this)
        rootView.header.profileButton.setOnClickListener(this)

        if (activity is SAESActivity) {
            rootView.navParen.setCheckedItem((activity as SAESActivity?)?.getActivatedItem()!!)
            rootView.navParen.setNavigationItemSelectedListener {
                (activity as SAESActivity?)?.onNavigationItemSelected(it)
                this@SAESMenuDrawerDialogFragment.dismiss()
                true
            }
        }
        return rootView.root
    }

    override fun onClick(view: View) {
        crashlytics.log("Click en ${resources.getResourceName(view.id)} en la clase ${this.javaClass.canonicalName}")
        when(view.id){
            R.id.schooled_calendar_button -> {
                context?.startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("https://www.ipn.mx/assets/files/main/docs/inicio/cal-Escolarizada-20-21.pdf"))
                )
            }
            R.id.not_schooled_calendar_button -> {
                context?.startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("https://www.ipn.mx/assets/files/main/docs/inicio/cal-NoEscolarizada-20-21.pdf"))
                )
            }
            R.id.log_out_button ->  {
                CookieManager.getInstance().removeAllCookies {
                    activity?.startActivity(Intent(activity, MainActivity::class.java))
                    activity?.finish()
                }
            }
            R.id.profile_button -> {
                (activity as SAESActivity?)?.postNavigationItemSelected(R.id.profile_button, false)
                this@SAESMenuDrawerDialogFragment.dismiss()
            }
        }
    }
}