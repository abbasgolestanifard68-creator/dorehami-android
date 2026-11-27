import Foundation

/// اعتبارسنجی و نرمال‌سازی ورودی‌های فرم؛ همان منطق نسخهٔ اندروید.
enum Validators {
    /// ارقام فارسی و عربی را به انگلیسی تبدیل می‌کند.
    static func normalizeDigits(_ s: String) -> String {
        var out = ""
        for sc in s.unicodeScalars {
            let v = sc.value
            if v >= 0x06F0 && v <= 0x06F9 {
                out.unicodeScalars.append(UnicodeScalar(0x30 + (v - 0x06F0))!)
            } else if v >= 0x0660 && v <= 0x0669 {
                out.unicodeScalars.append(UnicodeScalar(0x30 + (v - 0x0660))!)
            } else {
                out.unicodeScalars.append(sc)
            }
        }
        return out
    }

    /// شمارهٔ موبایل ایران را به شکل 09123456789 برمی‌گرداند؛ نامعتبر = nil.
    static func normalizePhone(_ raw: String) -> String? {
        let d = normalizeDigits(raw).filter { !$0.isWhitespace && $0 != "-" }
        let p: String
        if d.hasPrefix("+98") {
            p = "0" + String(d.dropFirst(3))
        } else if d.hasPrefix("0098") {
            p = "0" + String(d.dropFirst(4))
        } else if d.hasPrefix("98") && d.count == 12 {
            p = "0" + String(d.dropFirst(2))
        } else if d.count == 10 && d.hasPrefix("9") {
            p = "0" + d
        } else {
            p = d
        }
        return p.range(of: "^09[0-9]{9}$", options: .regularExpression) != nil ? p : nil
    }

    /// تاریخ میلادی را به شکل YYYY-MM-DD برمی‌گرداند؛ نامعتبر یا آینده = nil.
    static func normalizeBirthDate(_ raw: String) -> String? {
        let s = normalizeDigits(raw).trimmingCharacters(in: .whitespaces).replacingOccurrences(of: "/", with: "-")
        guard s.allSatisfy({ $0.isASCII && ($0.isNumber || $0 == "-") }) else { return nil }
        let parts = s.split(separator: "-", omittingEmptySubsequences: false)
        guard parts.count == 3, parts[0].count == 4, parts[1].count == 2, parts[2].count == 2,
              let y = Int(parts[0]), let m = Int(parts[1]), let d = Int(parts[2]), y >= 1900 else { return nil }
        var cal = Calendar(identifier: .gregorian)
        cal.timeZone = TimeZone(identifier: "UTC")!
        guard let date = cal.date(from: DateComponents(year: y, month: m, day: d)) else { return nil }
        let c = cal.dateComponents([.year, .month, .day], from: date)
        guard c.year == y, c.month == m, c.day == d else { return nil }
        return date > Date() ? nil : s
    }

    /// نتیجهٔ بررسی قوانین رمز عبور؛ فقط نویسه‌های انگلیسی (ASCII) برای حرف، عدد و علامت حساب می‌شوند.
    struct PasswordCheck {
        let longEnough: Bool
        let hasLower: Bool
        let hasUpper: Bool
        let hasDigit: Bool
        let hasSymbol: Bool
        var ok: Bool { longEnough && hasLower && hasUpper && hasDigit && hasSymbol }
    }

    static func checkPassword(_ p: String) -> PasswordCheck {
        let values = p.unicodeScalars.map { $0.value }
        return PasswordCheck(
            longEnough: p.count >= 8,
            hasLower: values.contains { $0 >= 97 && $0 <= 122 },
            hasUpper: values.contains { $0 >= 65 && $0 <= 90 },
            hasDigit: values.contains { $0 >= 48 && $0 <= 57 },
            hasSymbol: values.contains { ($0 >= 33 && $0 <= 47) || ($0 >= 58 && $0 <= 64) || ($0 >= 91 && $0 <= 96) || ($0 >= 123 && $0 <= 126) }
        )
    }

    static func isStrongPassword(_ p: String) -> Bool { checkPassword(p).ok }
}
