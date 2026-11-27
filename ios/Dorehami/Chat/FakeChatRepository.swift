import Foundation

/// دادهٔ نمونه در حافظه، فقط برای توسعهٔ رابط کاربری؛ با بستن اپ از بین می‌رود.
actor FakeChatRepository: ChatRepository {
    private let titles: [String: String] = ["1": "تیم دورهمی (نمونه)", "2": "دوست نمونه"]
    private var data: [String: [Message]]

    init() {
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        data = [
            "1": [
                Message(id: "1-1", conversationId: "1", text: "سلام، به دورهمی خوش آمدید!", mine: false, timeMillis: now - 3_600_000, status: .read),
                Message(id: "1-2", conversationId: "1", text: "ممنون، نسخهٔ آزمایشی چت آماده است؟", mine: true, timeMillis: now - 3_000_000, status: .read),
            ],
            "2": [
                Message(id: "2-1", conversationId: "2", text: "امشب دورهمی داریم؟", mine: false, timeMillis: now - 7_200_000, status: .read),
            ],
        ]
    }

    func conversations() async throws -> [Conversation] {
        titles.map { (id, title) in
            let last = data[id]?.last
            return Conversation(id: id, title: title, lastMessage: last?.text ?? "", timeMillis: last?.timeMillis ?? 0)
        }.sorted { $0.timeMillis > $1.timeMillis }
    }

    func messages(conversationId: String) async throws -> [Message] {
        data[conversationId] ?? []
    }

    func send(conversationId: String, text: String) async throws -> Message {
        let m = Message(
            id: conversationId + "-" + UUID().uuidString,
            conversationId: conversationId,
            text: text,
            mine: true,
            timeMillis: Int64(Date().timeIntervalSince1970 * 1000)
        )
        data[conversationId, default: []].append(m)
        return m
    }
}
