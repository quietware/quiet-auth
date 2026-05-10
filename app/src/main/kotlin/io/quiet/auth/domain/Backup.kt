package io.quiet.auth.domain

private val HEADER_FIELDS = listOf("id", "name", "account", "secret", "digits", "period", "algorithm")
private val CSV_NEEDS_QUOTE = Regex("[\",\r\n]")

private fun escapeCsvField(value: String): String =
    if (CSV_NEEDS_QUOTE.containsMatchIn(value)) "\"${value.replace("\"", "\"\"")}\"" else value

fun twoFAItemsToCsv(items: List<TwoFAItem>): String {
    val header = HEADER_FIELDS.joinToString(",")
    val rows = items.map { item ->
        listOf(
            escapeCsvField(item.id),
            escapeCsvField(item.name),
            escapeCsvField(item.account),
            escapeCsvField(item.secret),
            item.digits.toString(),
            item.period.toString(),
            escapeCsvField(item.algorithm.name),
        ).joinToString(",")
    }
    return (listOf(header) + rows).joinToString("\n")
}

private fun splitCsvLine(line: String): List<String> {
    val fields = mutableListOf<String>()
    val field = StringBuilder()
    var i = 0
    var inQuotes = false
    while (i < line.length) {
        val c = line[i]
        if (inQuotes) {
            when {
                c == '"' && i + 1 < line.length && line[i + 1] == '"' -> {
                    field.append('"'); i += 2
                }
                c == '"' -> { inQuotes = false; i++ }
                else -> { field.append(c); i++ }
            }
            continue
        }
        when (c) {
            '"' -> { inQuotes = true; i++ }
            ',' -> { fields.add(field.toString()); field.clear(); i++ }
            else -> { field.append(c); i++ }
        }
    }
    fields.add(field.toString())
    return fields
}

class BackupFormatException(message: String) : RuntimeException(message)

/**
 * Mirrors `domain/backup.ts#parseBackupCsv`. Throws BACKUP_EMPTY / BACKUP_INVALID_FORMAT
 * with identical messages so calling code can stay platform-agnostic.
 */
fun parseBackupCsv(csv: String): List<TwoFAItem> {
    val stripped = csv.removePrefix("\uFEFF").trim()
    if (stripped.isEmpty()) throw BackupFormatException("BACKUP_EMPTY")

    val lines = stripped.split("\r\n", "\n").filter { it.isNotEmpty() }
    if (lines.size < 2) throw BackupFormatException("BACKUP_EMPTY")

    val headerCells = splitCsvLine(lines[0]).map { it.trim().lowercase() }
    val colIndex = HashMap<String, Int>()
    for (key in HEADER_FIELDS) {
        val idx = headerCells.indexOf(key)
        if (idx < 0) throw BackupFormatException("BACKUP_INVALID_FORMAT")
        colIndex[key] = idx
    }

    val parsed = mutableListOf<TwoFAItem>()
    for (r in 1 until lines.size) {
        val cells = splitCsvLine(lines[r])
        fun cell(key: String): String = cells.getOrNull(colIndex.getValue(key)).orEmpty()

        val secret = cell("secret").replace("\\s".toRegex(), "").uppercase()
        if (secret.isEmpty()) continue

        val digits = cell("digits").toIntOrNull().let { if (it == 8) 8 else 6 }
        val period = cell("period").toIntOrNull()?.takeIf { it > 0 } ?: 30
        val algorithm = OtpAlgorithm.fromRaw(cell("algorithm"))

        parsed += TwoFAItem(
            id = cell("id").ifEmpty { System.currentTimeMillis().toString() },
            name = cell("name").ifEmpty { "Imported" },
            account = cell("account").ifEmpty { "user" },
            secret = secret,
            digits = digits,
            period = period,
            algorithm = algorithm,
        )
    }

    if (parsed.isEmpty()) throw BackupFormatException("BACKUP_EMPTY")
    return parsed
}
