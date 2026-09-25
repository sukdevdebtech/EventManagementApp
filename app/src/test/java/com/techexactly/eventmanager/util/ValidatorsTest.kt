package com.techexactly.eventmanager.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    @Test fun `title is required`() {
        assertNotNull(Validators.validateTitle("   "))
        assertNull(Validators.validateTitle("Birthday"))
    }

    @Test fun `date must be selected and not in the past`() {
        val now = 1_000_000L
        assertEquals("Please select date & time", Validators.validateEventDate(null, now))
        assertEquals("Date & time cannot be in the past", Validators.validateEventDate(now - 1, now))
        assertNull(Validators.validateEventDate(now + 1, now))
    }

    @Test fun `unchanged past date is allowed when editing`() {
        val now = 1_000_000L
        val original = now - 5_000
        assertNull(Validators.validateEventDate(original, now, originalMillis = original))
    }

    @Test fun `email validation`() {
        assertNotNull(Validators.validateEmail(""))
        assertNotNull(Validators.validateEmail("not-an-email"))
        assertNull(Validators.validateEmail("user@example.com"))
    }

    @Test fun `password rules`() {
        assertNotNull(Validators.validatePassword("12345"))
        assertNull(Validators.validatePassword("123456"))
        assertNotNull(Validators.validateConfirmPassword("abcdef", "abcdeg"))
        assertNull(Validators.validateConfirmPassword("abcdef", "abcdef"))
    }
}
