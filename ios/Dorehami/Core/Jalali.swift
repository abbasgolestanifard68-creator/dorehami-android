import Foundation

/// سال/ماه/روز
struct YMD: Equatable, Hashable {
    var y: Int
    var m: Int
    var d: Int
}

/// تبدیل تاریخ شمسی و میلادی (همان الگوریتم نسخهٔ اندروید که با تاریخ‌های مشخص آزموده شده).
enum Jalali {
    static let monthNames = [
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
    ]

    static func toGregorian(_ jy: Int, _ jm: Int, _ jd: Int) -> YMD {
        let y: Int = jy + 1595
        var days: Int = -355668 + 365 * y
        days += (y / 33) * 8
        days += ((y % 33) + 3) / 4
        days += jd
        if jm < 7 {
            days += (jm - 1) * 31
        } else {
            days += (jm - 7) * 30 + 186
        }
        var gy: Int = 400 * (days / 146097)
        days %= 146097
        if days > 36524 {
            days -= 1
            gy += 100 * (days / 36524)
            days %= 36524
            if days >= 365 { days += 1 }
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if days > 365 {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        var gd: Int = days + 1
        let leap: Bool = (gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0
        let feb: Int = leap ? 29 : 28
        let sal: [Int] = [0, 31, feb, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]
        var gm = 0
        while gm < 13 {
            let v = sal[gm]
            if gd <= v { break }
            gd -= v
            gm += 1
        }
        return YMD(y: gy, m: gm, d: gd)
    }

    static func fromGregorian(_ gy: Int, _ gm: Int, _ gd: Int) -> YMD {
        let gdm: [Int] = [0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334]
        let gy2: Int = gm > 2 ? gy + 1 : gy
        var days: Int = 355666 + 365 * gy
        days += (gy2 + 3) / 4
        days -= (gy2 + 99) / 100
        days += (gy2 + 399) / 400
        days += gd + gdm[gm - 1]
        var jy: Int = -1595 + 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if days > 365 {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        let jm: Int
        let jd: Int
        if days < 186 {
            jm = 1 + days / 31
            jd = 1 + days % 31
        } else {
            jm = 7 + (days - 186) / 30
            jd = 1 + (days - 186) % 30
        }
        return YMD(y: jy, m: jm, d: jd)
    }

    /// سال کبیسه یعنی ۳۰ اسفند وجود دارد: تبدیل رفت‌وبرگشتی همان تاریخ را برمی‌گرداند.
    static func isLeap(_ jy: Int) -> Bool {
        let g = toGregorian(jy, 12, 30)
        return fromGregorian(g.y, g.m, g.d) == YMD(y: jy, m: 12, d: 30)
    }

    static func monthLength(_ jy: Int, _ jm: Int) -> Int {
        if jm <= 6 { return 31 }
        if jm <= 11 { return 30 }
        return isLeap(jy) ? 30 : 29
    }

    /// برای ارسال به سرور: میلادی YYYY-MM-DD با ارقام انگلیسی.
    static func toGregorianString(_ jy: Int, _ jm: Int, _ jd: Int) -> String {
        let g = toGregorian(jy, jm, jd)
        return pad(g.y, 4) + "-" + pad(g.m, 2) + "-" + pad(g.d, 2)
    }

    static func today() -> YMD {
        let c = Calendar(identifier: .gregorian).dateComponents([.year, .month, .day], from: Date())
        return fromGregorian(c.year ?? 2000, c.month ?? 1, c.day ?? 1)
    }

    /// روز هفتهٔ اولین روز ماه؛ 0 = شنبه ... 6 = جمعه.
    static func firstWeekday(_ jy: Int, _ jm: Int) -> Int {
        let g = toGregorian(jy, jm, 1)
        var cal = Calendar(identifier: .gregorian)
        cal.timeZone = TimeZone(identifier: "UTC")!
        guard let date = cal.date(from: DateComponents(year: g.y, month: g.m, day: g.d)) else { return 0 }
        return cal.component(.weekday, from: date) % 7 // 1 = یکشنبه ... 7 = شنبه
    }

    static func isAfter(_ a: YMD, _ b: YMD) -> Bool {
        if a.y != b.y { return a.y > b.y }
        if a.m != b.m { return a.m > b.m }
        return a.d > b.d
    }

    static func format(_ jy: Int, _ jm: Int, _ jd: Int) -> String {
        pad(jy, 4) + "/" + pad(jm, 2) + "/" + pad(jd, 2)
    }

    static func toPersianDigits(_ s: String) -> String {
        var out = ""
        for sc in s.unicodeScalars {
            if sc.value >= 0x30 && sc.value <= 0x39 {
                out.unicodeScalars.append(UnicodeScalar(0x06F0 + (sc.value - 0x30))!)
            } else {
                out.unicodeScalars.append(sc)
            }
        }
        return out
    }

    private static func pad(_ n: Int, _ width: Int) -> String {
        let s = String(n)
        return s.count >= width ? s : String(repeating: "0", count: width - s.count) + s
    }
}
