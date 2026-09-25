package com.techexactly.eventmanager.util

import kotlin.coroutines.cancellation.CancellationException

/** Runs [block] and converts any exception into [Resource.Error] (cancellation is re-thrown). */
suspend inline fun <T> safeCall(block: suspend () -> T): Resource<T> = try {
    Resource.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Resource.Error(FirebaseErrorMapper.map(e))
}
