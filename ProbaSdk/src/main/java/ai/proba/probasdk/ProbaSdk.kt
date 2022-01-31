package ai.proba.probasdk

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
 * Copyright (c) 2020 Proba
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
 * Entry point for the Proba Sdk for Android
 * */
public class ProbaSdk private constructor(
    private val applicationContext: Context,
    sdkToken: String,
    appId: String,
    deviceId: String,
    usingShake: Boolean,
    connectionTimeout: Long,
    showLogs: Boolean,
    private val defaults: Map<String, String>,
    appsFlyerId: String?,
    amplitudeId: String?,
    myTrackerId: String?,
    deviceProperties: Map<String, String>?,
    private val store: Store
) {

    private var mLastShakeTime: Long = -1L
    private val client: Client =
        Client(store, appId, deviceId, sdkToken, appsFlyerId, amplitudeId, myTrackerId, deviceProperties, connectionTimeout, showLogs)
    private val handler: ProbaHandler = ProbaHandler()

    init {
        Logger.LOG = showLogs || BuildConfig.DEBUG

        if (store.experimentsDefaults.isEmpty()) {
            store.experimentsDefaults = defaults.map { (k, v) ->
                Experiment(k, v)
            }
        }

        if (usingShake) {
            val sensorManager = applicationContext.getSystemService(SENSOR_SERVICE) as SensorManager
            val shakeDetector = ShakeDetector(object :
                ShakeDetector.Listener {
                override fun hearShake() {
                    if (!store.isInDebugMode) {
                        return
                    }
                    val shakeTime = SystemClock.elapsedRealtime()
                    if (mLastShakeTime != -1L && (shakeTime - mLastShakeTime) < 5_000) {
                        return
                    }
                    mLastShakeTime = shakeTime
                    ProbaDebugActivity.launch(applicationContext)
                }
            })
            shakeDetector.start(sensorManager)
        }
    }

    /**
     * Returns map of [Experiment] key, [Experiment] value applied to current device
     */
    @JvmOverloads
    public fun getExperiments(): Map<String, String> {
        return mapExperimentsOnDefaults()
                    .map {
                        it.key to it.value
                    }.toMap()

    }

    /**
     * Returns map of [Experiment]s applied to current device with fields details
     */
    @JvmOverloads
    public fun getExperimentsWithDetails(): Map<String, String> {
        return mapExperimentsOnDefaults()
                    .flatMap {
                        val valueKey = "[Proba] ${it.key}"
                        val valueValue = it.value
                        val optionKey = "[Proba] [internal] ${it.key}"
                        val optionValue = it.optionId.toString()
                        listOf(valueKey to valueValue, optionKey to optionValue)
                    }.toMap()
    }

    private fun mapExperimentsOnDefaults(): List<Experiment> {
        return if (store.isInDebugMode) {
            store.experimentsDefaults
                .map { experiment ->
                    val debugExperiment =
                        store.experimentsDebug.firstOrNull { debug -> experiment.key == debug.key }
                    Experiment(
                        experiment.key,
                        debugExperiment?.value ?: experiment.value,
                        experiment.optionId
                    )
                }
        } else {
            store.experimentsDefaults
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
     * Asynchronously fetches experiments from the Proba servers.
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
     * Launches ProbaDebugActivity with experiments debug information
     *
     * @param context Context to launch activity from
     *
     * @returns [Boolean] `false` if your Proba SDK configuration does not allow debug mode. `true` by default.
     * */
    public fun launchDebugMode(@NotNull context: Context): Boolean {
        if (store.isInDebugMode) {
            ProbaDebugActivity.launch(context)
            return true
        } else {
            return false
        }
    }

    /** Builder for a [ProbaSdk]. */
    public class Builder(private val context: Context) {
        private var sdkToken: String? = null
        private var appId: String? = null
        private var deviceId: String? = null
        private var usingShake: Boolean = true
        private var connectionTimeout: Long = 3000L
        private var showLogs: Boolean = false
        private var defaults: Map<String, String> = emptyMap()
        private var appsFlyerId: String? = null
        private var amplitudeId: String? = null
        private var myTrackerId: String? = null
        private var deviceProperties: Map<String, String>? = null

        private val store = Store.getInstance(context.applicationContext)

        /**
         * Sets Proba SDK Token.
         *
         * @param sdkToken to be applied.
         */
        fun sdkToken(@NotNull sdkToken: String) = apply { this.sdkToken = sdkToken }

        /**
         * Sets Proba AppId.
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
         * Turns the shake motion to show [ProbaDebugActivity] on or off.
         *
         * @param enable Should be `true` to enable, or `false` to disable this
         *     setting. `true` by default.
         */
        fun usingShake(@NotNull enable: Boolean = true) = apply { this.usingShake = enable }

        /**
         * Sets the timeout for fetch requests to the Proba servers in milliseconds.
         *
         * <p>A fetch call will fail if it takes longer than the specified timeout to fetch data
         * from the Proba servers. Previously fetched experiments values or experiments defaults will be applied.
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
         * Sets default experiments key/value map to fetch from the Proba servers and fallback in case of failed fetch.
         *
         * @param defaults Experiments key/value map.
         */
        fun defaults(@NotNull defaults: Map<String, String>) = apply { this.defaults = defaults }

        /**
         * Place here Appsflyer Id if you use it
         *
         *
         * */
        fun appsFlyerId(id: String) = apply { this.appsFlyerId = id }

        /**
         * Place here Amplitude User Id if you use it
         *
         *
         * */
        fun amplitudeUserId(id: String) = apply { this.amplitudeId = id }

        /**
         * Place here My Tracker Id if you use it
         *
         *
         * */
        fun myTrackerId(id: String) = apply { this.myTrackerId = id }

        /**
         * Place here additional information about device you want to track
         *
         *
         */
        fun deviceProperties(properties: Map<String, String>) = apply {this.deviceProperties = properties}


        /**
         * Returns a [ProbaSdk] instance.
         *
         * @throws ProbaSetupException
         */
        @UiThread
        fun build(): ProbaSdk {
            if (sdkToken.isNullOrEmpty()) {
                throw ProbaSetupException("SDK Token must not be null")
            }
            if (appId.isNullOrEmpty()) {
                throw ProbaSetupException("Proba App id must not be null")
            }
            if (connectionTimeout <= 0) {
                throw ProbaSetupException("Proba connectionTimeout can not be zero or negative")
            }
            if (deviceId.isNullOrEmpty()) {
                deviceId =
                    store.deviceId
                        ?: UUID.randomUUID().toString()
            }

            if (Looper.getMainLooper().thread != Thread.currentThread()) {
                throw ProbaSetupException("Proba SDK should been initialized in main thread")
            }

            store.deviceId = deviceId

            return ProbaSdk(
                context.applicationContext,
                sdkToken!!,
                appId!!,
                deviceId!!,
                usingShake,
                connectionTimeout,
                showLogs,
                defaults,
                appsFlyerId,
                amplitudeId,
                myTrackerId,
                deviceProperties,
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
