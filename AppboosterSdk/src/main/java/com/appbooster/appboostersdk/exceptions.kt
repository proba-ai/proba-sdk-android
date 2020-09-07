package com.appbooster.appboostersdk

import java.lang.IllegalArgumentException

/**
 * Created at 11.08.2020 16:00
 * @author Alexey_Ivanov
 */
class AppboosterSetupException : IllegalArgumentException{
    constructor(message: String): super(message)
    constructor(message: String, cause: Throwable): super(message, cause)
}

class AppboosterFetchException : IllegalArgumentException {
    constructor(code: Int, message: String) : super("Fetch failed with code: '$code' and message: '$message'")
    constructor(cause: Throwable) : super("Fetch failed", cause)
}
