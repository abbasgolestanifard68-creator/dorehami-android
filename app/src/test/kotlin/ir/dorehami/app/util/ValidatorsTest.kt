package ir.dorehami.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {
    @Test fun persianAndArabicDigits() {
        assertEquals("09123456789", Validators.normalizeDigits("۰۹۱۲۳۴۵۶۷۸۹"))
        assertEquals("09123456789", Validators.normalizeDigits("٠٩١٢٣٤٥٦٧٨٩"))
    }

    @Test fun validPhoneFormats() {
        listOf("09123456789", "+989123456789", "00989123456789", "9123456789", "۰۹۱۲ ۳۴۵ ۶۷۸۹", "0912-345-6789")
            .forEach { assertEquals(it, "09123456789", Validators.normalizePhone(it)) }
    }

    @Test fun invalidPhones() {
        assertNull(Validators.normalizePhone("12345"))
        assertNull(Validators.normalizePhone("08123456789"))
        assertNull(Validators.normalizePhone("abc"))
    }

    @Test fun birthDate() {
        assertEquals("2000-05-21", Validators.normalizeBirthDate("۲۰۰۰-۰۵-۲۱"))
        assertEquals("2000-05-21", Validators.normalizeBirthDate("2000/05/21"))
        assertNull(Validators.normalizeBirthDate("2000-13-01"))
        assertNull(Validators.normalizeBirthDate("2999-01-01"))
        assertNull(Validators.normalizeBirthDate("abc"))
    }

    @Test fun strongPasswords() {
        assertTrue(Validators.isStrongPassword("Abcdef1!"))
        assertTrue(Validators.isStrongPassword("dorehami_Ok9"))
    }

    @Test fun weakPasswords() {
        assertFalse(Validators.isStrongPassword("abcdefg1!"))   // بدون حرف بزرگ
        assertFalse(Validators.isStrongPassword("ABCDEFG1!"))   // بدون حرف کوچک
        assertFalse(Validators.isStrongPassword("Abcdefgh!"))   // بدون عدد
        assertFalse(Validators.isStrongPassword("Abcdefg12"))   // بدون علامت
        assertFalse(Validators.isStrongPassword("Ab1!"))        // کوتاه
        assertFalse(Validators.isStrongPassword("Abcdef۱!"))    // عدد فارسی، عدد انگلیسی نیست
        assertFalse(Validators.isStrongPassword("Abcdef1 "))    // فاصله علامت حساب نمی‌شود
    }

    @Test fun passwordCheckFlags() {
        val c = Validators.checkPassword("abc")
        assertTrue(c.hasLower)
        assertFalse(c.hasUpper || c.hasDigit || c.hasSymbol || c.longEnough)
    }
}
