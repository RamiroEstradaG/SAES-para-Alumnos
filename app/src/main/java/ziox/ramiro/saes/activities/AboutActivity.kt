package ziox.ramiro.saes.activities

/**
 * Creado por Ramiro el 14/04/2019 a las 12:43 AM para SAESv2.
 */
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.Constants
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.activity_about.*
import ziox.ramiro.saes.R
import ziox.ramiro.saes.dialogs.FragmentDialogLicencias
import ziox.ramiro.saes.dialogs.FragmentDialogPoliticaSAES
import ziox.ramiro.saes.utils.initTheme
import ziox.ramiro.saes.utils.addBottomInsetPadding

class AboutActivity : AppCompatActivity(), BillingProcessor.IBillingHandler {
    private lateinit var bp: BillingProcessor
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var clickCount = 0
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        initTheme(this)
        aboutScroll.addBottomInsetPadding()

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        bp = BillingProcessor(
            this,
            resources.getString(R.string.billingKey),
            this
        )

        purchase20.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.localClassName}")
            bp.purchase(this, "01_saes_donation_20")
        }

        purchase50.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.localClassName}")
            bp.purchase(this, "02_saes_donation_50")
        }

        cardView4.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.localClassName}")
            clickCount++
            if (clickCount % 3 == 0 && clickCount > 0) {
                Toast.makeText(this, "Stan LOOΠΔ", Toast.LENGTH_SHORT).show()
            }
        }

        try {
            version_text.text = packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            Log.e("AppException", e.toString())
        }

        githubLink.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.localClassName}")
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/RamiroEda/SAES-para-Alumnos")
                )
            )
        }

        bugReport.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.localClassName}")
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/RamiroEda/SAES-para-Alumnos/issues/new?labels=bug&template=issue.md&title=%5BFECHA+EN+YY-MM-DD%5D%3A+%5BTITULO+DEL+ISSUE%5D")
                )
            )
        }

        featureRequest.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.localClassName}")
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/RamiroEda/SAES-para-Alumnos/issues/new?labels=feature&template=feature.md&title=%5BFECHA+EN+YY-MM-DD%5D%3A+%5BTITULO+DEL+ISSUE%5D")
                )
            )
        }

        link_2.setOnClickListener { v ->
            crashlytics.log("Click en ${resources.getResourceName(v.id)} en la clase ${this.localClassName}")
            val manager = ReviewManagerFactory.create(this)
            manager.requestReviewFlow().addOnCompleteListener { request ->
                if (request.isSuccessful) {
                    val reviewInfo = request.result
                    manager.launchReviewFlow(this, reviewInfo).addOnFailureListener {
                        Log.w("GooglePlayReview","In-app review request failed, reason=$it")
                    }.addOnCompleteListener { _ ->
                        Log.i("GooglePlayReview","In-app review finished")
                    }
                } else {
                    Log.w("GooglePlayReview","In-app review request failed, reason=${request.exception}")
                }
            }
        }

        link_3.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.localClassName}")
            FragmentDialogLicencias().show(supportFragmentManager, "licencia_dialog")
        }

        link_4.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.localClassName}")
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://ramiroeda.github.io/AppSAESv2/Politica")
                )
            )
        }

        link_6.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.localClassName}")
            FragmentDialogPoliticaSAES().show(supportFragmentManager, "politicaSAES_dialog")
        }

        link_5.setOnClickListener {
            crashlytics.log("Click en ${resources.getResourceName(it.id)} en la clase ${this.localClassName}")
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://trello.com/b/bYPns3O2")
                )
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBillingInitialized() {}

    override fun onPurchaseHistoryRestored() {}

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        Toast.makeText(this, "¡Gracias por apoyar este proyecto!", Toast.LENGTH_LONG).show()
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        if (errorCode != Constants.BILLING_RESPONSE_RESULT_USER_CANCELED) {
            Toast.makeText(this, "Error: $errorCode", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        bp.release()
        super.onDestroy()
    }
}