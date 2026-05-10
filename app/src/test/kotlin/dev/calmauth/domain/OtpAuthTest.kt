package dev.calmauth.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OtpAuthTest {
    @Test fun `parses issuer account and crypto parameters`() {
        val parsed = parseOtpAuthUri(
            "otpauth://totp/GitHub:dominik?secret=JBSWY3DPEHPK3PXP&issuer=GitHub&digits=6&period=30&algorithm=SHA256"
        )
        assertEquals(
            ParsedOtpAuth(
                name = "GitHub",
                account = "dominik",
                secret = "JBSWY3DPEHPK3PXP",
                digits = 6,
                period = 30,
                algorithm = OtpAlgorithm.SHA256,
            ),
            parsed
        )
    }

    @Test fun `falls back to defaults for invalid payload`() {
        assertNull(parseOtpAuthUri("https://example.com"))
        assertNull(parseOtpAuthUri("otpauth://totp/NoSecret"))
    }
}
