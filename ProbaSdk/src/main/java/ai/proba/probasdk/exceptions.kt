package ai.proba.probasdk

import java.lang.IllegalArgumentException

/**
 * Created at 11.08.2020 16:00
 * @author Alexey_Ivanov
 */
class ProbaSetupException : IllegalArgumentException{
    constructor(message: String): super(message)
    constructor(message: String, cause: Throwable): super(message, cause)
}

class ProbaFetchException : IllegalArgumentException {
    constructor(code: Int, message: String) : super("Fetch failed with code: '$code' and message: '$message'")
    constructor(cause: Throwable) : super("Fetch failed", cause)
}
