package com.appbooster.appboostersdk

import android.util.Log
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

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

internal class Client(
    private val prefs: Store,
    appId: String,
    deviceId: String,
    token: String
) {

    private val okHttpClientBuilder = OkHttpClient.Builder()
        .connectTimeout(3000, TimeUnit.MILLISECONDS)
        .addInterceptor(HttpLoggingInterceptor()
            .apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
        )
    private val requestBuilder = RequestBuilder(appId, deviceId, token)
    private val jsonAdapters = JsonAdapters

    internal fun fetchExperimentsShort(
        timeoutMillis: Long,
        defaultKeys: Set<String>,
        onSuccessListener: AppboosterSdk.OnSuccessListener,
        onErrorListener: AppboosterSdk.OnErrorListener
    ) {
        val okHttpClient = okHttpClientBuilder.connectTimeout(timeoutMillis, TimeUnit.MILLISECONDS).build()
        val request = requestBuilder.request(makeQueryString(defaultKeys), Api.EXPERIMENTS_PATH)

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Client", "onFailure: ", e)
                onErrorListener.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    val error = jsonAdapters.adapterFor(Error::class.java).fromJson(response.body?.string() ?: "")
                    Log.e("AppboosterSdk", "New Failure: $error, ${response.code}")
                    onErrorListener.onError(AppboosterFetchException(response.code, error?.error ?: ""))
                    return
                }
                val experiments = jsonAdapters.adapterFor(ExperimentResponse::class.java).fromJson(response.body?.string() ?: "")

                prefs.experimentsDefaults = experiments?.experiments ?: emptyList()
                prefs.isInDebugMode = experiments?.meta?.debug ?: false
                if (experiments?.meta?.debug == true) {
                    fetchExperimentsFull(timeoutMillis, defaultKeys, onSuccessListener, onErrorListener)
                } else {
                    onSuccessListener.onSuccess()
                }
            }
        })
    }

    private fun fetchExperimentsFull(
        timeoutMillis: Long,
        defaultKeys: Set<String>,
        onSuccessListener: AppboosterSdk.OnSuccessListener,
        onErrorListener: AppboosterSdk.OnErrorListener
    ) {
        val okHttpClient = okHttpClientBuilder.connectTimeout(timeoutMillis, TimeUnit.MILLISECONDS).build()
        val request = requestBuilder.request(
            makeQueryString(defaultKeys),
            Api.EXPERIMENTS_PATH,
            Api.OPTIONS_PATH
        )

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("AppboosterSdk", "New Failure: ", e)
                onErrorListener.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    val error = jsonAdapters.adapterFor(Error::class.java).fromJson(response.body?.string() ?: "")
                    Log.e("AppboosterSdk", "New Failure: $error, ${response.code}")
                    onErrorListener.onError(AppboosterFetchException(response.code, error?.error ?: ""))
                    return
                }
                val experimentsResponse = jsonAdapters.adapterFor(CompositeExperimentResponse::class.java).fromJson(response.body?.string() ?: "")
                val finishedExperiments = experimentsResponse?.experiments
                    ?.filter { it.status == ExperimentStatus.FINISHED }
                    ?.map { experiment ->
                        val default = prefs.experimentsDefaults.first { defaults -> defaults.key == experiment.key }
                        val defaultOption = experiment.options.first { option -> option.value == default.value }
                        val finishedExperiment = CompositeExperiment(
                            name = experiment.name,
                            key = experiment.key,
                            status = ExperimentStatus.FINISHED,
                            options = listOf(defaultOption)
                        )
                        finishedExperiment
                    }
                    ?: emptyList()
                val otherExperiments = experimentsResponse?.experiments
                    ?.filter { it.status != ExperimentStatus.FINISHED }
                    ?: emptyList()
                prefs.experiments = finishedExperiments + otherExperiments
                onSuccessListener.onSuccess()
            }
        })
    }

    private fun makeQueryString(defaultKeys: Set<String>): String = defaultKeys.joinToString("&") { "knownKeys[]=$it" }
}

internal interface Api {

    companion object {
        internal const val STAGING_HOST = "https://staging.appbooster.com"
        internal const val PRODUCTION_HOST = "https://api.appbooster.com"
        internal const val MOBILE_API_PATH = "api/mobile"
        internal const val EXPERIMENTS_PATH = "experiments"
        internal const val OPTIONS_PATH = "options"
    }
}

internal class RequestBuilder(
    private val appId: String,
    private val deviceId: String,
    private val token: String
) {
    internal fun request(query: String, vararg paths: String) = Request.Builder()
        .addHeader(contentTypeHeader.first, contentTypeHeader.second)
        .addHeader(sdkAppIdHeader.first, sdkAppIdHeader.second)
        .addHeader(authorizationHeader.first, authorizationHeader.second)
        .addHeader(appVersionHeader.first, appVersionHeader.second)
        .url("${Api.STAGING_HOST}/${Api.MOBILE_API_PATH}/${paths.joinToString("/") { it }}?$query")
        .build()

    private val contentTypeHeader = "Content-Type" to "application/json"
    private val sdkAppIdHeader = "SDK-App-ID" to appId
    private val authorizationHeader = "Authorization" to "Bearer ${makeAccessToken()}"
    private val appVersionHeader = "AppVersion" to BuildConfig.VERSION_CODE.toString()

    private fun makeAccessToken(): String {
        return Jwts.builder()
            .setHeaderParam("alg", "HS256")
            .setHeaderParam("typ", "JWT")
            .claim("deviceId", deviceId)
            .signWith(Keys.hmacShaKeyFor(token.toByteArray()), SignatureAlgorithm.HS256)
            .compact()
    }
}

