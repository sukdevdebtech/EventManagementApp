package com.techexactly.eventmanager.util

/** Simple wrapper used by repositories to return success / error without throwing. */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}
