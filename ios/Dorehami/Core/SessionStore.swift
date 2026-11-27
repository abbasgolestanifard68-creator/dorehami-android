import Foundation
import Security

/// ذخیرهٔ امن توکن‌ها در Keychain (معادل Keystore در اندروید).
enum Keychain {
    private static let service = "ir.dorehami.app.session"

    private static func baseQuery(_ key: String) -> [String: Any] {
        [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
        ]
    }

    static func set(_ value: String, for key: String) {
        SecItemDelete(baseQuery(key) as CFDictionary)
        var add = baseQuery(key)
        add[kSecValueData as String] = Data(value.utf8)
        add[kSecAttrAccessible as String] = kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
        SecItemAdd(add as CFDictionary, nil)
    }

    static func get(_ key: String) -> String? {
        var q = baseQuery(key)
        q[kSecReturnData as String] = true
        q[kSecMatchLimit as String] = kSecMatchLimitOne
        var out: AnyObject?
        guard SecItemCopyMatching(q as CFDictionary, &out) == errSecSuccess, let data = out as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }

    static func delete(_ key: String) {
        SecItemDelete(baseQuery(key) as CFDictionary)
    }
}

/// نشست کاربر. توکن‌ها فقط در Keychain می‌مانند؛ نام نمایشی در UserDefaults.
final class SessionStore: ObservableObject {
    @Published private(set) var isLoggedIn = false
    @Published private(set) var displayName = ""

    init() {
        let d = UserDefaults.standard
        // Keychain بعد از حذف اپ هم می‌ماند؛ با اولین اجرای بعد از نصب تمیزش می‌کنیم
        if !d.bool(forKey: "hasLaunched") {
            Keychain.delete("access")
            Keychain.delete("refresh")
            d.set(true, forKey: "hasLaunched")
        }
        isLoggedIn = Keychain.get("refresh") != nil
        displayName = d.string(forKey: "displayName") ?? ""
    }

    var accessToken: String? { Keychain.get("access") }
    var refreshToken: String? { Keychain.get("refresh") }

    func save(access: String, refresh: String, name: String) {
        Keychain.set(access, for: "access")
        Keychain.set(refresh, for: "refresh")
        UserDefaults.standard.set(name, forKey: "displayName")
        displayName = name
        isLoggedIn = true
    }

    /// تمدید توکن (بدون تغییر در وضعیت ورود؛ از هر thread امن است).
    func updateTokens(access: String, refresh: String) {
        Keychain.set(access, for: "access")
        Keychain.set(refresh, for: "refresh")
    }

    func clear() {
        Keychain.delete("access")
        Keychain.delete("refresh")
        UserDefaults.standard.removeObject(forKey: "displayName")
        displayName = ""
        isLoggedIn = false
    }
}
