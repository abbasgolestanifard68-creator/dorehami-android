import SwiftUI

/// نتیجهٔ پاسخ ورود/ثبت‌نام: در موفقیت نشست را ذخیره می‌کند، در خطا متن خطا را برمی‌گرداند.
enum AuthHelper {
    @MainActor
    static func handle(_ r: ApiResult, session: SessionStore) -> String? {
        switch r {
        case .ok(let json):
            guard let t = json["tokens"] as? [String: Any],
                  let access = t["accessToken"] as? String,
                  let refresh = t["refreshToken"] as? String else {
                return "پاسخ سرور نامعتبر بود."
            }
            let u = json["user"] as? [String: Any]
            let first = u?["firstName"] as? String ?? ""
            let last = u?["lastName"] as? String ?? ""
            let name = (first + " " + last).trimmingCharacters(in: .whitespaces)
            session.save(access: access, refresh: refresh, name: name)
            return nil
        case .fail(let code, let message, _):
            return code == "validation_error" ? "اطلاعات واردشده را بررسی کنید." : message
        }
    }
}

struct LoginView: View {
    @EnvironmentObject private var session: SessionStore
    let onRegister: () -> Void
    let onDemo: (() -> Void)?

    @State private var id = ""
    @State private var pass = ""
    @State private var busy = false
    @State private var error: String?

    var body: some View {
        ScrollView {
            VStack(spacing: 12) {
                Text("دورهمی")
                    .font(.largeTitle.bold())
                    .foregroundColor(Theme.emerald)
                    .padding(.top, 60)
                    .padding(.bottom, 12)
                TextField("نام کاربری یا شماره موبایل", text: $id)
                    .textFieldStyle(.roundedBorder)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                SecureField("رمز عبور", text: $pass)
                    .textFieldStyle(.roundedBorder)
                if let message = error {
                    Text(message).foregroundColor(.red).font(.footnote)
                }
                Button(action: login) {
                    Text(busy ? "…" : "ورود").frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .controlSize(.large)
                .disabled(busy || id.trimmingCharacters(in: .whitespaces).isEmpty || pass.isEmpty)
                Button("ثبت‌نام", action: onRegister)
                if let demo = onDemo {
                    Button("ورود آزمایشی (فقط نسخه debug)", action: demo)
                        .buttonStyle(.bordered)
                }
            }
            .padding(24)
        }
    }

    private func login() {
        busy = true
        error = nil
        let trimmed = Validators.normalizeDigits(id.trimmingCharacters(in: .whitespaces))
        let phone = Validators.normalizePhone(trimmed)
        let key = phone != nil ? "phone" : "username"
        let body: [String: Any] = [key: phone ?? trimmed, "password": pass]
        Task {
            let r = await Api.post("/auth/login", body: body)
            await MainActor.run {
                error = AuthHelper.handle(r, session: session)
                busy = false
            }
        }
    }
}

struct RegisterView: View {
    @EnvironmentObject private var session: SessionStore
    let onBack: () -> Void

    @State private var first = ""
    @State private var last = ""
    @State private var username = ""
    @State private var phone = ""
    @State private var pass = ""
    @State private var insta = ""
    @State private var birth: YMD?
    @State private var gender = "male"
    @State private var terms = false
    @State private var busy = false
    @State private var error: String?

    /// کاربر تاریخ شمسی انتخاب می‌کند؛ برای سرور به میلادی تبدیل می‌شود.
    private var birthString: String {
        guard let b = birth else { return "" }
        return Jalali.toGregorianString(b.y, b.m, b.d)
    }

    private func blank(_ s: String) -> Bool {
        s.trimmingCharacters(in: .whitespaces).isEmpty
    }

    private var canSubmit: Bool {
        !busy && terms && !blank(first) && !blank(last) && !blank(username) && !blank(phone)
            && Validators.isStrongPassword(pass) && birth != nil
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 10) {
                Text("ثبت‌نام")
                    .font(.title.bold())
                    .foregroundColor(Theme.emerald)
                    .padding(.top, 24)
                TextField("نام", text: $first).textFieldStyle(.roundedBorder)
                TextField("نام خانوادگی", text: $last).textFieldStyle(.roundedBorder)
                TextField("نام کاربری (انگلیسی)", text: $username)
                    .textFieldStyle(.roundedBorder)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                TextField("شماره موبایل", text: $phone)
                    .textFieldStyle(.roundedBorder)
                    .keyboardType(.phonePad)
                SecureField("رمز عبور", text: $pass).textFieldStyle(.roundedBorder)
                PasswordRules(pass: pass)
                JalaliDateField(value: $birth)
                TextField("آیدی اینستاگرام (اختیاری)", text: $insta)
                    .textFieldStyle(.roundedBorder)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                Picker("جنسیت", selection: $gender) {
                    Text("مرد").tag("male")
                    Text("زن").tag("female")
                }
                .pickerStyle(.segmented)
                Toggle("قوانین را می‌پذیرم", isOn: $terms)
                if let message = error {
                    Text(message).foregroundColor(.red).font(.footnote)
                }
                Button(action: submit) {
                    Text(busy ? "…" : "ثبت‌نام").frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .controlSize(.large)
                .disabled(!canSubmit)
                Button("بازگشت", action: onBack)
            }
            .padding(24)
        }
    }

    private func submit() {
        guard let ph = Validators.normalizePhone(phone) else {
            error = "شماره موبایل معتبر نیست (مثلاً 09123456789)."
            return
        }
        guard let bd = Validators.normalizeBirthDate(birthString) else {
            error = "تاریخ تولد معتبر نیست؛ دوباره انتخاب کنید."
            return
        }
        busy = true
        error = nil
        var body: [String: Any] = [
            "firstName": first.trimmingCharacters(in: .whitespaces),
            "lastName": last.trimmingCharacters(in: .whitespaces),
            "username": username.trimmingCharacters(in: .whitespaces),
            "phone": ph,
            "password": pass,
            "gender": gender,
            "birthDate": bd,
            "acceptTerms": true,
        ]
        if !blank(insta) { body["instagramId"] = insta.trimmingCharacters(in: .whitespaces) }
        let payload = body
        Task {
            let r = await Api.post("/auth/register", body: payload)
            await MainActor.run {
                error = AuthHelper.handle(r, session: session)
                busy = false
            }
        }
    }
}

/// فهرست قوانین رمز عبور که با تایپ کاربر زنده به‌روز می‌شود.
struct PasswordRules: View {
    let pass: String

    var body: some View {
        let c = Validators.checkPassword(pass)
        let rules: [(Bool, String)] = [
            (c.longEnough, "حداقل ۸ نویسه"),
            (c.hasLower, "یک حرف کوچک انگلیسی (a-z)"),
            (c.hasUpper, "یک حرف بزرگ انگلیسی (A-Z)"),
            (c.hasDigit, "یک عدد انگلیسی (0-9)"),
            (c.hasSymbol, "یک علامت مانند ! @ # $ % &"),
        ]
        VStack(alignment: .leading, spacing: 2) {
            ForEach(0..<rules.count, id: \.self) { i in
                Text((rules[i].0 ? "✓ " : "✗ ") + rules[i].1)
                    .font(.footnote)
                    .foregroundColor(rules[i].0 ? Theme.emerald : .secondary)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
