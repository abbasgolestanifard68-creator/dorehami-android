import SwiftUI

struct ChatListView: View {
    let repo: any ChatRepository
    @State private var items: [Conversation]?
    @State private var failure: String?

    var body: some View {
        NavigationStack {
            Group {
                if let list = items {
                    if list.isEmpty {
                        Text("هنوز گفتگویی ندارید")
                    } else {
                        List(list) { c in
                            NavigationLink(value: c) { row(c) }
                        }
                        .listStyle(.plain)
                    }
                } else if let message = failure {
                    VStack(spacing: 12) {
                        Text(message).foregroundColor(.red)
                        Button("تلاش دوباره") { Task { await load() } }
                    }
                    .padding(24)
                } else {
                    ProgressView()
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .navigationTitle("گفتگوها")
            .navigationDestination(for: Conversation.self) { c in
                ChatView(repo: repo, conversation: c)
            }
            // با برگشت از صفحهٔ چت هم فهرست به‌روز می‌شود
            .onAppear { Task { await load() } }
        }
    }

    private func row(_ c: Conversation) -> some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(c.title).bold().lineLimit(1)
                Text(c.lastMessage).font(.subheadline).foregroundColor(.secondary).lineLimit(1)
            }
            Spacer()
            Text(TimeFormat.hm(c.timeMillis)).font(.caption).foregroundColor(.secondary)
        }
    }

    @MainActor
    private func load() async {
        failure = nil
        do {
            items = try await repo.conversations()
        } catch {
            failure = error.localizedDescription
        }
    }
}

struct ChatView: View {
    let repo: any ChatRepository
    let conversation: Conversation

    @State private var messages: [Message] = []
    @State private var text = ""
    @State private var error: String?

    private var canSend: Bool {
        !text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    var body: some View {
        VStack(spacing: 0) {
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 8) {
                        ForEach(messages) { m in
                            MessageBubble(message: m).id(m.id)
                        }
                    }
                    .padding(12)
                }
                .onChange(of: messages.count) { _ in
                    if let last = messages.last {
                        withAnimation { proxy.scrollTo(last.id, anchor: .bottom) }
                    }
                }
            }
            Divider()
            if let message = error {
                Text(message).font(.footnote).foregroundColor(.red).padding(.horizontal, 12).padding(.top, 4)
            }
            HStack(spacing: 8) {
                TextField("پیام…", text: $text, axis: .vertical)
                    .lineLimit(1...4)
                    .textFieldStyle(.roundedBorder)
                Button("ارسال", action: send)
                    .buttonStyle(.borderedProminent)
                    .disabled(!canSend)
            }
            .padding(8)
        }
        .navigationTitle(conversation.title)
        .navigationBarTitleDisplayMode(.inline)
        .task { await load() }
    }

    @MainActor
    private func load() async {
        do {
            messages = try await repo.messages(conversationId: conversation.id)
        } catch {
            self.error = error.localizedDescription
        }
    }

    private func send() {
        let t = text.trimmingCharacters(in: .whitespacesAndNewlines)
        error = nil
        Task {
            do {
                let m = try await repo.send(conversationId: conversation.id, text: t)
                await MainActor.run {
                    messages.append(m)
                    text = ""
                }
            } catch {
                // متن در کادر می‌ماند تا دوباره تلاش شود
                await MainActor.run { self.error = error.localizedDescription }
            }
        }
    }
}

struct MessageBubble: View {
    let message: Message

    private var mark: String {
        switch message.status {
        case .sending: return "…"
        case .sent: return "✓"
        case .delivered, .read: return "✓✓"
        case .failed: return "!"
        }
    }

    private var meta: String {
        TimeFormat.hm(message.timeMillis) + (message.mine ? " " + mark : "")
    }

    var body: some View {
        // در چیدمان راست‌به‌چپ leading سمت راست است: پیام خودم راست، پیام طرف مقابل چپ
        HStack(spacing: 0) {
            if !message.mine { Spacer(minLength: 40) }
            VStack(alignment: .trailing, spacing: 2) {
                Text(message.text)
                Text(meta).font(.caption2).opacity(0.8)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(message.mine ? Theme.emerald : Color(uiColor: .secondarySystemBackground))
            .foregroundColor(message.mine ? Color.white : Color.primary)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            if message.mine { Spacer(minLength: 40) }
        }
    }
}
