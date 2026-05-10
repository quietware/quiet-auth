package dev.calmauth.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TwoFAContractTest {
    @Test fun `normalizes user input for token persistence`() {
        val normalized = normalizeTwoFAInput(
            AddTwoFAInput(
                name = " GitHub ",
                account = " dominik ",
                secret = "ab cd ef",
                digits = 7,
                period = -1,
            )
        )
        assertEquals(
            NormalizedTwoFA(
                name = "GitHub",
                account = "dominik",
                secret = "ABCDEF",
                digits = 6,
                period = 30,
                algorithm = OtpAlgorithm.SHA1,
            ),
            normalized,
        )
    }

    @Test fun `keeps pin hash deterministic and 4-digit constraint`() {
        assertTrue(isPinFormatValid("1234"))
        assertFalse(isPinFormatValid("12345"))
        assertEquals(hashPin("1234"), hashPin("1234"))
    }
}
