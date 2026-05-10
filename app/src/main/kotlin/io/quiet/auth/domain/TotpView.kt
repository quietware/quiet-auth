package io.quiet.auth.domain

fun formatLiveTotpCode(item: TwoFAItem, nowMs: Long): String =
    generateTimeBasedCode(
        secret = item.secret,
        nowMs = nowMs,
        digits = item.digits,
        period = item.period,
        algorithm = item.algorithm,
    )

fun secondsLeftForPeriod(period: Int, nowMs: Long): Int =
    period - ((nowMs / 1000).rem(period)).toInt()
