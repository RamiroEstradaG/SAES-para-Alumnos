package ziox.ramiro.saes.activities

/**
 * Creado por Ramiro el 14/04/2019 a las 12:43 AM para SAESv2.
 */
import android.R.id.message
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.Constants
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ziox.ramiro.saes.R
import ziox.ramiro.saes.databinding.ActivityAboutBinding
import ziox.ramiro.saes.dialogs.OpenSourceLicensesDialogFragment
import ziox.ramiro.saes.dialogs.SAESPrivacyPolicyDialogFragment
import ziox.ramiro.saes.utils.addBottomInsetPadding
import ziox.ramiro.saes.utils.initTheme


class AboutActivity : AppCompatActivity(), BillingProcessor.IBillingHandler, View.OnClickListener {
    private lateinit var billingProcessor: BillingProcessor
    private var clickCount = 0
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private lateinit var binding : ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initTheme(this)
        binding.aboutScroll.addBottomInsetPadding()

        billingProcessor = BillingProcessor(
            this,
            resources.getString(R.string.billingKey),
            this
        )

        binding.versionNameTextView.text = packageManager.getPackageInfo(packageName, 0).versionName

        binding.githubLinkButton.setOnClickListener(this)
        binding.purchase20Button.setOnClickListener(this)
        binding.purchase50Button.setOnClickListener(this)
        binding.appInfoCard.setOnClickListener(this)
        binding.bugReportButton.setOnClickListener(this)
        binding.featureRequestButton.setOnClickListener(this)
        binding.contactButton.setOnClickListener(this)
        binding.requestPlayReviewButton.setOnClickListener(this)
        binding.openSourceLicensesButton.setOnClickListener(this)
        binding.privacyPolicyButton.setOnClickListener(this)
        binding.saesPrivacyPolicyButton.setOnClickListener(this)
        binding.todoListButton.setOnClickListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
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
        billingProcessor.release()
        super.onDestroy()
    }

    override fun onClick(view: View) {
        crashlytics.log("Click en ${resources.getResourceName(view.id)} en la clase ${this.localClassName}")
        when(view.id){
            R.id.githubLinkButton -> {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/RamiroEstradaG/SAES-para-Alumnos")
                    )
                )
            }
            R.id.purchase20Button -> {
                billingProcessor.purchase(this, "01_saes_donation_20")
            }
            R.id.purchase50Button -> {
                billingProcessor.purchase(this, "02_saes_donation_50")
            }
            R.id.appInfoCard -> {
                clickCount++
                if (clickCount % 5 == 0 && clickCount > 0) {
                    Toast.makeText(this, "Stan LOOΠΔ", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.bugReportButton -> {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/RamiroEda/SAES-para-Alumnos/issues/new?labels=bug&template=issue.md&title=%5BFECHA+EN+YY-MM-DD%5D%3A+%5BTITULO+DEL+ISSUE%5D")
                    )
                )
            }
            R.id.contactButton -> {
                val email = Intent(Intent.ACTION_SEND)
                email.putExtra(Intent.EXTRA_EMAIL, arrayOf("ramiroestradag@gmail.com"))
                email.putExtra(Intent.EXTRA_SUBJECT, "SAES para Alumnos")
                email.putExtra(Intent.EXTRA_TEXT, message)

                email.type = "message/rfc822"

                startActivity(email)
            }
            R.id.featureRequestButton -> {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/RamiroEda/SAES-para-Alumnos/issues/new?labels=feature&template=feature.md&title=%5BFECHA+EN+YY-MM-DD%5D%3A+%5BTITULO+DEL+ISSUE%5D")
                    )
                )
            }
            R.id.requestPlayReviewButton -> {
                val manager = ReviewManagerFactory.create(this)
                manager.requestReviewFlow().addOnCompleteListener { request ->
                    if (request.isSuccessful) {
                        val reviewInfo = request.result
                        manager.launchReviewFlow(this, reviewInfo).addOnFailureListener {
                            Log.w("GooglePlayReview", "In-app review request failed, reason=$it")
                        }.addOnCompleteListener {
                            Log.i("GooglePlayReview", "In-app review finished")
                        }
                    } else {
                        Log.w(
                            "GooglePlayReview",
                            "In-app review request failed, reason=${request.exception}"
                        )
                    }
                }
            }
            R.id.openSourceLicensesButton -> {
                OpenSourceLicensesDialogFragment().show(supportFragmentManager, "licencia_dialog")
            }

            R.id.privacyPolicyButton -> {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://ramiroeda.github.io/AppSAESv2/Politica")
                    )
                )
            }

            R.id.saesPrivacyPolicyButton -> {
                SAESPrivacyPolicyDialogFragment().show(supportFragmentManager, "politicaSAES_dialog")
            }

            R.id.todoListButton -> {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://trello.com/b/bYPns3O2")
                    )
                )
            }
        }
    }
}