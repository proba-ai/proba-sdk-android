package com.appbooster.appboostersdk

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.SensorManager
import android.os.SystemClock
import android.util.Log
import java.util.*

/*
 * MIT License
 *
 * Copyright (c) 2020 Appbooster
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class AppboosterSdk private constructor(
    private val sdkToken: String,
    private val appId: String,
    private var deviceId: String,
    private var usingShake: Boolean,
    private val defaults: Map<String, Any>,
    private val store: Store,
    sensorManager: SensorManager,
    private val applicationContext: Context
) : ShakeDetector.Listener {

    private var mLastShakeTime: Long = -1L

    init {
        if (usingShake) {
            val shakeDetector = ShakeDetector(this)
            shakeDetector.start(sensorManager)
        }
    }

    private val client: Client = Client(store, appId, deviceId, sdkToken)

    fun fetch(timeoutMillis: Long = 30000L, onSuccessListener: OnSuccessListener, onErrorListener: OnErrorListener) {
        client.fetchExperimentsShort(timeoutMillis, defaults.keys, onSuccessListener, onErrorListener)
    }

    operator fun get(key: String): String? = value(key)
    fun value(key: String): String? = if (store.isInDebugMode) {
        store.experimentsDebugDefaults.firstOrNull { it.key == key }?.value ?: store.experimentsDefaults.firstOrNull { it.key == key }?.value
    } else {
        store.experimentsDefaults.firstOrNull { it.key == key }?.value
    }

    override fun hearShake() {
        Log.d("AppboosterSdk", "Shake heard")
        if (!store.isInDebugMode) {
            return
        }
        val shakeTime = SystemClock.elapsedRealtime()
        if (mLastShakeTime != -1L && (shakeTime - mLastShakeTime) < 5_000) {
            return
        }
        mLastShakeTime = shakeTime
        Log.d("AppboosterSdk", "Shake passed")
        AppboosterDebugActivity.launch(applicationContext)
    }

    class Builder(private val context: Context) {
        private var sdkToken: String? = null
        private var appId: String? = null
        private var deviceId: String? = null
        private var usingShake: Boolean = true
        private var defaults: Map<String, Any> = emptyMap()

        private val store = Store.getInstance(context.applicationContext)
        private val sensorManager = context.applicationContext.getSystemService(SENSOR_SERVICE) as SensorManager

        fun sdkToken(sdkToken: String) = apply { this.sdkToken = sdkToken }
        fun appId(appId: String) = apply { this.appId = appId }
        fun deviceId(deviceId: String) = apply { this.deviceId = deviceId }
        fun usingShake(usingShake: Boolean = true) = apply { this.usingShake = usingShake }
        fun defaults(defaults: Map<String, Any>) = apply { this.defaults = defaults }
        fun build(): AppboosterSdk {
            if (sdkToken.isNullOrEmpty()) {
                throw AppboosterSetupException("SDK Token must not be null")
            }
            if (appId.isNullOrEmpty()) {
                throw AppboosterSetupException("Appbooster App id must not be null")
            }
            if (deviceId.isNullOrEmpty()) {
                deviceId =
                    store.deviceId
                        ?: UUID.randomUUID().toString()
            }
            store.deviceId = deviceId

            return AppboosterSdk(sdkToken!!, appId!!, deviceId!!, usingShake, defaults, store, sensorManager, context.applicationContext)
        }
    }

    interface OnSuccessListener {
        fun onSuccess()
    }

    interface OnErrorListener {
        fun onError(throwable: Throwable)
    }
}
