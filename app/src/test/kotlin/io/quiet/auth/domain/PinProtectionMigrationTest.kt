package io.quiet.auth.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinProtectionMigrationTest {

    @Test
    fun explicitOffOverridesHash() {
        assertFalse(PinProtectionMigration.isProtectionEnabled("0", true))
    }

    @Test
    fun explicitOnWithoutHash() {
        assertTrue(PinProtectionMigration.isProtectionEnabled("1", false))
    }

    @Test
    fun legacyNullFlagWithHashMeansEnabled() {
        assertTrue(PinProtectionMigration.isProtectionEnabled(null, true))
    }

    @Test
    fun legacyNullFlagWithoutHashMeansDisabled() {
        assertFalse(PinProtectionMigration.isProtectionEnabled(null, false))
    }

    @Test
    fun corruptExplicitMeansDisabled() {
        assertFalse(PinProtectionMigration.isProtectionEnabled("x", true))
    }
}
