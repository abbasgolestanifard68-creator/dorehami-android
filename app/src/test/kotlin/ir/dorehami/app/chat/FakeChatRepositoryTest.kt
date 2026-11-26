package ir.dorehami.app.chat

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeChatRepositoryTest {
    @Test fun sendAppendsMessageAndUpdatesConversation() = runBlocking {
        val repo = FakeChatRepository()
        val before = repo.messages("1").size
        val sent = repo.send("1", "سلام")
        val after = repo.messages("1")
        assertEquals(before + 1, after.size)
        assertEquals(sent.id, after.last().id)
        assertTrue(after.last().mine)
        val conv = repo.conversations().first { it.id == "1" }
        assertEquals("سلام", conv.lastMessage)
        assertEquals("1", repo.conversations().first().id)
    }
}
