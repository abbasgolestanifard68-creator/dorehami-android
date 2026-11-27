import Foundation

/// نگهدارندهٔ مخزن‌های چت: «ورود آزمایشی» همیشه داده نمونه، و نسخهٔ واقعی با Config.useFakeChat انتخاب می‌شود.
final class ChatEnvironment: ObservableObject {
    let demo: any ChatRepository
    let live: any ChatRepository

    init(session: SessionStore) {
        let fake = FakeChatRepository()
        demo = fake
        if Config.useFakeChat {
            live = fake
        } else {
            live = ApiChatRepository(store: session)
        }
    }
}
