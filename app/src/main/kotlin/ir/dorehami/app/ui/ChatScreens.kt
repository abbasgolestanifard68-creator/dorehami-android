package ir.dorehami.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.dorehami.app.chat.ChatRepository
import ir.dorehami.app.chat.Conversation
import ir.dorehami.app.chat.Message
import ir.dorehami.app.chat.MessageStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatTime(ms: Long): String =
    if (ms <= 0L) "" else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ms))

private fun statusMark(s: MessageStatus): String = when (s) {
    MessageStatus.SENDING -> "…"
    MessageStatus.SENT -> "✓"
    MessageStatus.DELIVERED, MessageStatus.READ -> "✓✓"
    MessageStatus.FAILED -> "!"
}

@Composable
fun ChatListScreen(repo: ChatRepository, refreshKey: Int, onOpen: (Conversation) -> Unit) {
    var loaded by remember { mutableStateOf<List<Conversation>?>(null) }
    LaunchedEffect(repo, refreshKey) { loaded = repo.conversations() }
    val list = loaded
    when {
        list == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        list.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("هنوز گفتگویی ندارید") }
        else -> LazyColumn(Modifier.fillMaxSize()) {
            items(list, key = { it.id }) { c ->
                Row(
                    Modifier.fillMaxWidth().clickable { onOpen(c) }.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(c.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(c.lastMessage, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(formatTime(c.timeMillis), style = MaterialTheme.typography.labelSmall)
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun ChatScreen(repo: ChatRepository, conversation: Conversation, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var text by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(conversation.id) { messages = repo.messages(conversation.id) }
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex) }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("بازگشت") }
            Text(conversation.title, style = MaterialTheme.typography.titleMedium)
        }
        HorizontalDivider()
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(messages, key = { it.id }) { MessageBubble(it) }
        }
        HorizontalDivider()
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(text, { text = it }, modifier = Modifier.weight(1f), placeholder = { Text("پیام…") }, maxLines = 4)
            Spacer(Modifier.width(8.dp))
            Button(
                enabled = text.isNotBlank(),
                onClick = {
                    val t = text.trim()
                    text = ""
                    scope.launch { messages = messages + repo.send(conversation.id, t) }
                },
            ) { Text("ارسال") }
        }
    }
}

@Composable
private fun MessageBubble(m: Message) {
    val scheme = MaterialTheme.colorScheme
    // در چیدمان راست‌به‌چپ، Start سمت راست است: پیام خودم راست، پیام طرف مقابل چپ
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.mine) Arrangement.Start else Arrangement.End) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (m.mine) scheme.primary else scheme.surfaceVariant,
            contentColor = if (m.mine) scheme.onPrimary else scheme.onSurfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp),
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(m.text)
                Text(
                    formatTime(m.timeMillis) + if (m.mine) " " + statusMark(m.status) else "",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}
