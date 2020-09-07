package com.appbooster.appboostersdk

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.SensorManager
import android.os.SystemClock
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

/**
 * Entry point for the Appbooster Sdk for Android
 * */
public class AppboosterSdk private constructor(
    applicationContext: Context,
    sdkToken: String,
    appId: String,
    deviceId: String,
    usingShake: Boolean,
    connectionTimeout: Long,
    private val isInDevMode: Boolean,
    private val defaults: Map<String, String>,
    private val store: Store
) {

    private var mLastShakeTime: Long = -1L
    private val client: Client =
        Client(store, appId, deviceId, sdkToken, connectionTimeout)

    init {
        Logger.LOG = isInDevMode || BuildConfig.DEBUG

        if (store.experimentsDefaults.isEmpty()) {
            store.experimentsDefaults = defaults.map { (k, v) ->
                Experiment(k, v)
            }
        }

        if (usingShake) {
            val sensorManager = applicationContext.getSystemService(SENSOR_SERVICE) as SensorManager
            val shakeDetector = ShakeDetector(object : ShakeDetector.Listener {
                override fun hearShake() {
                    if (!store.isInDebugMode) {
                        return
                    }
                    val shakeTime = SystemClock.elapsedRealtime()
                    if (mLastShakeTime != -1L && (shakeTime - mLastShakeTime) < 5_000) {
                        return
                    }
                    mLastShakeTime = shakeTime
                    AppboosterDebugActivity.launch(applicationContext)
                }
            })
            shakeDetector.start(sensorManager)
        }
    }

    /** Returns list of {@link Experiment} applied to current device
     */
    val experiments: List<Experiment>
        get() {
            return if (store.isInDebugMode && isInDevMode) {
                store.experimentsDefaults
                    .map { experiment ->
                        val debugExperiment =
                            store.experimentsDebug.firstOrNull { debug -> experiment.key == debug.value }
                        Experiment(experiment.key, debugExperiment?.value ?: experiment.value)
                    }
            } else {
                store.experimentsDefaults
            }
        }

    /** Returns the latest fetch experiments operation duration
     */
    val lastOperationDurationMillis: Long
        get() {
            return client.lastOperationDurationMillis
        }

    /**
     * Asynchronously fetches experiments from the Appbooster servers.
     * */
    fun fetch(onSuccessListener: OnSuccessListener, onErrorListener: OnErrorListener) {
        client.fetchExperiments(defaults, onSuccessListener, onErrorListener)
    }

    /**
     * Returns value of experiment specified by the given key as {@link String}
     *
     * @param key: Experiment key
     *
     * @returns {@link String} representing the value of the experiment.
     * */
    @JvmName("getValue")
    operator fun get(key: String): String? = value(key)

    private fun value(key: String): String? = if (store.isInDebugMode && isInDevMode) {
        store.experimentsDebug.firstOrNull { it.key == key }?.value
            ?: store.experimentsDefaults.firstOrNull { it.key == key }?.value
    } else {
        store.experimentsDefaults.firstOrNull { it.key == key }?.value
    }


    /** Builder for a {@link AppboosterSdk}. */
    public class Builder(private val context: Context) {
        private var sdkToken: String? = null
        private var appId: String? = null
        private var deviceId: String? = null
        private var usingShake: Boolean = true
        private var connectionTimeout: Long = 3000L
        private var isInDevMode: Boolean = true
        private var defaults: Map<String, String> = emptyMap()

        private val store = Store.getInstance(context.applicationContext)

        /**
         * Sets Appbooster SDK Token.
         *
         * @param sdkToken to be applied.
         */
        fun sdkToken(sdkToken: String) = apply { this.sdkToken = sdkToken }

        /**
         * Sets Appbooster AppId.
         *
         * @param appId to be applied.
         */
        fun appId(appId: String) = apply { this.appId = appId }

        /**
         * Sets unique deviceId.
         *
         * @param deviceId to be applied. UUID generated by default.
         */
        fun deviceId(deviceId: String) = apply { this.deviceId = deviceId }

        /**
         * Turns the shake motion to show ${@link AppboosterDebugActivity} on or off.
         *
         * @param enable Should be <code>true</code> to enable, or <code>false</code> to disable this
         *     setting. <code>true</code> by default.
         */
        fun usingShake(enable: Boolean = true) = apply { this.usingShake = enable }

        /**
         * Sets the timeout for fetch requests to the Appbooster servers in milliseconds.
         *
         * <p>A fetch call will fail if it takes longer than the specified timeout to fetch data
         * from the Appbooster servers. Previously fetched experiments values or experiments defaults will be applied.
         *
         * @param duration Timeout duration in millseconds.
         */
        fun fetchTimeout(duration: Long = 3000L) = apply { this.connectionTimeout = duration }

        /**
         * Turns the developer mode on or off.
         *
         * @param enable Should be <code>true</code> to enable, or <code>false</code> to disable this
         *     setting. <code>false</code> by default.
         */
        fun isInDevMode(enable: Boolean = false) = apply { this.isInDevMode = enable }

        /**
         * Sets default experiments key/value map to fetch from the Appbooster servers and fallback in case of failed fetch.
         *
         * @param defaults Experiments key/value map.
         */
        fun defaults(defaults: Map<String, String>) = apply { this.defaults = defaults }

        /**
         * Returns a {@link AppboosterSdk}.
         */
        fun build(): AppboosterSdk {
            if (sdkToken.isNullOrEmpty()) {
                throw AppboosterSetupException("SDK Token must not be null")
            }
            if (appId.isNullOrEmpty()) {
                throw AppboosterSetupException("Appbooster App id must not be null")
            }
            if (connectionTimeout <= 0) {
                throw AppboosterSetupException("Appbooster connectionTimeout can not be zero or negative")
            }
            if (deviceId.isNullOrEmpty()) {
                deviceId =
                    store.deviceId
                        ?: UUID.randomUUID().toString()
            }
            store.deviceId = deviceId

            return AppboosterSdk(
                context.applicationContext,
                sdkToken!!,
                appId!!,
                deviceId!!,
                usingShake,
                connectionTimeout,
                isInDevMode,
                defaults,
                store
            )
        }
    }

    interface OnSuccessListener {
        fun onSuccess()
    }

    interface OnErrorListener {
        fun onError(th: Throwable)
    }
}
