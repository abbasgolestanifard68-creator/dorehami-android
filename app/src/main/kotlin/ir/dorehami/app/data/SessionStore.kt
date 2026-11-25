package ir.dorehami.app.data

import android.content.Context

/** ذخیرهٔ نشست در حافظهٔ خصوصی اپ. رمزنگاری با Keystore در مرحلهٔ بعد اضافه می‌شود. */
class SessionStore(context: Context) {
    private val p = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    var accessToken: String?
        get() = p.getString("at", null)
        private set(v) { p.edit().putString("at", v).apply() }
    var refreshToken: String?
        get() = p.getString("rt", null)
        private set(v) { p.edit().putString("rt", v).apply() }
    var displayName: String?
        get() = p.getString("name", null)
        private set(v) { p.edit().putString("name", v).apply() }

    val loggedIn get() = refreshToken != null

    fun save(access: String, refresh: String, name: String) {
        accessToken = access; refreshToken = refresh; displayName = name
    }
    fun clear() { p.edit().clear().apply() }
}
