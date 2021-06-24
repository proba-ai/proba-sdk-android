package ai.proba.probasdk

import android.content.Context
import android.content.SharedPreferences

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

internal class Store private constructor(private val prefs: SharedPreferences) {

    private val jsonAdapters = JsonAdapters

    internal var deviceId: String?
        get() {
            return prefs.getString(SHARED_PREFERENCES_DEVICE_ID_KEY, null)
        }
        set(value) {
            prefs.edit().putString(SHARED_PREFERENCES_DEVICE_ID_KEY, value).apply()
        }

    internal var experiments: List<CompositeExperiment>
        get () {
            val json = prefs.getString(SHARED_PREFERENCES_EXPERIMENTS_KEY, "[]")!!
            return jsonAdapters.adapterForExperiments().fromJson(json) ?: emptyList()
        }
        set(value) {
            val json = jsonAdapters.adapterForExperiments().toJson(value)
            prefs.edit().putString(SHARED_PREFERENCES_EXPERIMENTS_KEY, json).apply()
        }

    internal var experimentsDefaults: List<Experiment>
        get () {
            val json = prefs.getString(SHARED_PREFERENCES_EXPERIMENTS_DEFAULTS_KEY, "[]")!!
            return jsonAdapters.adapterForExperimentsDefaults().fromJson(json) ?: emptyList()
        }
        set(value) {
            val json = jsonAdapters.adapterForExperimentsDefaults().toJson(value)
            prefs.edit().putString(SHARED_PREFERENCES_EXPERIMENTS_DEFAULTS_KEY, json).apply()
        }

    internal var experimentsDebug: List<Experiment>
        get () {
            val json = prefs.getString(SHARED_PREFERENCES_EXPERIMENTS_DEBUG_DEFAULTS_KEY, "[]")!!
            return jsonAdapters.adapterForExperimentsDefaults().fromJson(json) ?: emptyList()
        }
        set(value) {
            val json = jsonAdapters.adapterForExperimentsDefaults().toJson(value)
            prefs.edit().putString(SHARED_PREFERENCES_EXPERIMENTS_DEBUG_DEFAULTS_KEY, json).apply()
        }

    internal var isInDebugMode: Boolean
        get() {
            return prefs.getBoolean(SHARED_PREFERENCES_EXPERIMENTS_DEBUG_MODE_KEY, false)
        }
        set(value) {
            prefs.edit().putBoolean(SHARED_PREFERENCES_EXPERIMENTS_DEBUG_MODE_KEY, value).apply()
        }

    companion object {

        private val SHARED_PREFERENCES_KEY = "com.proba.storage"
        private val SHARED_PREFERENCES_DEVICE_ID_KEY = "com.proba.storage.deviceId"

        private val SHARED_PREFERENCES_EXPERIMENTS_KEY = "com.proba.storage.experiments"
        private val SHARED_PREFERENCES_EXPERIMENTS_DEFAULTS_KEY = "com.proba.storage.experiments.defaults"
        private val SHARED_PREFERENCES_EXPERIMENTS_DEBUG_DEFAULTS_KEY = "com.proba.storage.experiments.debug.defaults"
        private val SHARED_PREFERENCES_EXPERIMENTS_DEBUG_MODE_KEY = "com.proba.storage.experiments.debug.mode"

        internal fun getInstance(context: Context): Store {
            val prefs = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
            return Store(prefs)
        }
    }
}
