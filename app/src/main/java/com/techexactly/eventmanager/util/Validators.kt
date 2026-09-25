package com.techexactly.eventmanager.util

/** Pure input validators. Each returns an error message, or null when the input is valid. */
object Validators {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    const val MIN_PASSWORD_LENGTH = 6

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !EMAIL_REGEX.matches(email.trim()) -> "Enter a valid email address"
        else -> null
    }

    fun validatePassword(password: String): String? = when {
        password.isEmpty() -> "Password is required"
        password.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
        else -> null
    }

    fun validateConfirmPassword(password: String, confirm: String): String? = when {
        confirm.isEmpty() -> "Please confirm your password"
        password != confirm -> "Passwords do not match"
        else -> null
    }

    fun validateTitle(title: String): String? =
        if (title.isBlank()) "Title is required" else null

    /**
     * @param selectedMillis chosen date & time (null if not chosen yet)
     * @param nowMillis current time
     * @param originalMillis when editing, the event's existing time. Keeping an unchanged
     *        (already past) date is allowed so users can still edit other fields.
     */
    fun validateEventDate(selectedMillis: Long?, nowMillis: Long, originalMillis: Long? = null): String? = when {
        selectedMillis == null -> "Please select date & time"
        selectedMillis < nowMillis && selectedMillis != originalMillis -> "Date & time cannot be in the past"
        else -> null
    }
}
