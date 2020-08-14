package com.appbooster.appboostersdk

import java.lang.IllegalArgumentException

/**
 * Created at 11.08.2020 16:00
 * @author Alexey_Ivanov
 */
class AppboosterSetupException (message: String) : IllegalArgumentException(message)
class AppboosterFetchException (code: Int, message: String) : IllegalArgumentException(message)
