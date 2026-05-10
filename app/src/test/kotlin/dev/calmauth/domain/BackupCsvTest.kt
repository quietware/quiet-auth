package dev.calmauth.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupCsvTest {
    private val sample = TwoFAItem(
        id = "a1",
        name = "Test, Inc.",
        account = "user@example.com",
        secret = "JBSWY3DPEHPK3PXP",
        digits = 6,
        period = 30,
        algorithm = OtpAlgorithm.SHA1,
    )

    @Test fun `roundtrips items through CSV`() {
        val csv = twoFAItemsToCsv(listOf(sample))
        val parsed = parseBackupCsv(csv)
        assertEquals(1, parsed.size)
        assertEquals(sample.copy(secret = sample.secret.uppercase()), parsed[0])
    }

    @Test fun `parses quoted fields with commas`() {
        val csv = listOf(
            "id,name,account,secret,digits,period,algorithm",
            "x,\"Name, Ltd.\",acct,SECRETSECRETSECRET,6,30,SHA1",
        ).joinToString("\n")
        val parsed = parseBackupCsv(csv)
        assertEquals("Name, Ltd.", parsed[0].name)
        assertEquals("SECRETSECRETSECRET", parsed[0].secret)
    }

    @Test fun `throws on wrong header`() {
        val ex = assertThrows(BackupFormatException::class.java) {
            parseBackupCsv("wrong,a,b,c,d,e,f\n1,2,3,4,5,6,7")
        }
        assertEquals("BACKUP_INVALID_FORMAT", ex.message)
    }

    @Test fun `throws when no data rows`() {
        val ex = assertThrows(BackupFormatException::class.java) {
            parseBackupCsv("id,name,account,secret,digits,period,algorithm")
        }
        assertEquals("BACKUP_EMPTY", ex.message)
    }
}
