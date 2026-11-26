package ir.dorehami.app.net

import ir.dorehami.app.Config
import ir.dorehami.app.data.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed class ApiResult {
    data class Ok(val json: JSONObject) : ApiResult()
    data class Fail(val code: String, val message: String, val status: Int = 0) : ApiResult()
}

object Api {
    private val refreshLock = Mutex()

    suspend fun post(path: String, body: JSONObject, token: String? = null): ApiResult = withContext(Dispatchers.IO) {
        try {
            val c = URL(Config.API_BASE + "/v1" + path).openConnection() as HttpURLConnection
            c.requestMethod = "POST"
            c.connectTimeout = 15000
            c.readTimeout = 20000
            c.doOutput = true
            c.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            if (token != null) c.setRequestProperty("Authorization", "Bearer $token")
            c.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val status = c.responseCode
            val text = (if (status in 200..299) c.inputStream else c.errorStream)?.bufferedReader()?.use { it.readText() } ?: "{}"
            val json = try { JSONObject(text) } catch (e: Exception) { JSONObject() }
            if (status in 200..299) ApiResult.Ok(json)
            else ApiResult.Fail(json.optString("code", "error"), json.optString("message", "خطایی رخ داد."), status)
        } catch (e: Exception) {
            ApiResult.Fail("network", "اتصال به سرور برقرار نشد.")
        }
    }

    /** درخواست با توکن؛ اگر 401 شد یک‌بار توکن را تمدید می‌کند و دوباره تلاش می‌کند. */
    suspend fun postAuthed(path: String, body: JSONObject, store: SessionStore): ApiResult {
        val first = post(path, body, store.accessToken)
        if (first !is ApiResult.Fail || first.status != 401) return first
        return if (refresh(store)) post(path, body, store.accessToken) else first
    }

    /** فرض: POST /v1/auth/refresh با {"refreshToken"} و پاسخی هم‌شکل ورود. باید با بک‌اند تأیید شود. */
    private suspend fun refresh(store: SessionStore): Boolean = refreshLock.withLock {
        val rt = store.refreshToken ?: return false
        val r = post("/auth/refresh", JSONObject().put("refreshToken", rt))
        val t = (r as? ApiResult.Ok)?.json?.optJSONObject("tokens") ?: return false
        store.updateTokens(t.getString("accessToken"), t.optString("refreshToken", rt))
        true
    }
}
