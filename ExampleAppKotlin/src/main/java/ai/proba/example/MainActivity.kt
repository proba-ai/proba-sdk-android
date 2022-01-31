package ai.proba.example

import ai.proba.probasdk.ProbaSdk
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.my.tracker.MyTracker
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MyTracker.initTracker(MY_TRACKER_ID, application)

        val sdk = ProbaSdk.Builder(this)
            .appId("25732")
            .sdkToken("430BBA69FBBC434AA6C1529F1E160EAD")
            .appsFlyerId("APPSFLYER_ID")
            .amplitudeUserId("AMPLITUDE_USER_ID")
            .myTrackerId(MY_TRACKER_ID)
            .defaults(
                mapOf(
                    "buttonColor" to "blue"
                )
            )
            .showLogs(true)
            .deviceProperties(mapOf("installedAt" to calculateInstalledAt()))
            .build()

        probaExampleButton.setOnClickListener {
            MyTracker.trackEvent("on button click event <kotlin>")
            MyTracker.flush()
            //sdk.launchDebugMode(this)
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

    companion object {
        private const val MY_TRACKER_ID = "16733057808593240177"
    }
}
