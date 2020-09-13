package ziox.ramiro.saes.dialogs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_menu_saes.view.*
import kotlinx.android.synthetic.main.header_menu.view.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.activities.MainActivity
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.utils.getBoleta
import ziox.ramiro.saes.utils.getPreference
import ziox.ramiro.saes.utils.isNetworkAvailable
import java.util.*

/**
 * Creado por Ramiro el 10/14/2018 a las 4:49 PM para SAESv2.
 */
class MenuDrawerSaesModal : BottomSheetDialogFragment() {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.dialog_menu_saes, container, false)
        try {
            val res = resources.getIdentifier(
                getPreference(
                    activity,
                    "name_escuela",
                    "Instituto Politecnico Nacional"
                ).toLowerCase(Locale.ROOT).replace(" ", ""), "drawable", activity?.packageName
            )
            rootView.logoMenu.setImageResource(
                if (res > 0) {
                    res
                } else {
                    R.drawable.ic_logopoli
                }
            )
        } catch (e: Exception) {

        }
        rootView.boletaMenu.text = getBoleta(activity)
        rootView.nombreMenu.text = getPreference(activity, "nombre", "")

        if (activity?.isNetworkAvailable() == false) {
            rootView.buttonEscolarizado.visibility = View.GONE
            rootView.buttonNoEscolarizado.visibility = View.GONE
        }

        rootView.buttonEscolarizado.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            context?.startActivity(
                Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse("https://www.ipn.mx/assets/files/main/docs/inicio/cal-Escolarizada-20-21.pdf"))
            )
        }

        rootView.buttonNoEscolarizado.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            context?.startActivity(
                Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse("https://www.ipn.mx/assets/files/main/docs/inicio/cal-NoEscolarizada-20-21.pdf"))
            )
        }

        rootView.closeMenu.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            CookieManager.getInstance().removeAllCookies {
                activity?.startActivity(Intent(activity, MainActivity::class.java))
                activity?.finish()
            }
        }

        rootView.perfilButton.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.javaClass.canonicalName}")
            (activity as SAESActivity?)?.postNavigationItemSelected(R.id.perfilButton, false)
            this@MenuDrawerSaesModal.dismiss()
        }

        if (activity is SAESActivity) {
            rootView.navParen.setCheckedItem((activity as SAESActivity?)?.getActivatedItem()!!)
            rootView.navParen.setNavigationItemSelectedListener {
                (activity as SAESActivity?)?.onNavigationItemSelected(it)
                this@MenuDrawerSaesModal.dismiss()
                true
            }
        }
        return rootView
    }
}