package ir.dorehami.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// رنگ‌های برند دورهمی (تصمیم مالک): زمرد، سرمه‌ای، سفید؛ طلایی فقط تأکید
val Emerald = Color(0xFF168A63)
val Navy = Color(0xFF172B4D)
val White = Color(0xFFFFFFFF)
val Gold = Color(0xFFC9A227)

private val Light = lightColorScheme(primary = Emerald, onPrimary = White, background = White, onBackground = Navy, surface = White, onSurface = Navy, tertiary = Gold)
private val Dark = darkColorScheme(primary = Emerald, onPrimary = White, background = Navy, onBackground = White, surface = Navy, onSurface = White, tertiary = Gold)

/** روز: پس‌زمینهٔ سفید؛ شب: سرمه‌ای */
@Composable
fun DorehamiTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
