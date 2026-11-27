import SwiftUI
import UIKit

/// رنگ‌های برند دورهمی (همان نسخهٔ اندروید): زمرد، سرمه‌ای، سفید؛ طلایی فقط تأکید.
enum Theme {
    static let emerald = Color(red: Double(0x16) / 255.0, green: Double(0x8A) / 255.0, blue: Double(0x63) / 255.0)
    static let navy = Color(red: Double(0x17) / 255.0, green: Double(0x2B) / 255.0, blue: Double(0x4D) / 255.0)
    static let gold = Color(red: Double(0xC9) / 255.0, green: Double(0xA2) / 255.0, blue: Double(0x27) / 255.0)

    /// روز: پس‌زمینهٔ سفید؛ شب: سرمه‌ای
    static let background = Color(uiColor: UIColor { trait in
        trait.userInterfaceStyle == .dark
            ? UIColor(red: CGFloat(0x17) / 255.0, green: CGFloat(0x2B) / 255.0, blue: CGFloat(0x4D) / 255.0, alpha: 1)
            : UIColor.white
    })
}

enum TimeFormat {
    static func hm(_ ms: Int64) -> String {
        if ms <= 0 { return "" }
        let f = DateFormatter()
        f.dateFormat = "HH:mm"
        return f.string(from: Date(timeIntervalSince1970: Double(ms) / 1000.0))
    }
}
