package ir.dorehami.app.chat

/** دادهٔ نمونه در حافظه، فقط برای توسعهٔ رابط کاربری؛ با بستن اپ از بین می‌رود. */
class FakeChatRepository : ChatRepository {
    private val now = System.currentTimeMillis()
    private val titles = mapOf("1" to "تیم دورهمی (نمونه)", "2" to "دوست نمونه")
    private val data = mutableMapOf(
        "1" to mutableListOf(
            Message("1-1", "1", "سلام، به دورهمی خوش آمدید!", false, now - 3_600_000, MessageStatus.READ),
            Message("1-2", "1", "ممنون، نسخهٔ آزمایشی چت آماده است؟", true, now - 3_000_000, MessageStatus.READ),
        ),
        "2" to mutableListOf(
            Message("2-1", "2", "امشب دورهمی داریم؟", false, now - 7_200_000, MessageStatus.READ),
        ),
    )

    override suspend fun conversations(): List<Conversation> =
        titles.map { (id, title) ->
            val last = data[id].orEmpty().lastOrNull()
            Conversation(id, title, last?.text ?: "", last?.timeMillis ?: 0L)
        }.sortedByDescending { it.timeMillis }

    override suspend fun messages(conversationId: String): List<Message> = data[conversationId].orEmpty().toList()

    override suspend fun send(conversationId: String, text: String): Message {
        val m = Message("$conversationId-${System.nanoTime()}", conversationId, text, true, System.currentTimeMillis())
        data.getOrPut(conversationId) { mutableListOf() }.add(m)
        return m
    }
}
