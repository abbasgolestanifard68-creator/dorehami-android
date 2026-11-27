import XCTest
@testable import Dorehami

final class ValidatorsTests: XCTestCase {
    func testPersianAndArabicDigits() {
        XCTAssertEqual(Validators.normalizeDigits("۰۹۱۲۳۴۵۶۷۸۹"), "09123456789")
        XCTAssertEqual(Validators.normalizeDigits("٠٩١٢٣٤٥٦٧٨٩"), "09123456789")
    }

    func testValidPhones() {
        for p in ["09123456789", "+989123456789", "00989123456789", "9123456789", "۰۹۱۲ ۳۴۵ ۶۷۸۹", "0912-345-6789"] {
            XCTAssertEqual(Validators.normalizePhone(p), "09123456789", p)
        }
    }

    func testInvalidPhones() {
        XCTAssertNil(Validators.normalizePhone("12345"))
        XCTAssertNil(Validators.normalizePhone("08123456789"))
        XCTAssertNil(Validators.normalizePhone("abc"))
    }

    func testBirthDate() {
        XCTAssertEqual(Validators.normalizeBirthDate("۲۰۰۰-۰۵-۲۱"), "2000-05-21")
        XCTAssertEqual(Validators.normalizeBirthDate("2000/05/21"), "2000-05-21")
        XCTAssertNil(Validators.normalizeBirthDate("2000-13-01"))
        XCTAssertNil(Validators.normalizeBirthDate("2999-01-01"))
        XCTAssertNil(Validators.normalizeBirthDate("abc"))
    }

    func testStrongPasswords() {
        XCTAssertTrue(Validators.isStrongPassword("Abcdef1!"))
        XCTAssertTrue(Validators.isStrongPassword("dorehami_Ok9"))
    }

    func testWeakPasswords() {
        XCTAssertFalse(Validators.isStrongPassword("abcdefg1!")) // بدون حرف بزرگ
        XCTAssertFalse(Validators.isStrongPassword("ABCDEFG1!")) // بدون حرف کوچک
        XCTAssertFalse(Validators.isStrongPassword("Abcdefgh!")) // بدون عدد
        XCTAssertFalse(Validators.isStrongPassword("Abcdefg12")) // بدون علامت
        XCTAssertFalse(Validators.isStrongPassword("Ab1!")) // کوتاه
        XCTAssertFalse(Validators.isStrongPassword("Abcdef۱!")) // عدد فارسی
        XCTAssertFalse(Validators.isStrongPassword("Abcdef1 ")) // فاصله علامت نیست
    }
}
