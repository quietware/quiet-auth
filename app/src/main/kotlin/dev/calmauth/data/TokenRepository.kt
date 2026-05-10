package dev.calmauth.data

import dev.calmauth.domain.OtpAlgorithm
import dev.calmauth.domain.TwoFAItem
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists the user's 2FA list under [STORAGE_KEY] using the same JSON layout produced by
 * `adapters/expo/token-repository-adapter.ts`, so a CSV-less migration via raw SharedPreferences
 * stays straightforward.
 */
class TokenRepository(private val storage: SecureStorage) {

    fun load(): List<TwoFAItem> {
        val raw = storage.getItem(STORAGE_KEY) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val out = ArrayList<TwoFAItem>(array.length())
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                if (obj.optString("id").isEmpty()) continue
                out += TwoFAItem(
                    id = obj.getString("id"),
                    name = obj.optString("name"),
                    account = obj.optString("account"),
                    secret = obj.optString("secret").ifEmpty { obj.optString("code", "MISSINGSECRET") },
                    digits = obj.optInt("digits", 6),
                    period = obj.optInt("period", 30),
                    algorithm = OtpAlgorithm.fromRaw(obj.optString("algorithm", "SHA1")),
                )
            }
            out
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(items: List<TwoFAItem>) {
        val array = JSONArray()
        for (item in items) {
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("name", item.name)
                    .put("account", item.account)
                    .put("secret", item.secret)
                    .put("digits", item.digits)
                    .put("period", item.period)
                    .put("algorithm", item.algorithm.name)
            )
        }
        storage.setItem(STORAGE_KEY, array.toString())
    }

    fun clear() {
        storage.deleteItem(STORAGE_KEY)
    }

    companion object {
        const val STORAGE_KEY = "calmauth_twofa_items_v1"
    }
}
