import SwiftUI

/// فیلد انتخاب تاریخ شمسی؛ با لمس آن، تقویم انتخاب باز می‌شود.
struct JalaliDateField: View {
    @Binding var value: YMD?
    @State private var open = false

    private var label: String {
        guard let v = value else { return "تاریخ تولد (شمسی) را انتخاب کنید" }
        return "تاریخ تولد: " + Jalali.toPersianDigits(Jalali.format(v.y, v.m, v.d))
    }

    var body: some View {
        Button {
            open = true
        } label: {
            Text(label).frame(maxWidth: .infinity, minHeight: 36)
        }
        .buttonStyle(.bordered)
        .sheet(isPresented: $open) {
            JalaliDateSheet(
                initial: value,
                onPick: { picked in
                    value = picked
                    open = false
                },
                onCancel: { open = false }
            )
        }
    }
}

struct JalaliDateSheet: View {
    let initial: YMD?
    let onPick: (YMD) -> Void
    let onCancel: () -> Void

    @State private var year: Int
    @State private var month: Int
    @State private var day: Int?
    private let today = Jalali.today()
    private let weekdayLetters = ["ش", "ی", "د", "س", "چ", "پ", "ج"] // شنبه تا جمعه

    init(initial: YMD?, onPick: @escaping (YMD) -> Void, onCancel: @escaping () -> Void) {
        self.initial = initial
        self.onPick = onPick
        self.onCancel = onCancel
        let t = Jalali.today()
        _year = State(initialValue: initial?.y ?? (t.y - 25))
        _month = State(initialValue: initial?.m ?? 1)
        _day = State(initialValue: initial?.d)
    }

    var body: some View {
        let len = Jalali.monthLength(year, month)
        let offset = Jalali.firstWeekday(year, month)
        let years: [Int] = Array((1300...today.y).reversed())

        NavigationStack {
            VStack(spacing: 12) {
                HStack {
                    Picker("سال", selection: $year) {
                        ForEach(years, id: \.self) { y in
                            Text(Jalali.toPersianDigits(String(y))).tag(y)
                        }
                    }
                    .pickerStyle(.menu)
                    Picker("ماه", selection: $month) {
                        ForEach(1...12, id: \.self) { m in
                            Text(Jalali.monthNames[m - 1]).tag(m)
                        }
                    }
                    .pickerStyle(.menu)
                }
                HStack(spacing: 0) {
                    ForEach(weekdayLetters, id: \.self) { letter in
                        Text(letter).font(.caption).frame(maxWidth: .infinity)
                    }
                }
                LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 7), spacing: 6) {
                    ForEach(0..<(offset + len), id: \.self) { i in
                        let d = i - offset + 1
                        if d >= 1 {
                            dayCell(d)
                        } else {
                            Color.clear.frame(height: 36)
                        }
                    }
                }
                Spacer()
            }
            .padding()
            .navigationTitle("تاریخ تولد (شمسی)")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("انصراف", action: onCancel)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("تأیید") {
                        if let d = day { onPick(YMD(y: year, m: month, d: d)) }
                    }
                    .disabled(day == nil)
                }
            }
        }
        .onChange(of: year) { _ in clampDay() }
        .onChange(of: month) { _ in clampDay() }
        .environment(\.layoutDirection, .rightToLeft)
    }

    private func clampDay() {
        if let d = day, d > Jalali.monthLength(year, month) { day = nil }
    }

    @ViewBuilder
    private func dayCell(_ d: Int) -> some View {
        let future = Jalali.isAfter(YMD(y: year, m: month, d: d), today)
        let selected = day == d
        Button {
            day = d
        } label: {
            Text(Jalali.toPersianDigits(String(d)))
                .frame(maxWidth: .infinity, minHeight: 36)
                .background(Circle().fill(selected ? Theme.emerald : Color.clear))
                .foregroundColor(selected ? Color.white : (future ? Color.secondary : Color.primary))
        }
        .disabled(future)
    }
}
