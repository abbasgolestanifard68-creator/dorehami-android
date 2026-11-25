package ir.dorehami.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ir.dorehami.app.ui.DorehamiTheme

/** مرحلهٔ ۰: فقط اسکلت؛ صفحه‌ها در مرحله‌های بعد */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DorehamiTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("دورهمی", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineLarge)
                    }
                }
            }
        }
    }
}
