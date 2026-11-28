package ir.dorehami.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class JalaliTest {
    @Test fun toGregorianKnownDates() {
        assertEquals(Triple(2000, 5, 21), Jalali.toGregorian(1379, 3, 1))
        assertEquals(Triple(2024, 3, 20), Jalali.toGregorian(1403, 1, 1))
        assertEquals(Triple(2025, 3, 20), Jalali.toGregorian(1403, 12, 30))
    }

    @Test fun fromGregorianKnownDates() {
        assertEquals(Triple(1379, 3, 1), Jalali.fromGregorian(2000, 5, 21))
        assertEquals(Triple(1404, 1, 1), Jalali.fromGregorian(2025, 3, 21))
        assertEquals(Triple(1378, 10, 11), Jalali.fromGregorian(2000, 1, 1))
    }

    @Test fun leapYearsAndMonthLengths() {
        listOf(1395, 1399, 1403, 1408).forEach { assertTrue("$it", Jalali.isLeap(it)) }
        listOf(1400, 1401, 1402, 1404).forEach { assertFalse("$it", Jalali.isLeap(it)) }
        assertEquals(31, Jalali.monthLength(1403, 1))
        assertEquals(30, Jalali.monthLength(1403, 7))
        assertEquals(30, Jalali.monthLength(1403, 12))
        assertEquals(29, Jalali.monthLength(1404, 12))
    }

    @Test fun roundTripOverCentury() {
        var d = LocalDate.of(1930, 1, 1)
        val end = LocalDate.of(2030, 1, 1)
        while (d.isBefore(end)) {
            val j = Jalali.fromGregorian(d.year, d.monthValue, d.dayOfMonth)
            assertEquals(d.toString(), Triple(d.year, d.monthValue, d.dayOfMonth), Jalali.toGregorian(j.first, j.second, j.third))
            d = d.plusDays(1)
        }
    }

    @Test fun formatting() {
        assertEquals("2000-05-21", Jalali.toGregorianString(1379, 3, 1))
        assertEquals("1379/03/01", Jalali.format(1379, 3, 1))
        assertEquals("۱۳۷۹/۰۳/۰۱", Jalali.toPersianDigits("1379/03/01"))
    }

    @Test fun firstWeekdayStartsFromSaturday() {
        assertEquals(4, Jalali.firstWeekday(1403, 1)) // ۱ فروردین ۱۴۰۳ = چهارشنبه
    }

    @Test fun isAfter() {
        val t = Triple(1405, 9, 6)
        assertTrue(Jalali.isAfter(1405, 9, 7, t))
        assertFalse(Jalali.isAfter(1405, 9, 6, t))
        assertFalse(Jalali.isAfter(1404, 12, 29, t))
    }
}
