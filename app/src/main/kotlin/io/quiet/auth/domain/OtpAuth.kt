package io.quiet.auth.domain

import java.net.URLDecoder

data class ParsedOtpAuth(
    val name: String,
    val account: String,
    val secret: String,
    val digits: Int? = null,
    val period: Int? = null,
    val algorithm: OtpAlgorithm? = null,
)

/**
 * Mirrors `domain/otpauth.ts#parseOtpAuthUri` from the reference TypeScript project. Returns null for non-otpauth
 * URIs or payloads missing a secret. Falls back to "Authenticator" / "User" when issuer/account
 * cannot be inferred from the label. Implemented without Android APIs so it can be exercised
 * by plain JVM unit tests.
 */
fun parseOtpAuthUri(uri: String): ParsedOtpAuth? {
    if (!uri.startsWith("otpauth://")) return null
    return try {
        val rest = uri.removePrefix("otpauth://")
        val queryStart = rest.indexOf('?')
        val pathPart = if (queryStart >= 0) rest.substring(0, queryStart) else rest
        val queryPart = if (queryStart >= 0) rest.substring(queryStart + 1) else ""

        val params = parseQuery(queryPart)
        val secret = params["secret"]?.takeIf { it.isNotEmpty() } ?: return null

        val digits = params["digits"]?.toIntOrNull() ?: 6
        val period = params["period"]?.toIntOrNull() ?: 30
        val algorithm = OtpAlgorithm.fromRaw(params["algorithm"])
        val issuerFromQuery = params["issuer"]?.takeIf { it.isNotBlank() }

        val labelRaw = pathPart.substringAfter('/', missingDelimiterValue = pathPart)
        val label = URLDecoder.decode(labelRaw, "UTF-8")
        val (issuerFromLabel, accountFromLabel) = if (label.contains(':')) {
            label.split(":", limit = 2).let { it[0] to it[1] }
        } else {
            "" to label
        }

        val issuer = issuerFromQuery
            ?: issuerFromLabel.takeIf { it.isNotBlank() }
            ?: "Authenticator"
        val account = accountFromLabel.takeIf { it.isNotBlank() } ?: "User"

        ParsedOtpAuth(
            name = issuer.trim(),
            account = account.trim(),
            secret = secret.trim(),
            digits = digits,
            period = period,
            algorithm = algorithm,
        )
    } catch (_: Exception) {
        null
    }
}

private fun parseQuery(query: String): Map<String, String> {
    if (query.isEmpty()) return emptyMap()
    val out = LinkedHashMap<String, String>()
    for (pair in query.split('&')) {
        if (pair.isEmpty()) continue
        val eq = pair.indexOf('=')
        val (rawKey, rawValue) = if (eq < 0) pair to "" else pair.substring(0, eq) to pair.substring(eq + 1)
        val key = URLDecoder.decode(rawKey, "UTF-8")
        val value = URLDecoder.decode(rawValue, "UTF-8")
        out[key] = value
    }
    return out
}
