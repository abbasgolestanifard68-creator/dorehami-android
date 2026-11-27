import SwiftUI

@main
struct DorehamiApp: App {
    @StateObject private var session: SessionStore
    @StateObject private var chat: ChatEnvironment

    init() {
        let s = SessionStore()
        _session = StateObject(wrappedValue: s)
        _chat = StateObject(wrappedValue: ChatEnvironment(session: s))
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(session)
                .environmentObject(chat)
                .environment(\.layoutDirection, .rightToLeft)
                .tint(Theme.emerald)
        }
    }
}

struct RootView: View {
    @EnvironmentObject private var session: SessionStore
    @EnvironmentObject private var chat: ChatEnvironment
    @State private var showRegister = false
    @State private var demo = false

    var body: some View {
        Group {
            if session.isLoggedIn || demo {
                HomeView(
                    name: demo ? "کاربر آزمایشی" : session.displayName,
                    repo: demo ? chat.demo : chat.live,
                    onLogout: {
                        session.clear()
                        demo = false
                    }
                )
            } else if showRegister {
                RegisterView(onBack: { showRegister = false })
            } else {
                LoginView(onRegister: { showRegister = true }, onDemo: demoAction)
            }
        }
        .background(Theme.background.ignoresSafeArea())
    }

    /// فقط در نسخهٔ debug نمایش داده می‌شود؛ بدون سرور و بدون ذخیرهٔ توکن.
    private var demoAction: (() -> Void)? {
        #if DEBUG
        return { demo = true }
        #else
        return nil
        #endif
    }
}
