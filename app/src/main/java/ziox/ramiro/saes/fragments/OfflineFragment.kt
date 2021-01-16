package ziox.ramiro.saes.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import ziox.ramiro.saes.databinding.FragmentOfflineBinding
import ziox.ramiro.saes.utils.dpToPixel
import ziox.ramiro.saes.utils.getWindowMetrics
import kotlin.random.Random

class OfflineFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentOfflineBinding.inflate(inflater, container, false).root
    }
}