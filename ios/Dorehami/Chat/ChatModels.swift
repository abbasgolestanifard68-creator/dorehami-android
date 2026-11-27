import Foundation

struct Conversation: Identifiable, Hashable {
    let id: String
    var title: String
    var lastMessage: String
    var timeMillis: Int64
    var unread: Int = 0
}

enum MessageStatus {
    case sending, sent, delivered, read, failed
}

struct Message: Identifiable, Hashable {
    let id: String
    let conversationId: String
    let text: String
    let mine: Bool
    let timeMillis: Int64
    var status: MessageStatus = .sent
}

/// خطای قابل نمایش به کاربر هنگام کار با چت (مثلاً قطع اتصال).
struct ChatError: LocalizedError {
    let message: String
    var errorDescription: String? { message }
}

/// لایهٔ داده چت. با Config.useFakeChat بین داده نمونه و API واقعی عوض می‌شود.
protocol ChatRepository {
    func conversations() async throws -> [Conversation]
    func messages(conversationId: String) async throws -> [Message]
    func send(conversationId: String, text: String) async throws -> Message
}
