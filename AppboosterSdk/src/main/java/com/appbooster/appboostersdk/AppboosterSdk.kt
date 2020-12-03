package com.appbooster.appboostersdk

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.SensorManager
import android.os.Looper
import android.os.SystemClock
import androidx.annotation.UiThread
import org.jetbrains.annotations.NotNull
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
    private val applicationContext: Context,
    sdkToken: String,
    appId: String,
    deviceId: String,
    usingShake: Boolean,
    connectionTimeout: Long,
    showLogs: Boolean,
    private val defaults: Map<String, String>,
    private val store: Store
) {

    private var mLastShakeTime: Long = -1L
    private val client: Client =
        Client(store, appId, deviceId, sdkToken, connectionTimeout, showLogs)
    private val handler: AppboosterHandler = AppboosterHandler()

    init {
        Logger.LOG = showLogs || BuildConfig.DEBUG

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

    /**
     * Returns list of [Experiment] applied to current device
     *
     * @param withPrefix to prepend `[Appbooster]` prefix to experiment keys returned. `true` by default.
     */
    @JvmOverloads
    public fun getExperiments(withPrefix: Boolean = true): Map<String, String> {
        return HashMap<String, String>()
            .apply {
                if (store.isInDebugMode) {
                    store.experimentsDefaults
                        .map { experiment ->
                            val debugExperiment =
                                store.experimentsDebug.firstOrNull { debug -> experiment.key == debug.key }
                            Experiment(experiment.key, debugExperiment?.value ?: experiment.value)
                        }
                } else {
                    store.experimentsDefaults
                }.forEach { experiment ->
                    put(
                        if (withPrefix) "[Appbooster] ${experiment.key}" else experiment.key,
                        experiment.value
                    )
                }
            }
    }

    /**
     * Returns the latest fetch experiments operation duration
     */
    public val lastOperationDurationMillis: Long
        get() {
            return client.lastOperationDurationMillis
        }

    /**
     * Asynchronously fetches experiments from the Appbooster servers.
     * */
    public fun fetch(
        @NotNull onSuccessListener: OnSuccessListener,
        @NotNull onErrorListener: OnErrorListener
    ) {
        client.fetchExperiments(defaults, handler, onSuccessListener, onErrorListener)
    }

    /**
     * Returns value of experiment specified by the given key as [String]
     *
     * @param key Experiment key
     *
     * @returns [String] representing the value of the experiment.
     * */
    @JvmName("getValue")
    public operator fun get(@NotNull key: String): String? = value(key)

    private fun value(@NotNull key: String): String? = if (store.isInDebugMode) {
        store.experimentsDebug.firstOrNull { it.key == key }?.value
            ?: store.experimentsDefaults.firstOrNull { it.key == key }?.value
    } else {
        store.experimentsDefaults.firstOrNull { it.key == key }?.value
    }


    /**
     * Launches AppboosterDebugActivity with experiments debug information
     *
     * @param context Context to launch activity from
     *
     * @returns [Boolean] `false` if your Appbooster SDK configuration does not allow debug mode. `true` by default.
     * */
    public fun launchDebugMode(@NotNull context: Context): Boolean {
        if (store.isInDebugMode) {
            AppboosterDebugActivity.launch(context)
            return true
        } else {
            return false
        }
    }

    /** Builder for a [AppboosterSdk]. */
    public class Builder(private val context: Context) {
        private var sdkToken: String? = null
        private var appId: String? = null
        private var deviceId: String? = null
        private var usingShake: Boolean = true
        private var connectionTimeout: Long = 3000L
        private var showLogs: Boolean = false
        private var defaults: Map<String, String> = emptyMap()

        private val store = Store.getInstance(context.applicationContext)

        /**
         * Sets Appbooster SDK Token.
         *
         * @param sdkToken to be applied.
         */
        fun sdkToken(@NotNull sdkToken: String) = apply { this.sdkToken = sdkToken }

        /**
         * Sets Appbooster AppId.
         *
         * @param appId to be applied.
         */
        fun appId(@NotNull appId: String) = apply { this.appId = appId }

        /**
         * Sets unique deviceId.
         *
         * @param deviceId to be applied. [UUID] generated by default.
         */
        fun deviceId(@NotNull deviceId: String) = apply { this.deviceId = deviceId }

        /**
         * Turns the shake motion to show [AppboosterDebugActivity] on or off.
         *
         * @param enable Should be `true` to enable, or `false` to disable this
         *     setting. `true` by default.
         */
        fun usingShake(@NotNull enable: Boolean = true) = apply { this.usingShake = enable }

        /**
         * Sets the timeout for fetch requests to the Appbooster servers in milliseconds.
         *
         * <p>A fetch call will fail if it takes longer than the specified timeout to fetch data
         * from the Appbooster servers. Previously fetched experiments values or experiments defaults will be applied.
         *
         * @param duration Timeout duration in millseconds.
         */
        fun fetchTimeout(@NotNull duration: Long = 3000L) =
            apply { this.connectionTimeout = duration }

        /**
         * Switch internal dev logs on and off.
         *
         * @param enable Should be `true` to enable, or `false` to disable this
         *     setting. `false` by default.
         */
        fun showLogs(@NotNull enable: Boolean = false) = apply { this.showLogs = enable }

        /**
         * Sets default experiments key/value map to fetch from the Appbooster servers and fallback in case of failed fetch.
         *
         * @param defaults Experiments key/value map.
         */
        fun defaults(@NotNull defaults: Map<String, String>) = apply { this.defaults = defaults }

        /**
         * Returns a [AppboosterSdk] instance.
         *
         * @throws AppboosterSetupException
         */
        @UiThread
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

            if (Looper.getMainLooper().thread != Thread.currentThread()) {
                throw AppboosterSetupException("Appbooster SDK should been initialized in main thread")
            }

            store.deviceId = deviceId

            return AppboosterSdk(
                context.applicationContext,
                sdkToken!!,
                appId!!,
                deviceId!!,
                usingShake,
                connectionTimeout,
                showLogs,
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
