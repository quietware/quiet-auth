package dev.calmauth.domain

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
private val INVALID_BASE32 = Regex("[^A-Z2-7]")
private val TRAILING_PADDING = Regex("=+$")

private fun decodeBase32(secret: String): ByteArray {
    val cleaned = secret.uppercase()
        .replace(TRAILING_PADDING, "")
        .replace(INVALID_BASE32, "")
    if (cleaned.isEmpty()) return ByteArray(0)

    val bytes = ArrayList<Byte>(cleaned.length)
    var bits = 0
    var value = 0
    for (char in cleaned) {
        val index = BASE32_ALPHABET.indexOf(char)
        if (index < 0) continue
        value = (value shl 5) or index
        bits += 5
        if (bits >= 8) {
            bytes.add(((value ushr (bits - 8)) and 0xFF).toByte())
            bits -= 8
        }
    }
    return bytes.toByteArray()
}

private fun toCounterBytes(counter: Long): ByteArray {
    val out = ByteArray(8)
    var temp = counter
    for (i in 7 downTo 0) {
        out[i] = (temp and 0xFF).toByte()
        temp = temp ushr 8
    }
    return out
}

private fun macAlgorithm(algorithm: OtpAlgorithm): String = when (algorithm) {
    OtpAlgorithm.SHA1 -> "HmacSHA1"
    OtpAlgorithm.SHA256 -> "HmacSHA256"
    OtpAlgorithm.SHA512 -> "HmacSHA512"
}

/**
 * Mirrors `domain/totp.ts#generateTimeBasedCode` from calmauth-expo, including the
 * "000 000" fallback for invalid secrets and the space-after-3-digits format for 6-digit
 * codes. Verified against the same test vectors.
 */
fun generateTimeBasedCode(
    secret: String,
    nowMs: Long = System.currentTimeMillis(),
    digits: Int = 6,
    period: Int = 30,
    algorithm: OtpAlgorithm = OtpAlgorithm.SHA1,
): String {
    val key = decodeBase32(secret.replace("\\s".toRegex(), "").uppercase())
    if (key.isEmpty()) return "000 000"

    val effectivePeriod = if (period > 0) period else 30
    val effectiveDigits = if (digits in 6..8) digits else 6

    val timeStep = nowMs / 1000 / effectivePeriod
    val mac = Mac.getInstance(macAlgorithm(algorithm)).apply {
        init(SecretKeySpec(key, macAlgorithm(algorithm)))
    }
    val digest = mac.doFinal(toCounterBytes(timeStep))

    val offset = digest[digest.size - 1].toInt() and 0x0F
    val binary = ((digest[offset].toInt() and 0x7F) shl 24) or
        ((digest[offset + 1].toInt() and 0xFF) shl 16) or
        ((digest[offset + 2].toInt() and 0xFF) shl 8) or
        (digest[offset + 3].toInt() and 0xFF)

    val modulus = pow10(effectiveDigits)
    val otp = (binary.toLong() and 0xFFFFFFFFL).rem(modulus).toString()
        .padStart(effectiveDigits, '0')
    return if (effectiveDigits == 6) "${otp.substring(0, 3)} ${otp.substring(3)}" else otp
}

private fun pow10(n: Int): Long {
    var result = 1L
    repeat(n) { result *= 10 }
    return result
}
