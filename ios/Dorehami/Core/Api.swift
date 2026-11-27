import Foundation

enum ApiResult {
    case ok([String: Any])
    case fail(code: String, message: String, status: Int)
}

enum Api {
    static func post(_ path: String, body: [String: Any], token: String? = nil) async -> ApiResult {
        await request("POST", path, body, token)
    }

    static func get(_ path: String, token: String? = nil) async -> ApiResult {
        await request("GET", path, nil, token)
    }

    private static func request(_ method: String, _ path: String, _ body: [String: Any]?, _ token: String?) async -> ApiResult {
        guard let url = URL(string: Config.apiBase + "/v1" + path) else {
            return .fail(code: "network", message: "آدرس سرور نامعتبر است.", status: 0)
        }
        var req = URLRequest(url: url, timeoutInterval: 20)
        req.httpMethod = method
        if let token = token {
            req.setValue("Bearer " + token, forHTTPHeaderField: "Authorization")
        }
        if let body = body {
            req.setValue("application/json; charset=utf-8", forHTTPHeaderField: "Content-Type")
            req.httpBody = try? JSONSerialization.data(withJSONObject: body)
        }
        do {
            let (data, resp) = try await URLSession.shared.data(for: req)
            let status = (resp as? HTTPURLResponse)?.statusCode ?? 0
            let json = (try? JSONSerialization.jsonObject(with: data)) as? [String: Any] ?? [:]
            if (200...299).contains(status) { return .ok(json) }
            return .fail(
                code: json["code"] as? String ?? "error",
                message: json["message"] as? String ?? "خطایی رخ داد.",
                status: status
            )
        } catch {
            return .fail(code: "network", message: "اتصال به سرور برقرار نشد.", status: 0)
        }
    }

    /// درخواست با توکن؛ اگر 401 شد یک‌بار توکن را تمدید می‌کند و دوباره تلاش می‌کند.
    static func postAuthed(_ path: String, body: [String: Any], store: SessionStore) async -> ApiResult {
        let first = await post(path, body: body, token: store.accessToken)
        guard case .fail(_, _, let status) = first, status == 401 else { return first }
        if await refresh(store) {
            return await post(path, body: body, token: store.accessToken)
        }
        return first
    }

    static func getAuthed(_ path: String, store: SessionStore) async -> ApiResult {
        let first = await get(path, token: store.accessToken)
        guard case .fail(_, _, let status) = first, status == 401 else { return first }
        if await refresh(store) {
            return await get(path, token: store.accessToken)
        }
        return first
    }

    /// فرض: POST /v1/auth/refresh با {"refreshToken"} و پاسخی هم‌شکل ورود. باید با بک‌اند تأیید شود.
    /// توجه: قفل هم‌زمانی ندارد؛ چند درخواست هم‌زمان ممکن است چند بار تمدید بزنند (TODO).
    private static func refresh(_ store: SessionStore) async -> Bool {
        guard let rt = store.refreshToken else { return false }
        let r = await post("/auth/refresh", body: ["refreshToken": rt])
        guard case .ok(let json) = r,
              let tokens = json["tokens"] as? [String: Any],
              let access = tokens["accessToken"] as? String else { return false }
        store.updateTokens(access: access, refresh: tokens["refreshToken"] as? String ?? rt)
        return true
    }
}
