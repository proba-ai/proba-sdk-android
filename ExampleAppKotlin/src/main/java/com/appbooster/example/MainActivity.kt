package com.appbooster.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.appbooster.appboostersdk.AppboosterSdk

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sdk = AppboosterSdk.Builder(this)
            .appId("657")
            .sdkToken("e3b0c44298fc1c149afbf4c8996fb924")
            .usingShake(true)
            .defaults(mapOf(
                "android_1" to "",
                "android_2" to ""
            ))
            .build()

        sdk.fetch(onSuccessListener = object: AppboosterSdk.OnSuccessListener{
            override fun onSuccess() {
                val def1_op = sdk["android_1"]
                Log.d("MainActivity", "def1_op: $def1_op")
                val def1_val = sdk.value("android_1")
                Log.d("MainActivity", "def1_val: $def1_val")
            }
        },
        onErrorListener = object: AppboosterSdk.OnErrorListener{
            override fun onError(throwable: Throwable) {
            }
        })
    }
}
