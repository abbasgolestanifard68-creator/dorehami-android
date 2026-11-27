import Foundation

/// پیاده‌سازی بر اساس پیش‌نویس docs/API_CONTRACT.md؛ همهٔ مسیرها و نام فیلدها باید با بک‌اند تأیید شوند.
/// هنوز در اپ فعال نیست (Config.useFakeChat = true).
final class ApiChatRepository: ChatRepository {
    private let store: SessionStore

    init(store: SessionStore) {
        self.store = store
    }

    func conversations() async throws -> [Conversation] {
        let r = await Api.getAuthed("/conversations", store: store)
        let json = try ok(r)
        let arr = json["conversations"] as? [[String: Any]] ?? []
        return arr.compactMap { o -> Conversation? in
            guard let id = o["id"] as? String else { return nil }
            return Conversation(
                id: id,
                title: o["title"] as? String ?? "",
                lastMessage: o["lastMessage"] as? String ?? "",
                timeMillis: (o["lastMessageAt"] as? NSNumber)?.int64Value ?? 0,
                unread: (o["unread"] as? NSNumber)?.intValue ?? 0
            )
        }
    }

    func messages(conversationId: String) async throws -> [Message] {
        let r = await Api.getAuthed("/conversations/" + conversationId + "/messages", store: store)
        let json = try ok(r)
        let arr = json["messages"] as? [[String: Any]] ?? []
        return arr.compactMap { parse($0, conversationId: conversationId) }
    }

    func send(conversationId: String, text: String) async throws -> Message {
        let r = await Api.postAuthed("/conversations/" + conversationId + "/messages", body: ["text": text], store: store)
        let json = try ok(r)
        guard let o = json["message"] as? [String: Any], let m = parse(o, conversationId: conversationId) else {
            throw ChatError(message: "پاسخ سرور نامعتبر بود.")
        }
        return m
    }

    private func ok(_ r: ApiResult) throws -> [String: Any] {
        switch r {
        case .ok(let json): return json
        case .fail(_, let message, _): throw ChatError(message: message)
        }
    }

    private func parse(_ o: [String: Any], conversationId: String) -> Message? {
        guard let id = o["id"] as? String else { return nil }
        let status: MessageStatus
        switch (o["status"] as? String ?? "").lowercased() {
        case "sending": status = .sending
        case "delivered": status = .delivered
        case "read": status = .read
        case "failed": status = .failed
        default: status = .sent
        }
        return Message(
            id: id,
            conversationId: conversationId,
            text: o["text"] as? String ?? "",
            mine: o["mine"] as? Bool ?? false,
            timeMillis: (o["createdAt"] as? NSNumber)?.int64Value ?? 0,
            status: status
        )
    }
}
