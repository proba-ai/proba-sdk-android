package com.appbooster.example

import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.appbooster.appboostersdk.AppboosterDebugActivity
import com.appbooster.appboostersdk.AppboosterSdk
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sdk = AppboosterSdk.Builder(this)
            .appId("11314")
            .sdkToken("94EBA72B439D4A18B971231088C77D5F")
            .appsFlyerId("APPSFLYER_ID")
            .amplitudeUserId("AMPLITUDE_USER_ID")
            .defaults(
                mapOf(
                    "buttonColor" to "blue"
                )
            )
            .build()

        appboosterExampleButton.setOnClickListener {
            sdk.launchDebugMode(this)
        }

        if (sdk["buttonColor"] != null) {
            appboosterExampleButton.setBackgroundColor(Color.parseColor(sdk["buttonColor"]))
        }

        sdk.fetch(
            onSuccessListener = object : AppboosterSdk.OnSuccessListener {
                override fun onSuccess() {
                    appboosterExampleButton.setBackgroundColor(Color.parseColor(sdk["buttonColor"]))
                }
            },
            onErrorListener = object : AppboosterSdk.OnErrorListener {
                override fun onError(throwable: Throwable) {

                }
            })
    }
}
