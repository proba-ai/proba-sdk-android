package ai.proba.example

import ai.proba.probasdk.ProbaSdk
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sdk = ProbaSdk.Builder(this)
            .appId("11314")
            .sdkToken("94EBA72B439D4A18B971231088C77D5F")
            .appsFlyerId("APPSFLYER_ID")
            .amplitudeUserId("AMPLITUDE_USER_ID")
            .defaults(
                mapOf(
                    "buttonColor" to "blue"
                )
            )
            .deviceProperties(mapOf("installedAt" to calculateInstalledAt()))
            .build()

        probaExampleButton.setOnClickListener {
            sdk.launchDebugMode(this)
        }

        if (sdk["buttonColor"] != null) {
            probaExampleButton.setBackgroundColor(Color.parseColor(sdk["buttonColor"]))
        }

        sdk.fetch(
            onSuccessListener = object : ProbaSdk.OnSuccessListener {
                override fun onSuccess() {
                    probaExampleButton.setBackgroundColor(Color.parseColor(sdk["buttonColor"]))
                }
            },
            onErrorListener = object : ProbaSdk.OnErrorListener {
                override fun onError(th: Throwable) {

                }
            })
    }

    private fun calculateInstalledAt(): String {
        val firstInstallationTimeInMillis = packageManager.getPackageInfo(packageName, 0).firstInstallTime
        val formatter =  SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
        val firstInstallationString = formatter.format(Date(firstInstallationTimeInMillis))
        Log.d("MainActivity", "firstInstallationString: $firstInstallationString")
        return firstInstallationString
    }
}
