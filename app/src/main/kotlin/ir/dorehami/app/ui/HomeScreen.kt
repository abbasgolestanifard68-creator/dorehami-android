package ir.dorehami.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.dorehami.app.chat.ChatRepository
import ir.dorehami.app.chat.Conversation

/** صفحهٔ اصلی بعد از ورود: تب گفتگوها و تب پروفایل؛ با باز شدن یک گفتگو، صفحهٔ چت جایگزین می‌شود. */
@Composable
fun HomeScreen(name: String, repo: ChatRepository, onLogout: () -> Unit) {
    var tab by remember { mutableStateOf(0) }
    var open by remember { mutableStateOf<Conversation?>(null) }
    var refresh by remember { mutableStateOf(0) }

    val current = open
    if (current != null) {
        ChatScreen(repo, current) { open = null; refresh++ }
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Text("💬") }, label = { Text("گفتگوها") })
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Text("👤") }, label = { Text("پروفایل") })
            }
        }
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            if (tab == 0) {
                ChatListScreen(repo, refresh) { open = it }
            } else {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(if (name.isBlank()) "کاربر دورهمی" else name, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = onLogout) { Text("خروج") }
                }
            }
        }
    }
}
