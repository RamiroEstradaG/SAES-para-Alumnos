package ziox.ramiro.saes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ziox.ramiro.saes.activities.SAESActivity
import ziox.ramiro.saes.databinding.FragmentProfileBinding
import ziox.ramiro.saes.utils.getBoleta
import ziox.ramiro.saes.utils.getPreference
import ziox.ramiro.saes.utils.getUrl
import ziox.ramiro.saes.utils.initWebView


/**
 * Creado por Ramiro el 12/12/2018 a las 9:15 PM para SAESv2.
 */
class ProfileFragment : Fragment() {
    lateinit var rootView: FragmentProfileBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = FragmentProfileBinding.inflate(inflater, container, false)

        rootView.profileTabs.setupWithViewPager(rootView.profilePager)
        rootView.profilePager.adapter = PerfilAdapter(childFragmentManager)

        initWebView(rootView.profilePictureWebView, object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.loadUrl("javascript: if(document.getElementsByTagName(\"img\")[0] != null){" +
                        "document.body.style = \"\";" +
                        "document.getElementsByTagName(\"img\")[0].style.width = 100;" +
                        "document.getElementsByTagName(\"img\")[0].style.position = \"absolute\";" +
                        "document.getElementsByTagName(\"img\")[0].style.top = 0;" +
                        "document.getElementsByTagName(\"img\")[0].style.left = 0;" +
                        "window.scrollTo(0, 10);" +
                        "}else{" +
                        "   window.location = \"https://cdn2.dailytrend.mx/media/bi/mediabrowser/2017/09/colecta-viveres-terremoto-mexico-19-septiembre-2017-12.jpg\";" +
                        "}")
            }
        }, (activity as SAESActivity).getProgressBar())

        rootView.userNameTextView.text = getPreference(activity, "nombre", "")
        rootView.studentIdTextView.text = getBoleta(activity)

        rootView.profilePictureWebView.loadUrl(getUrl(activity)+"Alumnos/info_alumnos/Fotografia.aspx")

        return rootView.root
    }

    class PerfilAdapter (fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
        override fun getItem(position: Int): Fragment {
            return when(position){
                0 -> PersonalBasicDataFragment()
                1 -> PersonalMedicalDataFragment()
                else -> Fragment()
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when(position){
                0 -> "Datos personales"
                1 -> "Datos médicos"
                else -> ""
            }
        }

        override fun getCount(): Int {
            return 2
        }

    }
}