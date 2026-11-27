import SwiftUI

/// صفحهٔ اصلی بعد از ورود: تب گفتگوها و تب پروفایل.
struct HomeView: View {
    let name: String
    let repo: any ChatRepository
    let onLogout: () -> Void

    var body: some View {
        TabView {
            ChatListView(repo: repo)
                .tabItem { Label("گفتگوها", systemImage: "bubble.left.and.bubble.right") }
            ProfileView(name: name, onLogout: onLogout)
                .tabItem { Label("پروفایل", systemImage: "person") }
        }
    }
}

struct ProfileView: View {
    let name: String
    let onLogout: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Text(name.isEmpty ? "کاربر دورهمی" : name).font(.title2.bold())
            Button("خروج", action: onLogout).buttonStyle(.bordered)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
