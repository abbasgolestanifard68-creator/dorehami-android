package ir.dorehami.app.net

import ir.dorehami.app.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed class ApiResult {
    data class Ok(val json: JSONObject) : ApiResult()
    data class Fail(val code: String, val message: String) : ApiResult()
}

object Api {
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
            else ApiResult.Fail(json.optString("code", "error"), json.optString("message", "خطایی رخ داد."))
        } catch (e: Exception) {
            ApiResult.Fail("network", "اتصال به سرور برقرار نشد.")
        }
    }
}
