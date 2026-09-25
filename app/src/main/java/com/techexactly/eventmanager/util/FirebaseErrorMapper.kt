package com.techexactly.eventmanager.util

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code

/** Converts Firebase exceptions into short, user-friendly messages. */
object FirebaseErrorMapper {

    fun map(t: Throwable): String = when (t) {
        // NOTE: WeakPassword extends InvalidCredentials, so it must be checked first.
        is FirebaseAuthWeakPasswordException -> "Password is too weak. Use at least 6 characters."
        is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
        is FirebaseAuthInvalidUserException -> "No account found for this email."
        is FirebaseAuthUserCollisionException -> "An account with this email already exists."
        is FirebaseTooManyRequestsException -> "Too many attempts. Please try again later."
        is FirebaseNetworkException -> "No internet connection. Please check your network."
        is FirebaseFirestoreException -> when (t.code) {
            Code.PERMISSION_DENIED -> "You don't have permission to do that."
            Code.UNAVAILABLE -> "Service unavailable. Working offline."
            Code.NOT_FOUND -> "Event not found."
            Code.UNAUTHENTICATED -> "Session expired. Please log in again."
            else -> "Something went wrong (${t.code.name.lowercase()})."
        }
        else -> t.localizedMessage ?: "Unexpected error occurred."
    }
}
