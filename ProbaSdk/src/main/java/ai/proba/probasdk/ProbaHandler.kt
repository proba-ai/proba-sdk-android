package ai.proba.probasdk

import android.os.Handler
import android.os.Looper
import android.os.Message


internal class ProbaHandler : Handler(Looper.getMainLooper()) {

    override fun handleMessage(msg: Message) {
        val result = msg.obj as Result?
        when (msg.what) {
            ON_SUCCESS -> {
                result?.onSuccessListener?.onSuccess()
            }
            ON_ERROR -> {
                result?.onErrorListener?.onError(result.th!!)
            }
        }
    }

    internal fun sendSuccess(listener: ProbaSdk.OnSuccessListener) {
        obtainMessage(ON_SUCCESS, Result(listener, null, null)).sendToTarget();
    }

    internal fun sendError(listener: ProbaSdk.OnErrorListener, th: Throwable) {
        obtainMessage(ON_ERROR, Result(null, listener, th)).sendToTarget();
    }

    class Result(
        internal val onSuccessListener: ProbaSdk.OnSuccessListener?,
        internal val onErrorListener: ProbaSdk.OnErrorListener?,
        internal val th: Throwable?
    )

    companion object {
        internal val ON_SUCCESS = 0
        internal val ON_ERROR = 1
    }
}