package ir.dorehami.app.data

import android.content.Context

/** ذخیرهٔ نشست در حافظهٔ خصوصی اپ؛ توکن‌ها با کلید Keystore رمزنگاری می‌شوند. */
class SessionStore(context: Context) {
    private val p = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    private val crypto = TokenCrypto()

    init {
        // پاک‌سازی توکن‌های بدون رمزنگاری نسخهٔ قبل
        if (p.contains("at") || p.contains("rt")) p.edit().remove("at").remove("rt").apply()
    }

    private fun put(k: String, v: String?) {
        val enc = v?.let { crypto.encrypt(it) }
        p.edit().apply { if (enc == null) remove(k) else putString(k, enc) }.apply()
    }

    var accessToken: String?
        get() = p.getString("at2", null)?.let { crypto.decrypt(it) }
        private set(v) { put("at2", v) }
    var refreshToken: String?
        get() = p.getString("rt2", null)?.let { crypto.decrypt(it) }
        private set(v) { put("rt2", v) }
    var displayName: String?
        get() = p.getString("name", null)
        private set(v) { p.edit().putString("name", v).apply() }

    val loggedIn get() = refreshToken != null

    fun save(access: String, refresh: String, name: String) {
        accessToken = access; refreshToken = refresh; displayName = name
    }
    fun updateTokens(access: String, refresh: String) {
        accessToken = access; refreshToken = refresh
    }
    fun clear() { p.edit().clear().apply() }
}
