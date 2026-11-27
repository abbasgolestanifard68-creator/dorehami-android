package ir.dorehami.app.util

import java.time.LocalDate

/** اعتبارسنجی و نرمال‌سازی ورودی‌های فرم؛ پشتیبانی از ارقام فارسی و عربی. */
object Validators {
    fun normalizeDigits(s: String): String = buildString {
        for (c in s) append(
            when (c) {
                in '\u06F0'..'\u06F9' -> '0' + (c - '\u06F0')
                in '\u0660'..'\u0669' -> '0' + (c - '\u0660')
                else -> c
            }
        )
    }

    /** شمارهٔ موبایل ایران را به شکل 09123456789 برمی‌گرداند؛ نامعتبر = null. */
    fun normalizePhone(raw: String): String? {
        val d = normalizeDigits(raw).filter { !it.isWhitespace() && it != '-' }
        val p = when {
            d.startsWith("+98") -> "0" + d.drop(3)
            d.startsWith("0098") -> "0" + d.drop(4)
            d.startsWith("98") && d.length == 12 -> "0" + d.drop(2)
            d.length == 10 && d.startsWith("9") -> "0$d"
            else -> d
        }
        return if (Regex("^09\\d{9}$").matches(p)) p else null
    }

    /** تاریخ میلادی را به شکل YYYY-MM-DD برمی‌گرداند؛ نامعتبر یا آینده = null. */
    fun normalizeBirthDate(raw: String): String? {
        val s = normalizeDigits(raw).trim().replace('/', '-')
        if (!Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(s)) return null
        return try {
            val d = LocalDate.parse(s)
            if (d.year < 1900 || d.isAfter(LocalDate.now())) null else s
        } catch (e: Exception) {
            null
        }
    }

    /** نتیجهٔ بررسی قوانین رمز عبور؛ فقط نویسه‌های انگلیسی (ASCII) برای حرف، عدد و علامت حساب می‌شوند. */
    data class PasswordCheck(
        val longEnough: Boolean,
        val hasLower: Boolean,
        val hasUpper: Boolean,
        val hasDigit: Boolean,
        val hasSymbol: Boolean,
    ) {
        val ok: Boolean get() = longEnough && hasLower && hasUpper && hasDigit && hasSymbol
    }

    fun checkPassword(p: String) = PasswordCheck(
        longEnough = p.length >= 8,
        hasLower = p.any { it in 'a'..'z' },
        hasUpper = p.any { it in 'A'..'Z' },
        hasDigit = p.any { it in '0'..'9' },
        hasSymbol = p.any { it.code in 33..126 && !it.isLetterOrDigit() },
    )

    fun isStrongPassword(p: String): Boolean = checkPassword(p).ok
}
