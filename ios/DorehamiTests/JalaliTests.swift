import XCTest
@testable import Dorehami

final class JalaliTests: XCTestCase {
    func testToGregorian() {
        XCTAssertEqual(Jalali.toGregorian(1379, 3, 1), YMD(y: 2000, m: 5, d: 21))
        XCTAssertEqual(Jalali.toGregorian(1403, 1, 1), YMD(y: 2024, m: 3, d: 20))
        XCTAssertEqual(Jalali.toGregorian(1403, 12, 30), YMD(y: 2025, m: 3, d: 20))
    }

    func testFromGregorian() {
        XCTAssertEqual(Jalali.fromGregorian(2000, 5, 21), YMD(y: 1379, m: 3, d: 1))
        XCTAssertEqual(Jalali.fromGregorian(2025, 3, 21), YMD(y: 1404, m: 1, d: 1))
        XCTAssertEqual(Jalali.fromGregorian(2000, 1, 1), YMD(y: 1378, m: 10, d: 11))
    }

    func testLeapYearsAndMonthLengths() {
        for y in [1395, 1399, 1403, 1408] { XCTAssertTrue(Jalali.isLeap(y), String(y)) }
        for y in [1400, 1401, 1402, 1404] { XCTAssertFalse(Jalali.isLeap(y), String(y)) }
        XCTAssertEqual(Jalali.monthLength(1403, 1), 31)
        XCTAssertEqual(Jalali.monthLength(1403, 7), 30)
        XCTAssertEqual(Jalali.monthLength(1403, 12), 30)
        XCTAssertEqual(Jalali.monthLength(1404, 12), 29)
    }

    func testRoundTripOverCentury() {
        var cal = Calendar(identifier: .gregorian)
        cal.timeZone = TimeZone(identifier: "UTC")!
        var date = cal.date(from: DateComponents(year: 1930, month: 1, day: 1))!
        let end = cal.date(from: DateComponents(year: 2030, month: 1, day: 1))!
        while date < end {
            let c = cal.dateComponents([.year, .month, .day], from: date)
            let j = Jalali.fromGregorian(c.year!, c.month!, c.day!)
            XCTAssertEqual(Jalali.toGregorian(j.y, j.m, j.d), YMD(y: c.year!, m: c.month!, d: c.day!))
            date = cal.date(byAdding: .day, value: 1, to: date)!
        }
    }

    func testFormatting() {
        XCTAssertEqual(Jalali.toGregorianString(1379, 3, 1), "2000-05-21")
        XCTAssertEqual(Jalali.format(1379, 3, 1), "1379/03/01")
        XCTAssertEqual(Jalali.toPersianDigits("1379/03/01"), "۱۳۷۹/۰۳/۰۱")
    }

    func testFirstWeekdayStartsFromSaturday() {
        XCTAssertEqual(Jalali.firstWeekday(1403, 1), 4) // ۱ فروردین ۱۴۰۳ = چهارشنبه
    }

    func testIsAfter() {
        let t = YMD(y: 1405, m: 9, d: 6)
        XCTAssertTrue(Jalali.isAfter(YMD(y: 1405, m: 9, d: 7), t))
        XCTAssertFalse(Jalali.isAfter(YMD(y: 1405, m: 9, d: 6), t))
        XCTAssertFalse(Jalali.isAfter(YMD(y: 1404, m: 12, d: 29), t))
    }
}
