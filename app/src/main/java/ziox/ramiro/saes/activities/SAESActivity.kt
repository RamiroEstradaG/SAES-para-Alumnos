package ziox.ramiro.saes.activities

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Configuration
import android.database.sqlite.SQLiteConstraintException
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.metrics.AddTrace
import com.twitter.sdk.android.core.Twitter
import ziox.ramiro.saes.R
import ziox.ramiro.saes.databases.AppLocalDatabase
import ziox.ramiro.saes.databases.RecentActivity
import ziox.ramiro.saes.databases.RecentActivityDao
import ziox.ramiro.saes.databinding.ActivitySaesBinding
import ziox.ramiro.saes.dialogs.SAESMenuDrawerDialogFragment
import ziox.ramiro.saes.fragments.*
import ziox.ramiro.saes.utils.*
import ziox.ramiro.saes.widgets.AgendaEscolarWidget
import ziox.ramiro.saes.widgets.HorarioLargeWidget
import ziox.ramiro.saes.widgets.HorarioListWidget
import java.util.*
import kotlin.collections.ArrayList

/**
 * Creado por Ramiro el 10/13/2018 a las 1:22 PM para SAESv2.
 */
class SAESActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private lateinit var binding: ActivitySaesBinding

    companion object{
        const val INTENT_EXTRA_REDIRECT = "redirect"
    }

    private var selectedItemId = R.id.nav_kardex
    private var isCheckingSession = false
    private val fragmentHistory = ArrayList<Int>()
    private lateinit var sessionChecker: WebView
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var swipeHorizontalyFun : (direction: Boolean) -> Unit
    private lateinit var recentActivityDao: RecentActivityDao
    private var shortcutManager : ShortcutManager? = null

    @SuppressLint("ClickableViewAccessibility")
    @AddTrace(name = "onCreateSAES", enabled = true)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initTheme(this)
        setSupportActionBar(binding.bottomAppBar)
        binding.bottomAppBar.addBottomInsetPadding()
        Twitter.initialize(this)
        recentActivityDao = AppLocalDatabase.getInstance(this).recentActivityDao()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            shortcutManager = getSystemService(ShortcutManager::class.java)
        }

        sessionChecker = createWebView(this, object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isCheckingSession = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.loadUrl("javascript: JSI.setSessionStatus(document.getElementById(\"c_default_ctl00_leftcolumn_loginuser_logincaptcha_CaptchaImage\") == null)")
                isCheckingSession = false
            }
        }, null)

        binding.bottomAppBar.setOnTouchListener(object : OnSwipeTouchListener(this){
            override fun onSwipeTop() {
                try {
                    crashlytics.log("DragUp en MenuModalButton en la clase ${this@SAESActivity.localClassName}")
                    SAESMenuDrawerDialogFragment().show(supportFragmentManager, "menu_modal")
                } catch (e: Exception) {
                    crashlytics.recordException(e)
                    Log.e(this.javaClass.canonicalName, e.toString())
                }
            }

            override fun onSwipeLeft() {
                super.onSwipeLeft()
                if(::swipeHorizontalyFun.isInitialized){
                    swipeHorizontalyFun(false)
                }
            }

            override fun onSwipeRight() {
                super.onSwipeRight()
                if(::swipeHorizontalyFun.isInitialized){
                    swipeHorizontalyFun(true)
                }
            }
        })

        sessionChecker.addJavascriptInterface(CheckerInterface(), "JSI")

        val defaultFragment : () -> String = {
            var defaultFragment = resources.getResourceEntryName(R.id.nav_kardex)
            val preferenceFragment = getPreference(this, ValType.STRING, "seccion_inicio_v2") as String

            if (preferenceFragment != ""){
                defaultFragment = preferenceFragment
            }else{
                setPreference(this, "seccion_inicio_v2", resources.getResourceEntryName(R.id.nav_home))
            }

            defaultFragment
        }

        val fragmentId = getPreference(
            this.applicationContext,
            "seccion_inicio_v2",
            defaultFragment()
        )

        postNavigationItemSelected(
            when {
                intent.hasExtra(INTENT_EXTRA_REDIRECT) -> resources.getIdentifier(intent.getStringExtra(INTENT_EXTRA_REDIRECT), "id", packageName)
                fragmentHistory.isEmpty() -> resources.getIdentifier(fragmentId, "id", packageName)
                else -> fragmentHistory.last()
            }, false)

        getSharedPreferences(
            "preferences",
            Context.MODE_PRIVATE
        ).registerOnSharedPreferenceChangeListener(this)

        binding.bottomAppBar.setNavigationOnClickListener {
            try {
                crashlytics.log("Click en MenuModalButton en la clase ${this.localClassName}")
                SAESMenuDrawerDialogFragment().show(supportFragmentManager, "menu_modal")
            } catch (e: Exception) {
                crashlytics.recordException(e)
                Log.e(this.javaClass.canonicalName, e.toString())
            }
        }

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        registerEvent(
            "LOGIN",
            "activity",
            getPreference(this, "name_escuela", "ESCUELA INDEFINIDA"),
            FirebaseAnalytics.Event.LOGIN
        )
    }

    private fun initDragViewAnimation(){
        val animation = AnimationUtils.loadAnimation(this, R.anim.pulse)
        binding.dragHorizontalView.startAnimation(animation)
    }

    private fun registerEvent(itemId: String, itemName: String, contentType: String, event: String) {
        if(::firebaseAnalytics.isInitialized){
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName)
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            firebaseAnalytics.logEvent(event, bundle)
        }
    }

    fun getActivatedItem() = selectedItemId

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        postNavigationItemSelected(menuItem, false)
        return true
    }

    fun setOnDragHorizontaly(lambda: (direction: Boolean) -> Unit){
        showDragIcon()
        updateDragView()
        initDragViewAnimation()

        this.swipeHorizontalyFun = lambda
    }

    private fun updateDragView(){
        (binding.dragHorizontalView.layoutParams as CoordinatorLayout.LayoutParams).apply {
            gravity = when(binding.bottomAppBar.fabAlignmentMode){
                BottomAppBar.FAB_ALIGNMENT_MODE_END -> Gravity.CENTER
                BottomAppBar.FAB_ALIGNMENT_MODE_CENTER -> Gravity.CENTER_VERTICAL or Gravity.END
                else -> Gravity.CENTER
            }
        }
    }

    fun hideDragIcon(){
        binding.dragHorizontalView.clearAnimation()
        binding.dragHorizontalView.visibility = View.GONE
    }

    fun showDragIcon(){
        updateDragView()
        initDragViewAnimation()
        binding.dragHorizontalView.visibility = View.VISIBLE
    }

    fun postNavigationItemSelected(menuItem: MenuItem, isBackPressed: Boolean){
        try {
            registerEvent(
                menuItem.toString(),
                resources.getResourceName(menuItem.itemId),
                "menu",
                FirebaseAnalytics.Event.SELECT_CONTENT
            )
        } catch (e: Exception) {
            Log.e(this.javaClass.canonicalName, e.toString())
        }
        when (menuItem.itemId) {
            R.id.nav_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }
            R.id.nav_pref -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            else -> changeFragment(menuItem, isBackPressed)
        }
    }

    fun postNavigationItemSelected(menuItemId: Int, isBackPressed: Boolean){
        try {
            registerEvent(
                menuItemId.toString(),
                resources.getResourceName(menuItemId),
                "menu",
                FirebaseAnalytics.Event.SELECT_CONTENT
            )
        } catch (e: Exception) {
            Log.e(this.javaClass.canonicalName, e.toString())
        }
        when (menuItemId) {
            R.id.nav_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }
            R.id.nav_pref -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            else -> fragmentReplace(getFragmentById(menuItemId), menuItemId, isBackPressed)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "widget_nivel") {
            updateWidgets()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        crashlytics.log("Configuracion cambiada en ${this.localClassName}:\n$newConfig")
    }

    override fun onBackPressed() {
        if (fragmentHistory.size > 1) {
            do {
                fragmentHistory.removeAt(fragmentHistory.size - 1)
                postNavigationItemSelected(fragmentHistory.last(), true)
            }while (fragmentHistory.last() == -1)
        } else {
            super.onBackPressed()
        }
    }

    fun showEmptyText(text: String) {
        runOnUiThread {
            binding.emptyText.text = text
            binding.emptyText.visibility = View.VISIBLE
        }
    }

    fun hideEmptyText() {
        runOnUiThread {
            binding.emptyText.visibility = View.GONE
            binding.emptyText.text = ""
        }
    }

    private fun getFragmentById(id: Int) : Fragment{
        return when (id) {
            R.id.nav_home -> HomeFragment()
            R.id.nav_kardex -> KardexFragment()
            R.id.nav_horario -> ClassScheduleFragment()
            R.id.nav_calific -> GradesFragment()
            R.id.nav_reinsc -> ReEnrollmentAppointmentFragment()
            R.id.nav_personal_agenda -> UserCalendarFragment()
            else -> {
                if (isNetworkAvailable()){
                    when (id) {
                        R.id.nav_horarios_clase -> AllCareersScheduleFragment()
                        R.id.nav_eval_prof -> TeacherEvaluationListFragment()
                        R.id.nav_calendario_ets -> ETSCalendarFragment()
                        R.id.nav_ocupabilidad -> ScheduleOccupancyFragment()
                        R.id.nav_equivalencias -> CourseEquivalencesFragment()
                        R.id.profile_button -> ProfileFragment()
                        R.id.nav_ets -> ETSRegisterFragment()
                        R.id.nav_estado_general -> OverallStatusFragment()
                        else -> {
                            Fragment()
                        }
                    }
                }else{
                    OfflineFragment()
                }
            }
        }
    }

    private fun changeFragment(menuItem: MenuItem, isBackPressed: Boolean) {
        binding.mainProgress.visibility = View.GONE
        if(menuItem.itemId != -1){
            crashlytics.log("Se encuentra en ${resources.getResourceName(menuItem.itemId)}")
        }

        val fragment = getFragmentById(menuItem.itemId)

        try {
            if(menuItem.itemId != R.id.nav_home){
                this.recentActivityDao.insert(RecentActivity(
                    resources.getResourceName(menuItem.itemId),
                    menuItem.title.toString(),
                    menuItem.icon.toByteArray(),
                    Calendar.getInstance().timeInMillis
                ))
            }
        }catch (e : SQLiteConstraintException){
            this.recentActivityDao.update(
                resources.getResourceName(menuItem.itemId),
                Calendar.getInstance().timeInMillis,
                menuItem.icon.toByteArray(),
            )
        }finally { }

        fragmentReplace(fragment, menuItem.itemId, isBackPressed)
    }

    fun fragmentReplace(fragment: Fragment, id: Int, isBackPressed: Boolean = false){
        selectedItemId = id
        hideDragIcon()
        hideFab()
        hideEmptyText()
        setStatusBarByTheme(this)

        if (id == R.id.nav_horarios_clase && !isNetworkAvailable()){
            showFab(R.drawable.ic_add_schedule, {
                crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.localClassName}")
                startActivity(Intent(this, ScheduleGeneratorActivity::class.java))
            }, BottomAppBar.FAB_ALIGNMENT_MODE_CENTER)
        }

        if (!isBackPressed) {
            fragmentHistory.add(id)
        }

        checkSession()
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.fragment_in, R.anim.fragment_out)
            .replace(R.id.frameContent, fragment)
            .commitAllowingStateLoss()
    }

    fun getProgressBar(): ProgressBar? {
        return if (getPreference(this, "offline_mode", false)) {
            null
        } else {
            binding.mainProgress
        }
    }

    private fun checkSession() {
        if (this.isNetworkAvailable() && !isCheckingSession) {
            sessionChecker.loadUrl(getUrl(this) + "alumnos/default.aspx")
        }
    }

    override fun onResume() {
        super.onResume()
        checkSession()
    }

    fun showFab(icon: Int, listener: View.OnClickListener, alignMode: Int) {
        runOnUiThread {
            binding.floatingActionButton.show()
            binding.floatingActionButton.setImageResource(icon)
            binding.bottomAppBar.fabAlignmentMode = alignMode
            binding.floatingActionButton.setOnClickListener(listener)
            updateDragView()
        }
    }

    fun getMainLayout(): CoordinatorLayout {
        return binding.mainLayout
    }

    fun updateWidgets() {
        val widgetLarge = Intent(this, HorarioLargeWidget::class.java)
        widgetLarge.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val idsLarge = AppWidgetManager.getInstance(this)
            .getAppWidgetIds(ComponentName(this, HorarioLargeWidget::class.java))
        widgetLarge.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idsLarge)
        sendBroadcast(widgetLarge)

        val widgetList = Intent(this, HorarioListWidget::class.java)
        widgetList.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val idsList = AppWidgetManager.getInstance(this)
            .getAppWidgetIds(ComponentName(this, HorarioListWidget::class.java))
        widgetList.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idsList)
        sendBroadcast(widgetList)

        val widgetAgenda = Intent(this, AgendaEscolarWidget::class.java)
        widgetAgenda.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val idsAgenda = AppWidgetManager.getInstance(this)
            .getAppWidgetIds(ComponentName(this, AgendaEscolarWidget::class.java))
        widgetAgenda.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idsAgenda)
        sendBroadcast(widgetAgenda)
    }

    fun hideFab() {
        binding.floatingActionButton.hide()
    }

    fun showFab() {
        binding.floatingActionButton.show()
        updateDragView()
    }

    fun changeFabIcon(res : Int){
        binding.floatingActionButton.setImageResource(res)
    }

    fun supportShortcutPin() : Boolean{
        return if (shortcutManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            shortcutManager!!.isRequestPinShortcutSupported
        } else false
    }

    fun initShortcut(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(this, AddEventToAgendaActivity::class.java)
            val icon = Icon.createWithResource(this, R.drawable.ic_add_shortcut)
            intent.action = "random"

            if (shortcutManager?.isRequestPinShortcutSupported == true) {
                val pinShortcutInfo = ShortcutInfo.Builder(this, "add_user")
                    .setShortLabel("Evento")
                    .setLongLabel("Agregar evento al horario de trabajo")
                    .setIcon(icon)
                    .setIntent(intent)
                    .build()

                shortcutManager!!.requestPinShortcut(pinShortcutInfo, null)
            }
        }
    }

    inner class CheckerInterface {
        @JavascriptInterface
        fun setSessionStatus(isLogged: Boolean) {
            if (!isLogged) {
                runOnUiThread {
                    Toast.makeText(
                        this@SAESActivity,
                        getUrl(this@SAESActivity) + " ha cerrado tu sesión por inactividad",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(Intent(this@SAESActivity, MainActivity::class.java))
                    this@SAESActivity.finish()
                }
            }
        }
    }
}
