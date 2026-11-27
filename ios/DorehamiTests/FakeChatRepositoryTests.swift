import XCTest
@testable import Dorehami

final class FakeChatRepositoryTests: XCTestCase {
    func testSendAppendsMessageAndUpdatesConversation() async throws {
        let repo = FakeChatRepository()
        let before = try await repo.messages(conversationId: "1").count
        let sent = try await repo.send(conversationId: "1", text: "سلام")
        let after = try await repo.messages(conversationId: "1")
        XCTAssertEqual(after.count, before + 1)
        XCTAssertEqual(after.last?.id, sent.id)
        XCTAssertEqual(after.last?.mine, true)
        let list = try await repo.conversations()
        XCTAssertEqual(list.first?.id, "1")
        XCTAssertEqual(list.first(where: { $0.id == "1" })?.lastMessage, "سلام")
    }
}
