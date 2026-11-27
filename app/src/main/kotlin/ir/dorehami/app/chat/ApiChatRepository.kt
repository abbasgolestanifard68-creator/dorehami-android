package ir.dorehami.app.chat

import ir.dorehami.app.data.SessionStore
import ir.dorehami.app.net.Api
import ir.dorehami.app.net.ApiResult
import org.json.JSONArray
import org.json.JSONObject

/**
 * پیاده‌سازی بر اساس پیش‌نویس docs/API_CONTRACT.md؛ همهٔ مسیرها و نام فیلدها باید با بک‌اند تأیید شوند.
 * هنوز در اپ فعال نیست (Config.USE_FAKE_CHAT = true).
 */
class ApiChatRepository(private val store: SessionStore) : ChatRepository {

    override suspend fun conversations(): List<Conversation> {
        val arr = ok(Api.getAuthed("/conversations", store)).optJSONArray("conversations") ?: JSONArray()
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Conversation(o.getString("id"), o.optString("title"), o.optString("lastMessage"), o.optLong("lastMessageAt"), o.optInt("unread"))
        }
    }

    override suspend fun messages(conversationId: String): List<Message> {
        val arr = ok(Api.getAuthed("/conversations/$conversationId/messages", store)).optJSONArray("messages") ?: JSONArray()
        return (0 until arr.length()).map { parse(arr.getJSONObject(it), conversationId) }
    }

    override suspend fun send(conversationId: String, text: String): Message {
        val j = ok(Api.postAuthed("/conversations/$conversationId/messages", JSONObject().put("text", text), store))
        val o = j.optJSONObject("message") ?: throw ChatException("پاسخ سرور نامعتبر بود.")
        return parse(o, conversationId)
    }

    private fun ok(r: ApiResult): JSONObject = when (r) {
        is ApiResult.Ok -> r.json
        is ApiResult.Fail -> throw ChatException(r.message)
    }

    private fun parse(o: JSONObject, conversationId: String) = Message(
        id = o.getString("id"),
        conversationId = conversationId,
        text = o.optString("text"),
        mine = o.optBoolean("mine"),
        timeMillis = o.optLong("createdAt"),
        status = when (o.optString("status").lowercase()) {
            "sending" -> MessageStatus.SENDING
            "delivered" -> MessageStatus.DELIVERED
            "read" -> MessageStatus.READ
            "failed" -> MessageStatus.FAILED
            else -> MessageStatus.SENT
        },
    )
}
