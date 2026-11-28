package ir.dorehami.app.util

import java.time.LocalDate

/** تبدیل تاریخ شمسی و میلادی و ابزارهای نمایش تقویم شمسی (بدون وابستگی خارجی). */
object Jalali {
    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
    )

    fun toGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val y = jy + 1595
        var days = -355668 + (365 * y) + ((y / 33) * 8) + (((y % 33) + 3) / 4) + jd +
            (if (jm < 7) (jm - 1) * 31 else ((jm - 7) * 30) + 186)
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            days--
            gy += 100 * (days / 36524)
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        var gd = days + 1
        val leap = (gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0
        val sal = intArrayOf(0, 31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 13) {
            val v = sal[gm]
            if (gd <= v) break
            gd -= v
            gm++
        }
        return Triple(gy, gm, gd)
    }

    fun fromGregorian(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gdm[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + days / 31
            jd = 1 + days % 31
        } else {
            jm = 7 + (days - 186) / 30
            jd = 1 + (days - 186) % 30
        }
        return Triple(jy, jm, jd)
    }

    /** سال کبیسه یعنی ۳۰ اسفند وجود دارد: تبدیل رفت‌وبرگشتی همان تاریخ را برمی‌گرداند. */
    fun isLeap(jy: Int): Boolean {
        val g = toGregorian(jy, 12, 30)
        return fromGregorian(g.first, g.second, g.third) == Triple(jy, 12, 30)
    }

    fun monthLength(jy: Int, jm: Int): Int = when {
        jm <= 6 -> 31
        jm <= 11 -> 30
        else -> if (isLeap(jy)) 30 else 29
    }

    /** تاریخ را برای ارسال به سرور به صورت میلادی YYYY-MM-DD برمی‌گرداند (همیشه با ارقام انگلیسی). */
    fun toGregorianString(jy: Int, jm: Int, jd: Int): String {
        val (y, m, d) = toGregorian(jy, jm, jd)
        return y.toString().padStart(4, '0') + "-" + m.toString().padStart(2, '0') + "-" + d.toString().padStart(2, '0')
    }

    fun today(): Triple<Int, Int, Int> = LocalDate.now().let { fromGregorian(it.year, it.monthValue, it.dayOfMonth) }

    /** روز هفتهٔ اولین روز ماه؛ 0 = شنبه ... 6 = جمعه. */
    fun firstWeekday(jy: Int, jm: Int): Int {
        val (y, m, d) = toGregorian(jy, jm, 1)
        return (LocalDate.of(y, m, d).dayOfWeek.value + 1) % 7
    }

    fun isAfter(y: Int, m: Int, d: Int, other: Triple<Int, Int, Int>): Boolean =
        y > other.first || (y == other.first && (m > other.second || (m == other.second && d > other.third)))

    fun format(jy: Int, jm: Int, jd: Int): String =
        jy.toString().padStart(4, '0') + "/" + jm.toString().padStart(2, '0') + "/" + jd.toString().padStart(2, '0')

    fun toPersianDigits(s: String): String = buildString {
        for (c in s) append(if (c in '0'..'9') '\u06F0' + (c - '0') else c)
    }
}
