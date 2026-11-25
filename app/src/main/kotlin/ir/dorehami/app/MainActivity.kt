package ir.dorehami.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ir.dorehami.app.data.SessionStore
import ir.dorehami.app.ui.DorehamiTheme
import ir.dorehami.app.ui.LoginScreen
import ir.dorehami.app.ui.RegisterScreen

/** فاز ۱: ثبت‌نام، ورود و نگهداری نشست. نقشه و چت در فازهای بعد. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = SessionStore(applicationContext)
        setContent {
            DorehamiTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        var screen by remember { mutableStateOf(if (store.loggedIn) "home" else "login") }
                        val done: (String, String, String) -> Unit = { a, r, n -> store.save(a, r, n); screen = "home" }
                        when (screen) {
                            "login" -> LoginScreen(done) { screen = "register" }
                            "register" -> RegisterScreen(done) { screen = "login" }
                            else -> Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("خوش آمدید " + (store.displayName ?: ""), style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(16.dp))
                                OutlinedButton(onClick = { store.clear(); screen = "login" }) { Text("خروج") }
                            }
                        }
                    }
                }
            }
        }
    }
}
