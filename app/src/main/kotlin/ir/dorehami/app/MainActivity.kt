package ir.dorehami.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import ir.dorehami.app.chat.FakeChatRepository
import ir.dorehami.app.data.SessionStore
import ir.dorehami.app.ui.DorehamiTheme
import ir.dorehami.app.ui.HomeScreen
import ir.dorehami.app.ui.LoginScreen
import ir.dorehami.app.ui.RegisterScreen

/** فاز ۲ (در جریان): ورود/ثبت‌نام، نشست، و رابط چت روی داده نمونه. نقشه در فازهای بعد. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = SessionStore(applicationContext)
        val chatRepo = FakeChatRepository() // TODO: با پیاده‌سازی API واقعی جایگزین شود
        setContent {
            DorehamiTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        // اندروید ۱۵ به‌صورت پیش‌فرض لبه‌به‌لبه است؛ محتوا را از زیر نوار وضعیت و کیبورد بیرون نگه می‌داریم
                        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                            var screen by remember { mutableStateOf(if (store.loggedIn) "home" else "login") }
                            val done: (String, String, String) -> Unit = { a, r, n -> store.save(a, r, n); screen = "home" }
                            when (screen) {
                                "login" -> LoginScreen(done) { screen = "register" }
                                "register" -> RegisterScreen(done) { screen = "login" }
                                else -> HomeScreen(store.displayName ?: "", chatRepo) { store.clear(); screen = "login" }
                            }
                        }
                    }
                }
            }
        }
    }
}
