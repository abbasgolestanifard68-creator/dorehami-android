package ir.dorehami.app.chat

data class Conversation(val id: String, val title: String, val lastMessage: String, val timeMillis: Long, val unread: Int = 0)

enum class MessageStatus { SENDING, SENT, DELIVERED, READ, FAILED }

data class Message(
    val id: String,
    val conversationId: String,
    val text: String,
    val mine: Boolean,
    val timeMillis: Long,
    val status: MessageStatus = MessageStatus.SENT,
)

/** لایهٔ داده چت. تا نهایی شدن API و بک‌اند، FakeChatRepository جایگزین پیاده‌سازی واقعی است. */
interface ChatRepository {
    suspend fun conversations(): List<Conversation>
    suspend fun messages(conversationId: String): List<Message>
    suspend fun send(conversationId: String, text: String): Message
}
