package io.quiet.auth.domain

enum class OtpAlgorithm {
    SHA1, SHA256, SHA512;

    companion object {
        fun fromRaw(value: String?): OtpAlgorithm = when (value?.uppercase()) {
            "SHA256" -> SHA256
            "SHA512" -> SHA512
            else -> SHA1
        }
    }
}

data class TwoFAItem(
    val id: String,
    val name: String,
    val account: String,
    val secret: String,
    val digits: Int = 6,
    val period: Int = 30,
    val algorithm: OtpAlgorithm = OtpAlgorithm.SHA1,
)

data class AddTwoFAInput(
    val name: String,
    val account: String,
    val secret: String,
    val digits: Int? = null,
    val period: Int? = null,
    val algorithm: OtpAlgorithm? = null,
)

data class NormalizedTwoFA(
    val name: String,
    val account: String,
    val secret: String,
    val digits: Int,
    val period: Int,
    val algorithm: OtpAlgorithm,
)

fun normalizeTwoFAInput(input: AddTwoFAInput): NormalizedTwoFA = NormalizedTwoFA(
    name = input.name.trim(),
    account = input.account.trim(),
    secret = input.secret.replace("\\s".toRegex(), "").uppercase(),
    digits = if (input.digits == 8) 8 else 6,
    period = input.period?.takeIf { it > 0 } ?: 30,
    algorithm = input.algorithm ?: OtpAlgorithm.SHA1,
)
