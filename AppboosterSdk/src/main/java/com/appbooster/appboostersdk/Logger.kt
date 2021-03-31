package com.appbooster.appboostersdk

import android.util.Log

internal object Logger {
    internal var LOG = BuildConfig.DEBUG

    internal fun i(tag: String?, string: String?) {
        if (LOG) Log.i(tag, string?:"")
    }

    internal fun e(tag: String?, string: String?) {
        if (LOG) Log.e(tag, string?:"")
    }

    internal fun e(tag: String?, string: String?, e: Throwable) {
        if (LOG) Log.e(tag, string, e)
    }

    internal fun d(tag: String?, string: String?) {
        if (LOG) Log.d(tag, string?:"")
    }

    internal fun v(tag: String?, string: String?) {
        if (LOG) Log.v(tag, string?:"")
    }

    internal fun w(tag: String?, string: String?) {
        if (LOG) Log.w(tag, string?:"")
    }
}