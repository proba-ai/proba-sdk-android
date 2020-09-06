package com.appbooster.appboostersdk

import com.squareup.moshi.*
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

internal object JsonAdapters {
    private val parser: Moshi = Moshi.Builder()
        .add(ExperimentStatusAdapter)
        .build()

    internal fun <T> adapterFor(type: Class<T>) = parser.adapter<T>(type)
    internal fun adapterForExperimentsDefaults(): JsonAdapter<List<Experiment>> {
        val type = Types.newParameterizedType(List::class.java, Experiment::class.java)
        return parser.adapter(type)
    }

    internal fun adapterForExperiments(): JsonAdapter<List<CompositeExperiment>> {
        val type = Types.newParameterizedType(List::class.java, CompositeExperiment::class.java)
        return parser.adapter(type)
    }
}

internal object ExperimentStatusAdapter {
    @ToJson
    internal fun toJson(status: ExperimentStatus): String {
        return status.name.toLowerCase(Locale.ROOT)
    }

    @FromJson
    internal fun fromJson(status: String): ExperimentStatus {
        return when (status) {
            "paused" -> ExperimentStatus.PAUSED
            "running" -> ExperimentStatus.RUNNING
            "finished" -> ExperimentStatus.FINISHED
            else -> ExperimentStatus.UNKNOWN
        }
    }
}
