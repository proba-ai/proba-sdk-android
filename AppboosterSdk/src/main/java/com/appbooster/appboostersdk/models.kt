package com.appbooster.appboostersdk

import com.squareup.moshi.JsonClass

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

@JsonClass(generateAdapter = true)
data class Experiment (
    var key: String = "",
    var value: String = "",
    var optionId: Long = 0
)

@JsonClass(generateAdapter = true)
internal data class ExperimentResponse(
    var experiments: List<Experiment> = emptyList(),
    var meta: Meta = Meta.DEFAULT
)

@JsonClass(generateAdapter = true)
internal data class CompositeExperimentResponse (
    var experiments: List<CompositeExperiment> = emptyList()
)

@JsonClass(generateAdapter = true)
internal data class CompositeExperiment (
    var name: String = "",
    var key: String = "",
    var status: ExperimentStatus = ExperimentStatus.UNKNOWN,
    var options: List<ExperimentOption> = emptyList()
)

@JsonClass(generateAdapter = true)
internal data class ExperimentOption (
    var value: String = "",
    var description: String = ""
)

@JsonClass(generateAdapter = true)
internal data class Error (
    val error: String = ""
)

internal enum class ExperimentStatus {
    PAUSED, RUNNING, FINISHED, UNKNOWN;
}

@JsonClass(generateAdapter = true)
internal class Meta {
    var debug: Boolean = false

    companion object {
        val DEFAULT = Meta().apply { debug = false }
    }
}
