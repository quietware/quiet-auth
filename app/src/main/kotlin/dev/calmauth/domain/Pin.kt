package dev.calmauth.domain

import java.security.MessageDigest

val PIN_REGEX = Regex("^\\d{4}$")

fun isPinFormatValid(pin: String): Boolean = PIN_REGEX.matches(pin)

/**
 * Hex-encoded SHA-256 of the PIN bytes (UTF-8). Identical to `domain/pin.ts#hashPin`.
 */
fun hashPin(pin: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
    return buildString(digest.size * 2) {
        for (b in digest) {
            val v = b.toInt() and 0xFF
            append(HEX_CHARS[v ushr 4])
            append(HEX_CHARS[v and 0x0F])
        }
    }
}

private val HEX_CHARS = "0123456789abcdef".toCharArray()
